package io.github.trae.utilities;

/**
 * String utility methods.
 */
public final class UtilString {

    /**
     * Check whether a string is null, empty, or contains only whitespace.
     *
     * @param string the string to check
     * @return true if the string is null, empty, or blank
     */
    public static boolean isEmpty(final String string) {
        return string == null || string.isBlank();
    }
}