package org.nmcpye.datarun.jpa.accessfilter.event;

import org.springframework.context.ApplicationEvent;

/**
 * Domain event fired when a user's access rules (Teams, Assignments,
 * UserGroups, etc.)
 * have changed in the 'Planning' context. This signals the 'Execution'
 * projectors
 * to rebuild the materialized CQRS views for that specific user.
 * 
 * @author Hamza Assada
 */
public class UserAccessRulesChangedEvent extends ApplicationEvent {
    private final String userLogin;

    /**
     * @param source    The object on which the event initially occurred.
     * @param userLogin The login/username of the user whose rules changed.
     */
    public UserAccessRulesChangedEvent(Object source, String userLogin) {
        super(source);
        this.userLogin = userLogin;
    }

    public String getUserLogin() {
        return userLogin;
    }
}
