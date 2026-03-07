package io.github.trae.utilities;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Utility class containing helper methods related to {@link Class} reflection operations.
 *
 * <p>This class provides convenience methods for creating new instances of a class
 * using reflection while reducing repetitive boilerplate code.
 */
public final class UtilClass {

    /**
     * Creates a new instance of the specified class using reflection.
     *
     * <p>If arguments are provided, this method attempts to locate a constructor
     * matching the runtime types of the given arguments and invokes it.
     * If no arguments are provided, the default no-argument constructor is used.
     *
     * @param type the class type to instantiate
     * @param args optional constructor arguments
     * @param <T>  the type of the class being instantiated
     * @return a new instance of the specified class
     * @throws Exception if:
     *                   <ul>
     *                       <li>the constructor cannot be found</li>
     *                       <li>the constructor is inaccessible</li>
     *                       <li>the constructor throws an exception</li>
     *                       <li>the instance cannot be created</li>
     *                   </ul>
     */
    public static <T> T create(final Class<T> type, final Object... args) throws Exception {
        if (args != null && args.length > 0) {
            final Constructor<T> declaredConstructor = type.getDeclaredConstructor(Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));

            declaredConstructor.trySetAccessible();

            return declaredConstructor.newInstance(args);
        }

        final Constructor<T> declaredConstructor = type.getDeclaredConstructor();

        declaredConstructor.trySetAccessible();

        return declaredConstructor.newInstance();
    }

    public static <T> T create(final Class<T> type) throws Exception {
        return create(type, new Object[0]);
    }
}