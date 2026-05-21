package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Constructor;
import java.util.Arrays;

/**
 * Utility class containing helper methods related to {@link Class} reflection operations.
 *
 * <p>This class provides convenience methods for creating new instances of a class
 * using reflection while reducing repetitive boilerplate code.
 */
@UtilityClass
public class UtilClass {

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
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }

        if (args != null && args.length > 0) {
            final Constructor<T> declaredConstructor = type.getDeclaredConstructor(Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));

            declaredConstructor.trySetAccessible();

            return declaredConstructor.newInstance(args);
        }

        final Constructor<T> declaredConstructor = type.getDeclaredConstructor();

        declaredConstructor.trySetAccessible();

        return declaredConstructor.newInstance();
    }

    /**
     * Creates a new instance of the specified class using the default
     * no-argument constructor.
     *
     * @param type the class type to instantiate
     * @param <T>  the type of the class being instantiated
     * @return a new instance of the specified class
     * @throws Exception if the constructor cannot be found, is inaccessible,
     *                   or throws an exception
     * @see #create(Class, Object...)
     */
    public static <T> T create(final Class<T> type) throws Exception {
        return create(type, new Object[0]);
    }

    /**
     * Formats a class name by stripping the given base package prefix
     * and returning the remaining qualified path.
     *
     * @param basePackage the base package prefix to strip (e.g. {@code me.example.project})
     * @param type        the class to format
     * @return the trimmed qualified name
     * @throws IllegalArgumentException if {@code basePackage} or {@code type} is null
     */
    public static String formatName(final String basePackage, final Class<?> type) {
        if (basePackage == null) {
            throw new IllegalArgumentException("Base package cannot be null.");
        }

        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }

        final String name = type.getName();

        if (name.startsWith(basePackage + ".")) {
            return name.substring(basePackage.length() + 1);
        }

        return name;
    }

    /**
     * Formats a class name by stripping a 3-segment base package prefix
     * (e.g. {@code me.example.project}) and returning the remaining
     * qualified path.
     *
     * @param type the class to format
     * @return the trimmed qualified name
     * @throws IllegalArgumentException if {@code type} is null
     */
    public static String formatName(final Class<?> type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null.");
        }

        final String name = type.getName();
        final String[] parts = name.split("\\.");

        if (parts.length <= 3) {
            return name;
        }

        final String basePackage = String.join(".", Arrays.copyOfRange(parts, 0, 3));

        return formatName(basePackage, type);
    }
}