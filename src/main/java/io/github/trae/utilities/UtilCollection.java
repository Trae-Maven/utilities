package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Utility methods for working with collections.
 */
@UtilityClass
public class UtilCollection {

    /**
     * Searches a collection for an element, prioritising exact matches over partial matches.
     *
     * <p>Resolution order:</p>
     * <ol>
     *   <li>Elements failing {@code typePredicate} are skipped entirely.</li>
     *   <li>Exact match ({@code equalsPredicate}) — returned immediately.</li>
     *   <li>Single partial match ({@code containsPredicate}) — returned after the full scan.</li>
     *   <li>Multiple partial matches — forwarded to {@code listConsumer}; an optional summary
     *       message (up to 100 names) is sent to {@code messageConsumer} when {@code inform} is {@code true}.</li>
     *   <li>No matches — an optional "no matches" message is sent to {@code messageConsumer} when {@code inform} is {@code true}.</li>
     * </ol>
     *
     * @param collection        the collection to search
     * @param typePredicate     pre-filter applied before exact and partial matching, or {@code null} to skip
     * @param equalsPredicate   exact-match predicate, or {@code null} to skip
     * @param containsPredicate partial-match predicate, or {@code null} to skip
     * @param listConsumer      receives the partial-match list after the full scan, or {@code null} to skip
     * @param messageConsumer   receives a formatted result message when {@code inform} is {@code true}, or {@code null} to skip
     * @param colorFunction     applies colour formatting to highlighted segments of the result message
     * @param resultFunction    maps a matched element to its display name for use in the result message
     * @param input             the raw input string shown in the "no matches" message
     * @param inform            whether to produce a result message on failure or ambiguity
     * @param <Type>            the element type
     * @return an {@link Optional} containing the matched element, or empty if zero or multiple matches were found
     */
    public static <Type> Optional<Type> search(final Class<Type> clazz, final Collection<? extends Type> collection, final Predicate<Type> typePredicate, final Predicate<Type> equalsPredicate, final Predicate<Type> containsPredicate, final Consumer<List<Type>> listConsumer, final Consumer<String> messageConsumer, final Function<String, String> colorFunction, final Function<Type, String> resultFunction, final String input, final boolean inform) {
        final List<Type> list = new ArrayList<>();

        for (final Type object : collection) {
            if (typePredicate != null && !(typePredicate.test(object))) {
                continue;
            }

            if (equalsPredicate != null && equalsPredicate.test(object)) {
                return Optional.of(UtilJava.cast(clazz, object));
            }

            if (containsPredicate != null && containsPredicate.test(object)) {
                list.add(UtilJava.cast(clazz, object));
            }
        }

        if (listConsumer != null) {
            listConsumer.accept(list);
        }

        if (list.size() == 1) {
            return Optional.of(UtilJava.cast(clazz, list.getFirst()));
        }

        if (inform) {
            String message = "No matches found [%s].".formatted(colorFunction.apply(input));

            if (!(list.isEmpty())) {
                final List<String> results = UtilJava.createCollection(new ArrayList<>(), resultList -> {
                    for (int i = 0; i < list.size(); i++) {
                        if (i == 100) {
                            break;
                        }

                        resultList.add(colorFunction.apply(resultFunction.apply(list.get(i))));
                    }
                });

                message = "%s matches found [%s].".formatted(colorFunction.apply(String.valueOf(list.size())), String.join("<gray>, </gray>", results));
            }

            if (messageConsumer != null) {
                messageConsumer.accept(message);
            }
        }

        return Optional.empty();
    }
}