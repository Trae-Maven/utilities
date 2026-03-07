package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

/**
 * General-purpose Java utility methods for safe casting and collection/map initialization.
 */
@UtilityClass
public final class UtilJava {

    /**
     * Safely cast an object to the specified type.
     * Returns null if the object is null, the class is null, or the object is not an instance of the class.
     *
     * @param clazz  the target class to cast to
     * @param object the object to cast
     * @param <T>    the target type
     * @return the cast object, or null if the cast is not possible
     */
    public static <T> T cast(final Class<T> clazz, final Object object) {
        if ((clazz != null && object != null) && clazz.isInstance(object)) {
            try {
                return clazz.cast(object);
            } catch (final Exception e) {
                throw new RuntimeException("Failed to cast object to %s".formatted(clazz.getName()), e);
            }
        }

        return null;
    }

    /**
     * Initialize a collection using a consumer and return it.
     * Useful for inline collection population.
     *
     * @param collection the collection to populate
     * @param consumer   the initializer
     * @param <T>        the collection type
     * @return the populated collection
     */
    public static <T extends Collection<?>> T createCollection(final T collection, final Consumer<T> consumer) {
        consumer.accept(collection);
        return collection;
    }

    /**
     * Initialize a map using a consumer and return it.
     * Useful for inline map population.
     *
     * @param map      the map to populate
     * @param consumer the initializer
     * @param <T>      the map type
     * @return the populated map
     */
    public static <T extends Map<?, ?>> T createMap(final T map, final Consumer<T> consumer) {
        consumer.accept(map);
        return map;
    }

    /**
     * Mutate an existing collection using a consumer.
     *
     * @param collection the collection to update
     * @param consumer   the mutator
     * @param <T>        the collection type
     */
    public static <T extends Collection<?>> void updateCollection(final T collection, final Consumer<T> consumer) {
        consumer.accept(collection);
    }

    /**
     * Mutate an existing map using a consumer.
     *
     * @param map      the map to update
     * @param consumer the mutator
     * @param <T>      the map type
     */
    public static <T extends Map<?, ?>> void updateMap(final T map, final Consumer<T> consumer) {
        consumer.accept(map);
    }
}