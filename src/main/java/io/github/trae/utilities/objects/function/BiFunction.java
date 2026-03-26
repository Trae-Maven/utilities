package io.github.trae.utilities.objects.function;

/**
 * Represents a function that accepts two arguments and produces a result.
 *
 * @param <A>      the type of the first argument
 * @param <B>      the type of the second argument
 * @param <Return> the type of the result
 */
@FunctionalInterface
public interface BiFunction<A, B, Return> {

    Return apply(final A a, final B b);
}