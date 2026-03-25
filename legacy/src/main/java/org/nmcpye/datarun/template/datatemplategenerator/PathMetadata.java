package org.nmcpye.datarun.template.datatemplategenerator;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Simple POJO returned by PathResolver
 *
 * @author Hamza Assada
 * @since 09/09/2025
 */
@Getter
@AllArgsConstructor
public class PathMetadata {
    private final String jsonDataIdPath;
    private final String jsonDataPath;
    // null if submission-level
    private final String canonicalPath;
    private final Boolean hasParentRepeat;
    // full idPath to nearest repeatable ancestor (or null)
    private final String parentRepeatIdPath;
    // repeat-only path to nearest repeatable ancestor
    private final String canonicalParentRepeatPath;
}
