package io.github.trae.utilities;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

public final class UtilJava {

    public static <T> T cast(final Class<T> clazz, final Object object) {
        if ((clazz != null && object != null) && clazz.isInstance(object)) {
            try {
                return clazz.cast(object);
            } catch (Exception e) {
                throw new RuntimeException("Failed to cast object to %s".formatted(clazz.getName()), e);
            }
        }

        return null;
    }

    public static <T extends Collection<?>> T createCollection(final T collection, final Consumer<T> consumer) {
        consumer.accept(collection);
        return collection;
    }

    public static <T extends Map<?, ?>> T createMap(final T map, final Consumer<T> consumer) {
        consumer.accept(map);
        return map;
    }

    public static <T extends Collection<?>> void updateCollection(final T collection, final Consumer<T> consumer) {
        consumer.accept(collection);
    }

    public static <T extends Map<?, ?>> void updateMap(final T map, final Consumer<T> consumer) {
        consumer.accept(map);
    }
}