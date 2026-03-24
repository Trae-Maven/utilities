package io.github.trae.utilities.exceptions;

import lombok.Getter;

/**
 * Unchecked exception thrown by {@link io.github.trae.utilities.UtilHttp} when an HTTP request
 * fails, wrapping the underlying cause for propagation without requiring checked exception handling.
 *
 * @since 1.0
 */
@Getter
public class HttpException extends RuntimeException {

    public HttpException() {
        super();
    }

    public HttpException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public HttpException(final String message) {
        super(message);
    }

    public HttpException(final Throwable cause) {
        super(cause);
    }
}