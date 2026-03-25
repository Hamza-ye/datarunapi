package org.nmcpye.datarun.template.datatemplateprocessor;

import org.apache.commons.collections4.map.UnmodifiableMap;
import org.nmcpye.datarun.template.datatemplateelement.AbstractElement;
import org.nmcpye.datarun.template.datatemplateelement.FieldTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateelement.SectionTemplateElementDto;
import org.nmcpye.datarun.template.datatemplateprocessor.postprocessors.AbstractTemplateElementHandler;
import org.nmcpye.datarun.template.dataelement.DataElement;
import org.nmcpye.datarun.template.datatemplate.dto.DataTemplateVersionInterface;
import org.nmcpye.datarun.sharedkernal.exceptions.IllegalQueryException;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorCode;
import org.nmcpye.datarun.sharedkernal.feedback.ErrorMessage;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TemplateElementProcessor {
    private final DataTemplateVersionInterface formTemplate;
    final private Map<String, SectionTemplateElementDto> sectionMap;

    public TemplateElementProcessor(DataTemplateVersionInterface formTemplate) {
        this.formTemplate = formTemplate;
        this.sectionMap = UnmodifiableMap.unmodifiableMap( formTemplate.getSections()
            .stream().collect(Collectors
                .toMap(AbstractElement::getName, Function.identity())));
    }

    /**
     * @return FormElementConfigService to run the next step of get the formTemplate
     */
    private TemplateElementProcessor configureAndValidateSections() {
        final var formSections = new HashSet<>(formTemplate.getSections());
        final var sections = formSections.stream()
            .map(AbstractTemplateElementHandler::processSection)
            .peek(s -> s.path(buildPath(s, sectionMap))).toList();

        formTemplate.sections(sections);
        return this;
    }

    /**
     * @param dataTemplateElements data elements corresponding to the fields, should be fetched from repository and passed
     * @return FormElementConfigService to run the next step of get the formTemplate
     */
    private TemplateElementProcessor configureAndValidateFields(Collection<DataElement> dataTemplateElements) {
        final var dataElementMap = dataTemplateElements.stream()
            .collect(Collectors.toMap(DataElement::getUid, Function.identity()));
        final var fields = formTemplate.getFields().stream().distinct()
            .map((f) ->
                AbstractTemplateElementHandler
                    .processElement(f,
                        Optional.ofNullable(dataElementMap.get(f.getId()))
                            .orElseThrow(() -> new IllegalQueryException(ErrorCode.E1100, formTemplate.getUid(),
                                f.getId()))))
            .peek(f -> f.path(buildPath(f, sectionMap)))
            .toList();

        formTemplate.fields(fields);
        return this;
    }

    public TemplateElementProcessor process(Collection<DataElement> dataTemplateElements) {
        return configureAndValidateSections().configureAndValidateFields(dataTemplateElements);
    }

    public DataTemplateVersionInterface get() {
        return formTemplate;
    }

    /**
     * Builds the full path for a form element based on its parent's chain.
     * If an element has no parent, its path is simply its ID.
     *
     * @param element    The form element (field or section) for which to build the path.
     * @param sectionMap The Map of all sections.
     * @return The computed path (e.g., "mainSection.householdnames.P1D5FiaiHFP").
     */
    private <T extends AbstractElement> String buildPath(T element, Map<String, SectionTemplateElementDto> sectionMap) {
        Deque<String> pathParts = new ArrayDeque<>();
        pathParts.push(getKey(element));

        Set<String> visited = new HashSet<>();

        String parentId = element.getParent();
        while (parentId != null) {
            if (!visited.add(parentId)) {
                // a cycle exist.
                throw new IllegalQueryException(new ErrorMessage(ErrorCode.E1107, formTemplate.getUid(), parentId));

            }

            SectionTemplateElementDto parentSection = sectionMap.get(parentId);
            if (parentSection == null) {
                throw new IllegalQueryException(new ErrorMessage(ErrorCode.E1101,
                    formTemplate.getUid(), getKey(element), parentId));
            }
            pathParts.push(getKey(parentSection));
            parentId = parentSection.getParent();
        }

        // Join the parts with '.' to form the path.
        return String.join(".", pathParts);
    }

    private String getKey(AbstractElement element) {
        if (element instanceof FieldTemplateElementDto elementConf) {
            return elementConf.getId();
        } else {
            // is section
            return element.getName();
        }
    }

    private Map<String, SectionTemplateElementDto> getSectionMap() {
        return formTemplate.getSections()
            .stream().collect(Collectors.toMap(AbstractElement::getName, Function.identity()));
    }
}
