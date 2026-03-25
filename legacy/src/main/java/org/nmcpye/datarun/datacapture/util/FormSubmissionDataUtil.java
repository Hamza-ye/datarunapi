package org.nmcpye.datarun.datacapture.util;

import java.util.*;

public class FormSubmissionDataUtil {
    public static Map<String, Object> flatten(Map<String, ?> map, boolean all, boolean sortedKeys) {
        Map<String, Object> flatMap = all ? flattenAll(map, null) : flattenSections(map, null);
        return sortedKeys ? new TreeMap<>(flatMap) : flatMap;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> flattenAll(Map<String, ?> map, String parentKey) {
        Map<String, Object> flatMap = new HashMap<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = parentKey != null ? parentKey + "/" + entry.getKey() : entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatMap.putAll(flattenAll((Map<String, ?>) value, key));
            } else if (value instanceof List) {
                List<Object> list = (List<Object>) value;
                for (int i = 0; i < list.size(); i++) {
                    Object listItem = list.get(i);
                    String listKey = key + "[" + i + "]";
                    if (listItem instanceof Map) {
                        flatMap.putAll(flattenAll((Map<String, Object>) listItem, listKey));
                    } else {
                        flatMap.put(listKey, listItem);
                    }
                }
            } else {
                flatMap.put(key, value);
            }
        }
        return flatMap;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> flattenSections(Map<String, ?> map, String parentKey) {
        Map<String, Object> flatMap = new HashMap<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            String key = parentKey != null ? parentKey + "/" + entry.getKey() : entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                flatMap.putAll(flattenSections((Map<String, ?>) value, key));
            } else if (entry.getValue() instanceof List) {
                List<?> list = (List<?>) value;
                List<Object> newList = new ArrayList<>();
                for (final var listItem : list) {
                    if (listItem instanceof Map) {
                        Map<String, Object> flattenedListItem = flattenSections((Map<String, ?>) listItem, key);
                        newList.add(flattenedListItem);
                    } else {
                        newList.add(listItem);
                    }
                }
                flatMap.put(key, newList);
            } else {
                flatMap.put(key, value);
            }
        }
        return flatMap;
    }
}
