package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Method;

/**
 * Utility class for reflective {@link Method} operations.
 *
 * <p>This class simplifies method invocation using reflection while performing
 * basic validation and accessibility handling.
 */
@UtilityClass
public class UtilMethod {

    /**
     * Invokes the specified method on the given instance using reflection.
     *
     * <p>This method automatically sets the method as accessible, allowing
     * invocation of private or protected methods.
     *
     * @param method   the method to invoke
     * @param instance the object instance on which the method will be invoked
     * @param args     arguments to pass to the method
     * @throws IllegalArgumentException if the method, instance, or arguments are null
     * @throws Exception                if the reflective invocation fails
     */
    public static void invoke(final Method method, final Object instance, final Object... args) throws Exception {
        if (method == null) {
            throw new IllegalArgumentException("Method cannot be null.");
        }

        if (instance == null) {
            throw new IllegalArgumentException("Instance cannot be null.");
        }

        // Try to allow access to private/protected methods
        method.trySetAccessible();

        // Invoke the method with the provided arguments
        if (args != null && args.length > 0) {
            method.invoke(instance, args);
        } else {
            method.invoke(instance);
        }
    }

    public static void invoke(final Method method, final Object instance) throws Exception {
        invoke(method, instance, new Object[0]);
    }
}