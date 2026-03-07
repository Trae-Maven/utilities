package io.github.trae.utilities;

import java.lang.reflect.Method;

/**
 * Utility class for reflective {@link Method} operations.
 *
 * <p>This class simplifies method invocation using reflection while performing
 * basic validation and accessibility handling.
 */
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

        if (args == null) {
            throw new IllegalArgumentException("Instance cannot be null.");
        }

        // Allow access to private/protected methods
        method.setAccessible(true);

        // Invoke the method with the provided arguments
        method.invoke(instance, args);
    }
}