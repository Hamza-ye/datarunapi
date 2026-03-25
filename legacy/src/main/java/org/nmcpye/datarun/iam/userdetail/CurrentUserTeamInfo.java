package org.nmcpye.datarun.iam.userdetail;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

/**
 * @author Hamza Assada 20/03/2025 (7amza.it@gmail.com)
 */
@Value
@Builder
public class CurrentUserTeamInfo {
    String userId;

    String userUID;

    Set<String> teamIds;
    Set<String> teamUIDs;

    Set<String> managedTeamIds;
    Set<String> managedTeamUIDs;
}
