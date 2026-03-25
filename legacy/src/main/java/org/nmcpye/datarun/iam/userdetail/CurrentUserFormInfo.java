package org.nmcpye.datarun.iam.userdetail;

import lombok.Builder;
import lombok.Value;

import java.util.Set;

/**
 * @author Hamza Assada 20/03/2025 (7amza.it@gmail.com)
 */
@Value
@Builder
public class CurrentUserFormInfo {
    Long userId;

    String userUID;

    Set<String> formUIDs;
}
