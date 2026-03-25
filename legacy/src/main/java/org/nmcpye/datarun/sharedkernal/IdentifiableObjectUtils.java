package org.nmcpye.datarun.sharedkernal;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.datetime.standard.InstantFormatter;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class IdentifiableObjectUtils {

    public static final String SEPARATOR = "-";

    public static final String SEPARATOR_JOIN = ", ";

    /**
     * Joins the names of the IdentifiableObjects in the given list and
     * separates them with {@link IdentifiableObjectUtils#SEPARATOR_JOIN} (a
     * comma and a space). Returns null if the given list is null or has no
     * elements.
     *
     * @param objects the list of IdentifiableObjects.
     * @return the joined string.
     */
    public static String join(Collection<? extends JpaIdentifiableObject> objects) {
        if (objects == null || objects.isEmpty()) {
            return null;
        }

        List<String> names = objects.stream().map(JpaIdentifiableObject::getName).collect(Collectors.toList());

        return StringUtils.join(names, SEPARATOR_JOIN);
    }

    /**
     * Returns a list of uids for the given collection of IdentifiableObjects.
     *
     * @param objects the list of IdentifiableObjects.
     * @return a list of uids.
     */
    public static <T extends JpaIdentifiableObject> List<String> getUids(Collection<T> objects) {
        return objects != null ? objects.stream().filter(Objects::nonNull)
            .map(IdentifiableObject::getUid).collect(Collectors.toList()) : null;
    }

    /**
     * Returns a list of codes for the given collection of IdentifiableObjects.
     *
     * @param objects the list of IdentifiableObjects.
     * @return a list of codes.
     */
    public static <T extends JpaIdentifiableObject> List<String> getCodes(Collection<T> objects) {
        return objects != null ? objects.stream().map(IdentifiableObject::getCode).collect(Collectors.toList()) : null;
    }

    /**
     * Returns a list of internal identifiers for the given collection of
     * IdentifiableObjects.
     *
     * @param objects the list of IdentifiableObjects.
     * @return a list of identifiers.
     */
    public static <T extends IdentifiableObject<E>, E> List<E> getIdentifiers(Collection<T> objects) {
        return objects != null ? objects.stream().map(IdentifiableObject::getId).collect(Collectors.toList()) : null;
    }

    /**
     * Returns a map from internal identifiers to IdentifiableObjects, for the
     * given collection of IdentifiableObjects.
     *
     * @param objects the collection of IdentifiableObjects
     * @return a map from the object internal identifiers to the objects
     */
    public static <T extends IdentifiableObject<E>, E> Map<E, T> getIdentifierMap(Collection<T> objects) {
        Map<E, T> map = new HashMap<>();

        for (T object : objects) {
            map.put(object.getId(), object);
        }

        return map;
    }

    /**
     * Filters the given list of IdentifiableObjects based on the given key.
     *
     * @param identifiableObjects the list of IdentifiableObjects.
     * @param key                 the key.
     * @param ignoreCase          indicates whether to ignore case when filtering.
     * @return a filtered list of IdentifiableObjects.
     */
    public static <T extends JpaIdentifiableObject> List<T> filterNameByKey(List<T> identifiableObjects, String key, boolean ignoreCase) {
        List<T> objects = new ArrayList<>();
        ListIterator<T> iterator = identifiableObjects.listIterator();

        if (ignoreCase) {
            key = key.toLowerCase();
        }

        while (iterator.hasNext()) {
            T object = iterator.next();
            String name = ignoreCase ? object.getName().toLowerCase() : object.getName();

            if (name.contains(key)) {
                objects.add(object);
            }
        }

        return objects;
    }

    /**
     * Removes duplicates from the given list while maintaining the order.
     *
     * @param list the list.
     */
    public static <T extends JpaIdentifiableObject> List<T> removeDuplicates(List<T> list) {
        final List<T> temp = new ArrayList<>(list);
        list.clear();

        for (T object : temp) {
            if (!list.contains(object)) {
                list.add(object);
            }
        }

        return list;
    }

    /**
     * Generates a tag reflecting the date of when the most recently updated
     * JpaIdentifiableObject in the given collection was modified.
     *
     * @param objects the collection of IdentifiableObjects.
     * @return a string tag.
     */
    public static <T extends JpaIdentifiableObject> String getLastUpdatedTag(Collection<T> objects) {
        Instant latest = null;

        if (objects != null) {
            for (JpaIdentifiableObject object : objects) {
                if (
                    object != null &&
                        object.getLastModifiedDate() != null &&
                        (latest == null || object.getLastModifiedDate().isAfter(latest))
                ) {
                    latest = object.getLastModifiedDate();
                }
            }
        }

        return latest != null ? objects.size() + SEPARATOR + new InstantFormatter().print(latest, Locale.US) : null;
    }

    /**
     * Returns a mapping between the uid and the display name of the given
     * identifiable objects.
     *
     * @param objects the identifiable objects.
     * @return mapping between the uid and the display name of the given
     * objects.
     */
    public static Map<String, String> getUidNameMap(Collection<? extends JpaIdentifiableObject> objects) {
        return objects.stream().collect(Collectors.toMap(JpaIdentifiableObject::getUid, JpaIdentifiableObject::getName));
    }

    /**
     * Returns a mapping between the uid and the property defined by the given
     * identifiable property for the given identifiable objects.
     *
     * @param objects  the identifiable objects.
     * @param property the identifiable property.
     * @return a mapping between uid and property.
     */
    public static Map<String, String> getUidPropertyMap(Collection<? extends JpaIdentifiableObject> objects, IdentifiableProperty property) {
        Map<String, String> map = Maps.newHashMap();

        objects.forEach(obj ->
            map.put(obj.getUid(), obj.getPropertyValue(IdScheme.from(property))));

        return map;
    }

    /**
     * Returns a mapping between the uid and the name of the given identifiable
     * objects.
     *
     * @param objects the identifiable objects.
     * @return mapping between the uid and the name of the given objects.
     */
    public static <T extends JpaIdentifiableObject> Map<String, T> getUidObjectMap(Collection<T> objects) {
        return objects != null ? Maps.uniqueIndex(objects, T::getUid) : Maps.newHashMap();
    }

    /**
     * Returns a map of the identifiable property specified by the given id
     * scheme and the corresponding object.
     *
     * @param objects  the objects.
     * @param idScheme the id scheme.
     * @return a map.
     */
    public static <T extends JpaIdentifiableObject> Map<String, T> getIdMap(List<T> objects, IdScheme idScheme) {
        Map<String, T> map = new HashMap<>();

        for (T object : objects) {
            String value = object.getPropertyValue(idScheme);

            if (value != null) {
                map.put(value, object);
            }
        }

        return map;
    }

    /**
     * @param object Object to get display name for
     * @return A usable display name
     */
    public static String getDisplayName(Object object) {
        if (object == null) {
            return "[ object is null ]";
        } else if (object instanceof JpaIdentifiableObject identifiableObject) {

            if (identifiableObject.getName() != null && !identifiableObject.getName().isEmpty()) {
                return identifiableObject.getName();
            } else if (identifiableObject.getUid() != null && !identifiableObject.getUid().isEmpty()) {
                return identifiableObject.getUid();
            } else if (identifiableObject.getCode() != null && !identifiableObject.getCode().isEmpty()) {
                return identifiableObject.getCode();
            }
        }

        return object.getClass().getName();
    }

    /**
     * Returns an ID for given object based on given idScheme. However, does not
     * work for Attribute idScheme. Attribute idScheme has to have special
     * treatment in the client code.
     *
     * @param object   An identifiable object
     * @param idScheme An idScheme defining what property should be used as an
     *                 ID
     * @param <T>
     * @return Returns an ID for given object based on given idScheme
     */
    public static <T extends JpaIdentifiableObject> String getIdentifierBasedOnIdScheme(T object, IdScheme idScheme) {
        if (idScheme.isNull() || idScheme.is(IdentifiableProperty.UID)) {
            return object.getUid();
        } else if (idScheme.is(IdentifiableProperty.CODE)) {
            return object.getCode();
        } else if (idScheme.is(IdentifiableProperty.NAME)) {
            return object.getName();
        } else if (idScheme.is(IdentifiableProperty.ID) && object.getId() != null) {
            return String.valueOf(object.getId());
        }

        return null;
    }

    /**
     * Converts the given {@link Set} to a mutable {@link List} and sorts the
     * items by the ID property.
     *
     * @param <T>
     * @param set the {@link Set}.
     * @return a {@link List}.
     */
    public static <T extends JpaIdentifiableObject> List<T> sortById(Set<T> set) {
        List<T> list = new ArrayList<>(set);
        Collections.sort(list, Comparator.comparing(T::getId));
        return list;
    }

    /**
     * Compare two {@link JpaIdentifiableObject} using UID property.
     *
     * @param object object to compare.
     * @param target object to compare with.
     * @return TRUE if both objects are null or have same UID or both UIDs are
     * null. Otherwise, return FALSE.
     */
    public static boolean equalByUID(JpaIdentifiableObject object, JpaIdentifiableObject target) {
        if (ObjectUtils.allNotNull(object, target)) {
            if (ObjectUtils.allNotNull(object.getUid(), target.getUid())) {
                return object.getUid().equals(target.getUid());
            }

            return ObjectUtils.allNull(object.getUid(), target.getUid());
        }

        return ObjectUtils.allNull(object, target);
    }
}

