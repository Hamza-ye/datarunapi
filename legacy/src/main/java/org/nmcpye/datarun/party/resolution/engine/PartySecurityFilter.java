package org.nmcpye.datarun.party.resolution.engine;

import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import static org.nmcpye.datarun.jooq.public_.Tables.PARTY;
import static org.nmcpye.datarun.jooq.public_.Tables.USER_EXECUTION_CONTEXT;

@Component
public class PartySecurityFilter {

    /**
     * Applies the permission filter to an ongoing query.
     *
     * @param query          The base query (selecting from PARTY or joining PARTY)
     * @param userId         The requesting user UID (Business Key)
     * @param isMaterialized If false, the set is Public/Unsecured, so we skip the
     *                       check.
     * @return The query with the security JOIN/WHERE clause appended
     */
    public <R extends org.jooq.Record> SelectConditionStep<R> apply(
            SelectConditionStep<R> query,
            String userId,
            boolean isMaterialized) {
        if (!isMaterialized) {
            // Public set (e.g., Generic Option List, Survey Locations) -> No security check
            // needed
            return query;
        }

        // Secure set: Check for existence in user_execution_context.
        // This automatically filters out any party not present in the permission table
        // for this user.
        return query
                .andExists(
                        DSL.selectOne()
                                .from(USER_EXECUTION_CONTEXT)
                                .where(USER_EXECUTION_CONTEXT.ENTITY_UID.eq(PARTY.UID))
                                .and(USER_EXECUTION_CONTEXT.USER_UID.eq(userId)));
    }
}
