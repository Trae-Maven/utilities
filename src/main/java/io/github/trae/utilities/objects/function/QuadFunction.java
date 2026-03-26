package io.github.trae.utilities.objects.function;

/**
 * Represents a function that accepts four arguments and produces a result.
 *
 * @param <A>      the type of the first argument
 * @param <B>      the type of the second argument
 * @param <C>      the type of the third argument
 * @param <D>      the type of the fourth argument
 * @param <Return> the type of the result
 */
@FunctionalInterface
public interface QuadFunction<A, B, C, D, Return> {

    Return apply(final A a, final B b, final C c, final D d);
}