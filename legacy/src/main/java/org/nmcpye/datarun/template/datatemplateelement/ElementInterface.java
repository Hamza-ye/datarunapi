package org.nmcpye.datarun.template.datatemplateelement;

import java.util.List;
import java.util.Map;

/**
 * @author Hamza Assada
 * @since 27/05/2025
 */
public interface ElementInterface {
    String getId();

    String getPath();

    String getParent();

    String getCode();

    String getName();

    String getDescription();

    Integer getOrder();

    Map<String, String> getLabel();

    List<DataFieldRule> getRules();


    ElementInterface path(String path);

    Map<String, Object> getProperties();
}
