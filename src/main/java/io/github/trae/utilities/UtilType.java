package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.util.function.Predicate;

/**
 * Utility class providing methods for validating and matching character types within strings.
 */
@UtilityClass
public class UtilType {

    /**
     * Checks whether all characters within the specified input match the provided predicate.
     *
     * @param input     The input to check.
     * @param predicate The predicate used to test each character.
     * @return {@code true} if the input is not empty and all characters match the predicate; otherwise {@code false}.
     */
    public static boolean isAllMatch(final String input, final Predicate<Character> predicate) {
        return !UtilString.isEmpty(input) && input.chars().allMatch(value -> predicate.test((char) value));
    }

    /**
     * Checks whether any character within the specified input matches the provided predicate.
     *
     * @param input     The input to check.
     * @param predicate The predicate used to test each character.
     * @return {@code true} if the input is not empty and at least one character matches the predicate; otherwise {@code false}.
     */
    public static boolean isAnyMatch(final String input, final Predicate<Character> predicate) {
        return !UtilString.isEmpty(input) && input.chars().anyMatch(value -> predicate.test((char) value));
    }

    /**
     * Checks whether the specified input contains only alphabetic characters.
     *
     * @param input The input to check.
     * @return {@code true} if the input is not empty and contains only alphabetic characters; otherwise {@code false}.
     */
    public static boolean isAlphabetic(final String input) {
        return isAllMatch(input, Character::isLetter);
    }

    /**
     * Checks whether the specified input contains only numeric characters.
     *
     * @param input The input to check.
     * @return {@code true} if the input is not empty and contains only numeric characters; otherwise {@code false}.
     */
    public static boolean isNumeric(final String input) {
        return isAllMatch(input, Character::isDigit);
    }
}