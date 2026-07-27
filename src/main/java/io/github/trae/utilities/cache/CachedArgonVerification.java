package io.github.trae.utilities.cache;

/**
 * A cached successful verification and the point at which it expires.
 *
 * @param digestKey the keyed digest of the credential that verified.
 * @param expiresAt the epoch millisecond at which this entry stops being valid.
 */
public record CachedArgonVerification(String digestKey, long expiresAt) {
}