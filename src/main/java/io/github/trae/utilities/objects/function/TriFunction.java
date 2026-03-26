package io.github.trae.utilities.objects.function;

/**
 * Represents a function that accepts three arguments and produces a result.
 *
 * @param <A>      the type of the first argument
 * @param <B>      the type of the second argument
 * @param <C>      the type of the third argument
 * @param <Return> the type of the result
 */
@FunctionalInterface
public interface TriFunction<A, B, C, Return> {

    Return apply(final A a, final B b, final C c);
}