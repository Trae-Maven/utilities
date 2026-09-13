package io.github.trae.utilities;

import io.github.trae.utilities.enums.HttpMethod;
import io.github.trae.utilities.exceptions.HttpException;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * HTTP utility providing dispatch (fire-and-forget) and supply (response-returning)
 * patterns with both synchronous and asynchronous variants.
 *
 * <p><b>Dispatch</b> — execute a request, handle success/failure via callbacks, no return value.</p>
 * <p><b>Supply</b> — execute a request and return the {@link HttpResponse}.</p>
 *
 * <p>The body type is chosen by the supplied {@link HttpResponse.BodyHandler} —
 * {@link HttpResponse.BodyHandlers#ofString()} for text,
 * {@link HttpResponse.BodyHandlers#ofByteArray()} for binary,
 * {@link HttpResponse.BodyHandlers#ofFile(java.nio.file.Path)} to stream to disk.</p>
 *
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Fire-and-forget async POST with JSON
 * UtilHttp.dispatchAsynchronous(HttpMethod.POST, "https://api.example.com/users", "{\"name\":\"John\"}",
 *     "application/json", null, HttpResponse.BodyHandlers.ofString(),
 *     response -> System.out.println("Created: " + response.statusCode()),
 *     Throwable::printStackTrace
 * );
 *
 * // Synchronous GET
 * HttpResponse<String> response = UtilHttp.supply(HttpMethod.GET, "https://api.example.com/users", null, null, null, HttpResponse.BodyHandlers.ofString());
 *
 * // Synchronous GET returning raw bytes
 * HttpResponse<byte[]> binary = UtilHttp.supply(HttpMethod.GET, "https://example.com/pack.zip", null, null, null, HttpResponse.BodyHandlers.ofByteArray());
 *
 * // Async supply with chaining
 * UtilHttp.supplyAsynchronous(HttpMethod.GET, "https://api.example.com/users", null, null, null, HttpResponse.BodyHandlers.ofString())
 *     .thenAccept(response -> System.out.println(response.body()));
 * }</pre>
 *
 * @since 1.0
 */
@UtilityClass
public class UtilHttp {

    @Setter
    private static Duration defaultRequestTimeout = Duration.ofSeconds(10);

    @Setter
    private static HttpClient httpClient = HttpClient.newBuilder().connectTimeout(defaultRequestTimeout).followRedirects(HttpClient.Redirect.NORMAL).build();

    /**
     * Returns {@code true} if the response status code is in the 1xx range.
     *
     * @param httpResponse the HTTP response
     * @return whether the response is informational
     */
    public static boolean isInformational(final HttpResponse<?> httpResponse) {
        return isStatusClass(httpResponse, 1);
    }

    /**
     * Returns {@code true} if the response status code is in the 2xx range.
     *
     * @param httpResponse the HTTP response
     * @return whether the response indicates success
     */
    public static boolean isSuccess(final HttpResponse<?> httpResponse) {
        return isStatusClass(httpResponse, 2);
    }

    /**
     * Returns {@code true} if the response status code is in the 3xx range.
     *
     * <p>Note that the default {@link HttpClient} follows redirects, so a 3xx
     * response usually indicates a redirect the client declined to follow,
     * such as a protocol downgrade.</p>
     *
     * @param httpResponse the HTTP response
     * @return whether the response indicates a redirect
     */
    public static boolean isRedirect(final HttpResponse<?> httpResponse) {
        return isStatusClass(httpResponse, 3);
    }

    /**
     * Returns {@code true} if the response status code is in the 4xx range.
     *
     * @param httpResponse the HTTP response
     * @return whether the response indicates a client error
     */
    public static boolean isClientError(final HttpResponse<?> httpResponse) {
        return isStatusClass(httpResponse, 4);
    }

    /**
     * Returns {@code true} if the response status code is in the 5xx range.
     *
     * @param httpResponse the HTTP response
     * @return whether the response indicates a server error
     */
    public static boolean isServerError(final HttpResponse<?> httpResponse) {
        return isStatusClass(httpResponse, 5);
    }

    /**
     * Returns {@code true} if the response status code is in the 4xx or 5xx range.
     *
     * @param httpResponse the HTTP response
     * @return whether the response indicates an error
     */
    public static boolean isError(final HttpResponse<?> httpResponse) {
        return isClientError(httpResponse) || isServerError(httpResponse);
    }

    /**
     * Returns {@code true} if the response status code matches the given code.
     *
     * @param httpResponse the HTTP response
     * @param statusCode   the status code to match
     * @return whether the status codes are equal
     */
    public static boolean isStatus(final HttpResponse<?> httpResponse, final int statusCode) {
        return httpResponse != null && httpResponse.statusCode() == statusCode;
    }

    /**
     * Returns {@code true} if the response status code falls within the given
     * inclusive range.
     *
     * @param httpResponse the HTTP response
     * @param minimum      the lowest accepted status code
     * @param maximum      the highest accepted status code
     * @return whether the status code is within range
     */
    public static boolean isStatusBetween(final HttpResponse<?> httpResponse, final int minimum, final int maximum) {
        return httpResponse != null && httpResponse.statusCode() >= minimum && httpResponse.statusCode() <= maximum;
    }

    /**
     * Returns {@code true} if the leading digit of the response status code
     * matches the given class.
     *
     * @param httpResponse the HTTP response
     * @param statusClass  the status class (1 through 5)
     * @return whether the status code belongs to that class
     */
    public static boolean isStatusClass(final HttpResponse<?> httpResponse, final int statusClass) {
        return isStatusBetween(httpResponse, statusClass * 100, (statusClass * 100) + 99);
    }

    /**
     * Dispatches a request synchronously with callbacks. Blocks the calling thread.
     *
     * @param httpMethod      the HTTP method
     * @param url             the target URL
     * @param body            the request body (may be {@code null})
     * @param contentType     the Content-Type header value (may be {@code null})
     * @param headers         header key-value pairs (may be {@code null})
     * @param bodyHandler     the response body handler
     * @param successConsumer callback receiving the response on success (may be {@code null})
     * @param errorConsumer   callback receiving the exception on failure (may be {@code null})
     * @param <T>             the response body type
     */
    public static <T> void dispatch(final HttpMethod httpMethod, final String url, final String body, final String contentType, final Map<String, String> headers, final HttpResponse.BodyHandler<T> bodyHandler, final Consumer<HttpResponse<T>> successConsumer, final Consumer<Throwable> errorConsumer) {
        validate(httpMethod, url, bodyHandler);

        try {
            final HttpResponse<T> httpResponse = httpClient.send(buildHttpRequest(httpMethod, url, body, contentType, headers), bodyHandler);

            if (successConsumer != null) {
                successConsumer.accept(httpResponse);
            }
        } catch (final Exception e) {
            if (errorConsumer != null) {
                errorConsumer.accept(e);
            }
        }
    }

    /**
     * Dispatches a request asynchronously. Returns immediately — callbacks are invoked
     * on the {@link HttpClient}'s executor. Truly fire-and-forget.
     *
     * @param httpMethod      the HTTP method
     * @param url             the target URL
     * @param body            the request body (may be {@code null})
     * @param contentType     the Content-Type header value (may be {@code null})
     * @param headers         header key-value pairs (may be {@code null})
     * @param bodyHandler     the response body handler
     * @param successConsumer callback receiving the response on success (may be {@code null})
     * @param errorConsumer   callback receiving the exception on failure (may be {@code null})
     * @param <T>             the response body type
     */
    public static <T> void dispatchAsynchronous(final HttpMethod httpMethod, final String url, final String body, final String contentType, final Map<String, String> headers, final HttpResponse.BodyHandler<T> bodyHandler, final Consumer<HttpResponse<T>> successConsumer, final Consumer<Throwable> errorConsumer) {
        validate(httpMethod, url, bodyHandler);

        httpClient.sendAsync(buildHttpRequest(httpMethod, url, body, contentType, headers), bodyHandler)
                .thenAccept(httpResponse -> {
                    if (successConsumer != null) {
                        successConsumer.accept(httpResponse);
                    }
                })
                .exceptionally(throwable -> {
                    if (errorConsumer != null) {
                        errorConsumer.accept(throwable);
                    }
                    return null;
                });
    }

    /**
     * Supplies the HTTP response synchronously. Blocks the calling thread.
     *
     * @param httpMethod  the HTTP method
     * @param url         the target URL
     * @param body        the request body (may be {@code null})
     * @param contentType the Content-Type header value (may be {@code null})
     * @param headers     header key-value pairs (may be {@code null})
     * @param bodyHandler the response body handler
     * @param <T>         the response body type
     * @return the HTTP response
     * @throws HttpException if the request fails for any reason
     */
    public static <T> HttpResponse<T> supply(final HttpMethod httpMethod, final String url, final String body, final String contentType, final Map<String, String> headers, final HttpResponse.BodyHandler<T> bodyHandler) {
        validate(httpMethod, url, bodyHandler);

        try {
            return httpClient.send(buildHttpRequest(httpMethod, url, body, contentType, headers), bodyHandler);
        } catch (final Exception e) {
            throw new HttpException("Request failed: %s %s".formatted(httpMethod.name(), url), e);
        }
    }

    /**
     * Supplies the HTTP response asynchronously.
     *
     * @param httpMethod  the HTTP method
     * @param url         the target URL
     * @param body        the request body (may be {@code null})
     * @param contentType the Content-Type header value (may be {@code null})
     * @param headers     header key-value pairs (may be {@code null})
     * @param bodyHandler the response body handler
     * @param <T>         the response body type
     * @return a {@link CompletableFuture} that completes with the HTTP response
     */
    public static <T> CompletableFuture<HttpResponse<T>> supplyAsynchronous(final HttpMethod httpMethod, final String url, final String body, final String contentType, final Map<String, String> headers, final HttpResponse.BodyHandler<T> bodyHandler) {
        validate(httpMethod, url, bodyHandler);

        return httpClient.sendAsync(buildHttpRequest(httpMethod, url, body, contentType, headers), bodyHandler);
    }

    private static void validate(final HttpMethod httpMethod, final String url, final HttpResponse.BodyHandler<?> bodyHandler) {
        if (httpClient == null) {
            throw new IllegalStateException("Http Client cannot be null.");
        }

        if (httpMethod == null) {
            throw new IllegalArgumentException("Http Method cannot be null.");
        }

        if (UtilString.isEmpty(url)) {
            throw new IllegalArgumentException("Url cannot be empty or null.");
        }

        if (bodyHandler == null) {
            throw new IllegalArgumentException("Body Handler cannot be null.");
        }
    }

    private static HttpRequest buildHttpRequest(final HttpMethod httpMethod, final String url, final String body, final String contentType, final Map<String, String> headers) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(defaultRequestTimeout);

        if (!(UtilString.isEmpty(contentType))) {
            builder.header("Content-Type", contentType);
        }

        if (headers != null) {
            headers.forEach(builder::header);
        }

        final HttpRequest.BodyPublisher bodyPublisher = body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody();

        switch (httpMethod) {
            case GET -> builder.GET();
            case POST -> builder.POST(bodyPublisher);
            case PUT -> builder.PUT(bodyPublisher);
            case DELETE -> builder.DELETE();
            case PATCH -> builder.method("PATCH", bodyPublisher);
            case HEAD -> builder.method("HEAD", bodyPublisher);
            case OPTIONS -> builder.method("OPTIONS", bodyPublisher);
        }

        return builder.build();
    }
}