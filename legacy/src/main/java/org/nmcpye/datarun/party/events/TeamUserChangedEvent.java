package org.nmcpye.datarun.party.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
public class TeamUserChangedEvent {
    private String teamId;
    private String teamUid;

    private String userId;
    private String userUid;
}
