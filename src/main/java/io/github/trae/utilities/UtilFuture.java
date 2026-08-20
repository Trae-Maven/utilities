package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Terminal continuations for {@link CompletableFuture}, for callers that want to
 * react to a result rather than build a chain.
 *
 * <p>Every method here discards the future the underlying call returns, so these
 * are ends of a chain, not links in one. Anything that needs to compose further —
 * mapping a value, joining two futures, recovering from a failure — should use
 * {@link CompletableFuture} directly.</p>
 *
 * <p>The distinction between the plain and {@code Async} variants is which thread
 * runs the callback. The plain form runs it on whichever thread completed the
 * future, or on the calling thread if it has already completed; the
 * {@code Async} form always hands it to the common pool. Reach for the async
 * variant when the callback is slow, or when the completing thread is one you
 * must not block — a server's main thread, an event loop.</p>
 *
 * <p><b>Failures are silent in the {@code then} methods.</b> A future that
 * completes exceptionally simply never runs its callback, and with the returned
 * future discarded there is nothing left holding the exception. Use one of the
 * {@code whenComplete} methods anywhere a failure needs to be seen.</p>
 */
@UtilityClass
public class UtilFuture {

    /**
     * Runs a consumer with the future's value once it completes successfully.
     *
     * <p>Runs on the completing thread, or immediately on the calling thread if
     * the future has already completed. A failure is swallowed.</p>
     *
     * @param <T>               the future's value type
     * @param completableFuture the future to observe
     * @param consumer          called with the completed value
     */
    public static <T> void thenAccept(final CompletableFuture<T> completableFuture, final Consumer<? super T> consumer) {
        completableFuture.thenAccept(consumer);
    }

    /**
     * Runs a consumer with the future's value on the common pool once it
     * completes successfully.
     *
     * <p>Use when the callback is slow or the completing thread must not be
     * blocked. A failure is swallowed.</p>
     *
     * @param <T>               the future's value type
     * @param completableFuture the future to observe
     * @param consumer          called with the completed value
     */
    public static <T> void thenAcceptAsync(final CompletableFuture<T> completableFuture, final Consumer<? super T> consumer) {
        completableFuture.thenAcceptAsync(consumer);
    }

    /**
     * Runs an action once the future completes successfully, ignoring its value.
     *
     * <p>Runs on the completing thread, or immediately on the calling thread if
     * the future has already completed. A failure is swallowed.</p>
     *
     * @param <T>               the future's value type
     * @param completableFuture the future to observe
     * @param runnable          the action to run
     */
    public static <T> void thenRun(final CompletableFuture<T> completableFuture, final Runnable runnable) {
        completableFuture.thenRun(runnable);
    }

    /**
     * Runs an action on the common pool once the future completes successfully,
     * ignoring its value.
     *
     * <p>A failure is swallowed.</p>
     *
     * @param <T>               the future's value type
     * @param completableFuture the future to observe
     * @param runnable          the action to run
     */
    public static <T> void thenRunAsync(final CompletableFuture<T> completableFuture, final Runnable runnable) {
        completableFuture.thenRunAsync(runnable);
    }

    /**
     * Runs a callback when the future settles, whichever way it went.
     *
     * <p>Exactly one of the two arguments is non-null: the value on success, the
     * throwable on failure. The throwable is the raw cause where the future
     * failed directly, or a {@link java.util.concurrent.CompletionException}
     * wrapping it where the failure came from further up a chain.</p>
     *
     * @param <T>               the future's value type
     * @param completableFuture the future to observe
     * @param biConsumer        called with the value, or with the failure
     */
    public static <T> void whenComplete(final CompletableFuture<T> completableFuture, final BiConsumer<? super T, ? super Throwable> biConsumer) {
        completableFuture.whenComplete(biConsumer);
    }

    /**
     * Runs one of two callbacks when the future settles, splitting success from
     * failure.
     *
     * <p>The same as the {@link BiConsumer} form, with the null check done for
     * you — the value consumer only ever sees a successful completion, and the
     * throwable consumer only ever a failure. Exactly one of them runs.</p>
     *
     * @param <T>               the future's value type
     * @param completableFuture the future to observe
     * @param consumer          called with the value on success
     * @param throwableConsumer called with the failure on error
     */
    public static <T> void whenComplete(final CompletableFuture<T> completableFuture, final Consumer<? super T> consumer, final Consumer<Throwable> throwableConsumer) {
        completableFuture.whenComplete((value, throwable) -> {
            if (throwable != null) {
                throwableConsumer.accept(throwable);
                return;
            }

            consumer.accept(value);
        });
    }
}