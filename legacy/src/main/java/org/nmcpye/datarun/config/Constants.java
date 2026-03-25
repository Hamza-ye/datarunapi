package org.nmcpye.datarun.config;

import com.google.common.collect.ImmutableSet;

import javax.imageio.ImageIO;
import java.util.Set;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";
    public static final String DEFAULT_LANGUAGE = "en";

    private Constants() {
    }

    public static final int MAX_ATTR_VALUE_LENGTH = 1200;

    public static final int SPLIT_LIST_PARTITION_SIZE = 20_000;

    public static final int ATTRIBUTE_VALUE_MAX_LENGTH = 50000;

    public static final Set<String> VALID_IMAGE_FORMATS = ImmutableSet.<String>builder().add(
        ImageIO.getReaderFormatNames()).build();
}
