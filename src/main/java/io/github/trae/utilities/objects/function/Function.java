package io.github.trae.utilities.objects.function;

/**
 * Represents a function that accepts one argument and produces a result.
 *
 * @param <A>      the type of the first argument
 * @param <Return> the type of the result
 */
@FunctionalInterface
public interface Function<A, Return> {

    Return apply(final A a);
}