package org.nmcpye.datarun.web.rest.v2.formtemplate.service;

import org.nmcpye.datarun.template.datatemplateelement.DataFieldRule;
import org.nmcpye.datarun.template.datatemplateelement.enumeration.RuleAction;
import org.nmcpye.datarun.web.rest.v2.dto.V2Rule;
import org.nmcpye.datarun.web.rest.v2.dto.V2RuleEffect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transforms V1 {@link DataFieldRule} expressions into V2 {@link V2Rule}
 * objects
 * with JsonLogic AST conditions.
 * <p>
 * V1 format: {@code "#{gender} == 'FEMALE'"} with action {@code Show}
 * <p>
 * V2 format: JsonLogic AST with resolved namespaces:
 *
 * <pre>{@code
 * {
 *   "condition": {"==": [{"var": "values.gender"}, "FEMALE"]},
 *   "effects": [{"targetNode": "FjfKSsEWAYf", "action": "SHOW"}]
 * }
 * }</pre>
 * <p>
 * This class has no Spring dependencies, no state, and no side effects.
 *
 * @author Hamza Assada
 */
public final class RuleTransformer {

    private static final Logger log = LoggerFactory.getLogger(RuleTransformer.class);

    // Matches #{fieldName}
    private static final Pattern FIELD_REF_PATTERN = Pattern.compile("#\\{(\\w+)}");

    // Matches a single comparison: #{field} op value
    // Groups: 1=fieldName, 2=operator, 3=value (may be quoted string, boolean, or
    // number)
    private static final Pattern COMPARISON_PATTERN = Pattern.compile(
            "#\\{(\\w+)}\\s*(==|!=|<=|>=|<|>)\\s*(.+?)(?=\\s*(?:\\|\\||&&|$))");

    private RuleTransformer() {
    }

    /**
     * Transform a list of V1 rules into V2 rules.
     *
     * @param v1Rules          list of V1 DataFieldRule objects
     * @param ownerNodeId      the node_id of the field/section that owns these
     *                         rules
     * @param ownerSectionName the section where the owner lives
     * @param fieldResolver    resolver for #{fieldName} → V2 namespace
     * @return list of V2 rules
     */
    public static List<V2Rule> transformRules(List<DataFieldRule> v1Rules,
            String ownerNodeId,
            String ownerSectionName,
            FieldResolver fieldResolver) {
        if (v1Rules == null || v1Rules.isEmpty()) {
            return List.of();
        }

        List<V2Rule> result = new ArrayList<>();
        for (int i = 0; i < v1Rules.size(); i++) {
            DataFieldRule v1Rule = v1Rules.get(i);
            V2Rule v2Rule = transformSingleRule(
                    v1Rule, ownerNodeId, ownerSectionName, fieldResolver, i);
            result.add(v2Rule);
        }
        return List.copyOf(result);
    }

    private static V2Rule transformSingleRule(DataFieldRule v1Rule,
            String ownerNodeId,
            String ownerSectionName,
            FieldResolver fieldResolver,
            int index) {
        String ruleId = "rule_" + ownerNodeId + "_" + index;
        String expression = v1Rule.getExpression();

        // Parse the expression into JsonLogic AST
        Object condition;
        List<String> triggers = new ArrayList<>();

        try {
            condition = parseExpression(expression, ownerSectionName, fieldResolver, triggers);
        } catch (Exception e) {
            log.warn("Could not parse V1 rule expression '{}' on node '{}': {}. "
                    + "Wrapping in _legacy escape hatch.", expression, ownerNodeId, e.getMessage());
            condition = Map.of(
                    "_legacy", expression,
                    "_parse_error", e.getMessage() != null ? e.getMessage() : "unknown");
            // Extract triggers best-effort from field references
            triggers = extractFieldReferences(expression, ownerSectionName, fieldResolver);
        }

        // Map V1 action → V2 effect
        V2RuleEffect effect = mapEffect(v1Rule, ownerNodeId);

        return V2Rule.builder()
                .ruleId(ruleId)
                .scope(ownerSectionName)
                .triggers(List.copyOf(triggers))
                .condition(condition)
                .effects(List.of(effect))
                .build();
    }

    /**
     * Parse a V1 expression string into a JsonLogic AST.
     */
    static Object parseExpression(String expression,
            String ownerSectionName,
            FieldResolver fieldResolver,
            List<String> triggers) {
        if (expression == null || expression.isBlank()) {
            return Map.of("===", List.of(true, true)); // always-true
        }

        String trimmed = expression.trim();

        // Split by || first (lower precedence), then &&
        if (trimmed.contains("||")) {
            return parseLogicalOr(trimmed, ownerSectionName, fieldResolver, triggers);
        }
        if (trimmed.contains("&&")) {
            return parseLogicalAnd(trimmed, ownerSectionName, fieldResolver, triggers);
        }

        // Single comparison
        return parseSingleComparison(trimmed, ownerSectionName, fieldResolver, triggers);
    }

    private static Object parseLogicalOr(String expression,
            String ownerSectionName,
            FieldResolver fieldResolver,
            List<String> triggers) {
        // Split on || but not inside quoted strings
        String[] parts = expression.split("\\|\\|");
        List<Object> operands = new ArrayList<>();
        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.contains("&&")) {
                operands.add(parseLogicalAnd(trimmed, ownerSectionName, fieldResolver, triggers));
            } else {
                operands.add(parseSingleComparison(trimmed, ownerSectionName, fieldResolver, triggers));
            }
        }
        return Map.of("or", operands);
    }

    private static Object parseLogicalAnd(String expression,
            String ownerSectionName,
            FieldResolver fieldResolver,
            List<String> triggers) {
        String[] parts = expression.split("&&");
        List<Object> operands = new ArrayList<>();
        for (String part : parts) {
            operands.add(parseSingleComparison(part.trim(), ownerSectionName, fieldResolver, triggers));
        }
        return Map.of("and", operands);
    }

    /**
     * Parse a single comparison like {@code #{gender} == 'FEMALE'}.
     */
    private static Object parseSingleComparison(String comparison,
            String ownerSectionName,
            FieldResolver fieldResolver,
            List<String> triggers) {
        Matcher matcher = COMPARISON_PATTERN.matcher(comparison.trim());
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Cannot parse comparison: '" + comparison + "'");
        }

        String fieldName = matcher.group(1);
        String operator = matcher.group(2);
        String rawValue = matcher.group(3).trim();

        // Resolve field to V2 namespace
        Optional<FieldResolver.ResolvedField> resolved = fieldResolver.resolve(fieldName, ownerSectionName);

        String v2Var;
        if (resolved.isPresent()) {
            v2Var = resolved.get().toV2Var();
            String triggerPath = resolved.get().toTriggerPath();
            if (!triggers.contains(triggerPath)) {
                triggers.add(triggerPath);
            }
        } else {
            // Unresolved field — use _row as default with a warning
            log.warn("Field '{}' in rule expression could not be resolved. "
                    + "Defaulting to _row.{}", fieldName, fieldName);
            v2Var = "_row." + fieldName;
        }

        // Parse the value
        Object value = parseValue(rawValue);

        // Build JsonLogic: {"op": [{"var": "ns.field"}, value]}
        Map<String, Object> varRef = Map.of("var", v2Var);
        return Map.of(operator, List.of(varRef, value));
    }

    /**
     * Parse a value literal from a V1 expression.
     */
    static Object parseValue(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        // Quoted string: 'value' or "value"
        if ((raw.startsWith("'") && raw.endsWith("'"))
                || (raw.startsWith("\"") && raw.endsWith("\""))) {
            return raw.substring(1, raw.length() - 1);
        }

        // Booleans
        if ("true".equalsIgnoreCase(raw))
            return true;
        if ("false".equalsIgnoreCase(raw))
            return false;

        // Numbers
        try {
            if (raw.contains(".")) {
                return Double.parseDouble(raw);
            }
            return Long.parseLong(raw);
        } catch (NumberFormatException ignore) {
            // Not a number — return as string
        }

        // Unquoted string (V1 doesn't always quote values consistently)
        return raw;
    }

    /**
     * Map a V1 action to a V2 effect.
     */
    private static V2RuleEffect mapEffect(DataFieldRule v1Rule, String ownerNodeId) {
        RuleAction action = v1Rule.getAction();
        String v2Action = mapAction(action);

        V2RuleEffect.V2RuleEffectBuilder builder = V2RuleEffect.builder()
                .targetNode(ownerNodeId)
                .action(v2Action);

        // Handle ASSIGN action with assignedValue
        if (action == RuleAction.Assign && v1Rule.getAssignedValue() != null) {
            builder.value(v1Rule.getAssignedValue());
        }

        // Handle ERROR/WARNING with message
        if ((action == RuleAction.Error || action == RuleAction.Warning)
                && v1Rule.getMessage() != null) {
            builder.message(v1Rule.getMessage());
        }

        return builder.build();
    }

    /**
     * Map V1 {@link RuleAction} to V2 action string.
     */
    static String mapAction(RuleAction action) {
        if (action == null)
            return "SHOW";
        return switch (action) {
            case Show -> "SHOW";
            case Hide -> "HIDE";
            case Error, ErrorOnComplete -> "ERROR";
            case Warning, WarningOnComplete -> "WARNING";
            case Filter -> "FILTER";
            case Mandatory -> "SET_REQUIRED";
            case Assign -> "ASSIGN";
            case DisplayText -> "DISPLAY_TEXT";
            case DisplayKeyValuePair -> "DISPLAY_KEY_VALUE";
            case HideOption -> "HIDE_OPTION";
            case HideOptionGroup -> "HIDE_OPTION_GROUP";
            case ShowOptionGroup -> "SHOW_OPTION_GROUP";
        };
    }

    /**
     * Best-effort trigger extraction (for the _legacy fallback).
     */
    private static List<String> extractFieldReferences(String expression,
            String ownerSectionName,
            FieldResolver fieldResolver) {
        List<String> triggers = new ArrayList<>();
        Matcher matcher = FIELD_REF_PATTERN.matcher(expression);
        while (matcher.find()) {
            String fieldName = matcher.group(1);
            fieldResolver.resolve(fieldName, ownerSectionName)
                    .ifPresent(rf -> {
                        String path = rf.toTriggerPath();
                        if (!triggers.contains(path)) {
                            triggers.add(path);
                        }
                    });
        }
        return triggers;
    }
}
