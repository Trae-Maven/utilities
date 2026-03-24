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
 * <p>Usage examples:</p>
 * <pre>{@code
 * // Fire-and-forget async POST
 * UtilHttp.dispatchAsynchronous(HttpMethod.POST, "https://api.example.com/users", "{\"name\":\"John\"}",
 *     null,
 *     response -> System.out.println("Created: " + response.statusCode()),
 *     Throwable::printStackTrace
 * );
 *
 * // Synchronous GET
 * HttpResponse<String> response = UtilHttp.supply(HttpMethod.GET, "https://api.example.com/users", null, null);
 *
 * // Async supply with chaining
 * UtilHttp.supplyAsynchronous(HttpMethod.GET, "https://api.example.com/users", null, null)
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
     * Returns {@code true} if the response status code is in the 2xx range.
     *
     * @param httpResponse the HTTP response
     * @return whether the response indicates success
     */
    public static boolean isSuccess(final HttpResponse<?> httpResponse) {
        return httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300;
    }

    /**
     * Dispatches a request synchronously with callbacks. Blocks the calling thread.
     *
     * @param httpMethod      the HTTP method
     * @param url             the target URL
     * @param body            the request body (may be {@code null})
     * @param headers         header key-value pairs (may be {@code null})
     * @param successConsumer callback receiving the response on success (may be {@code null})
     * @param errorConsumer   callback receiving the exception on failure (may be {@code null})
     */
    public static void dispatch(final HttpMethod httpMethod, final String url, final String body, final Map<String, String> headers, final Consumer<HttpResponse<String>> successConsumer, final Consumer<Throwable> errorConsumer) {
        validate(httpMethod, url);

        try {
            final HttpResponse<String> httpResponse = httpClient.send(buildHttpRequest(httpMethod, url, body, headers), HttpResponse.BodyHandlers.ofString());

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
     * @param headers         header key-value pairs (may be {@code null})
     * @param successConsumer callback receiving the response on success (may be {@code null})
     * @param errorConsumer   callback receiving the exception on failure (may be {@code null})
     */
    public static void dispatchAsynchronous(final HttpMethod httpMethod, final String url, final String body, final Map<String, String> headers, final Consumer<HttpResponse<String>> successConsumer, final Consumer<Throwable> errorConsumer) {
        validate(httpMethod, url);

        httpClient.sendAsync(buildHttpRequest(httpMethod, url, body, headers), HttpResponse.BodyHandlers.ofString())
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
     * @param httpMethod the HTTP method
     * @param url        the target URL
     * @param body       the request body (may be {@code null})
     * @param headers    header key-value pairs (may be {@code null})
     * @return the HTTP response
     * @throws HttpException if the request fails for any reason
     */
    public static HttpResponse<String> supply(final HttpMethod httpMethod, final String url, final String body, final Map<String, String> headers) {
        validate(httpMethod, url);

        try {
            return httpClient.send(buildHttpRequest(httpMethod, url, body, headers), HttpResponse.BodyHandlers.ofString());
        } catch (final Exception e) {
            throw new HttpException("Request failed: %s %s".formatted(httpMethod.name(), url), e);
        }
    }

    /**
     * Supplies the HTTP response asynchronously.
     *
     * @param httpMethod the HTTP method
     * @param url        the target URL
     * @param body       the request body (may be {@code null})
     * @param headers    header key-value pairs (may be {@code null})
     * @return a {@link CompletableFuture} that completes with the HTTP response
     */
    public static CompletableFuture<HttpResponse<String>> supplyAsynchronous(final HttpMethod httpMethod, final String url, final String body, final Map<String, String> headers) {
        validate(httpMethod, url);

        return httpClient.sendAsync(buildHttpRequest(httpMethod, url, body, headers), HttpResponse.BodyHandlers.ofString());
    }

    private static void validate(final HttpMethod httpMethod, final String url) {
        if (httpClient == null) {
            throw new IllegalStateException("Http Client cannot be null.");
        }

        if (httpMethod == null) {
            throw new IllegalArgumentException("Http Method cannot be null.");
        }

        if (UtilString.isEmpty(url)) {
            throw new IllegalArgumentException("Url cannot be empty or null.");
        }
    }

    private static HttpRequest buildHttpRequest(final HttpMethod httpMethod, final String url, final String body, final Map<String, String> headers) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder().uri(URI.create(url)).timeout(defaultRequestTimeout);

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