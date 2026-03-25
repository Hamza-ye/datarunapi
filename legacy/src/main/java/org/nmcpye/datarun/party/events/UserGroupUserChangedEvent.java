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
public class UserGroupUserChangedEvent {
    private String groupId;
    private String groupUid;

    private String userId;
    private String userUid;
}
