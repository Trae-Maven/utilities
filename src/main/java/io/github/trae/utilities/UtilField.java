package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.lang.reflect.Field;

/**
 * Utility class for reflective {@link Field} access operations.
 *
 * <p>This class provides helper methods for safely reading and writing
 * field values using reflection while performing validation and type checking.
 */
@UtilityClass
public final class UtilField {

    /**
     * Sets the value of a field on a given instance using reflection.
     *
     * <p>The field accessibility is overridden to allow modification of
     * private or protected fields.
     *
     * @param field    the field to modify
     * @param instance the object instance containing the field
     * @param value    the new value to assign to the field
     * @throws IllegalArgumentException if the field, instance, or value are null
     * @throws Exception                if the field cannot be accessed or modified
     */
    public static void set(final Field field, final Object instance, final Object value) throws Exception {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null.");
        }

        if (instance == null) {
            throw new IllegalArgumentException("Instance cannot be null.");
        }

        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null.");
        }

        // Try to allow access to private/protected fields
        field.trySetAccessible();

        // Set the field value
        field.set(instance, value);
    }

    /**
     * Retrieves the value of a field from an instance and safely casts it to the specified type.
     *
     * <p>This method validates that the retrieved value matches the expected type before casting.
     * If the value type does not match, a detailed exception is thrown describing the mismatch.
     *
     * @param type     the expected type of the field value
     * @param field    the field to read from
     * @param instance the object instance containing the field
     * @param <T>      the expected return type
     * @return the field value cast to the requested type
     * @throws IllegalArgumentException if the field or instance are null
     * @throws IllegalStateException    if the retrieved value is not of the expected type
     * @throws Exception                if the field cannot be accessed
     */
    public static <T> T get(final Class<T> type, final Field field, final Object instance) throws Exception {
        if (field == null) {
            throw new IllegalArgumentException("Field cannot be null.");
        }

        if (instance == null) {
            throw new IllegalArgumentException("Instance cannot be null.");
        }

        final Object value = field.get(instance);

        if (!(type.isInstance(value))) {
            throw new IllegalStateException("Instance %s field %s has value type %s, expected %s.".formatted(instance.getClass().getName(), field.getName(), value == null ? "null" : value.getClass().getName(), type.getName()));
        }

        return UtilJava.cast(type, value);
    }
}