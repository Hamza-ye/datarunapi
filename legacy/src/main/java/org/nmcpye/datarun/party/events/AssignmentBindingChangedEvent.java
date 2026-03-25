package org.nmcpye.datarun.party.events;

import org.nmcpye.datarun.datacapture.datasubmission.events.EventChangeType;
import org.nmcpye.datarun.party.entities.AssignmentRolePartyPolicy;

public record AssignmentBindingChangedEvent(AssignmentRolePartyPolicy binding, EventChangeType type) {
}
