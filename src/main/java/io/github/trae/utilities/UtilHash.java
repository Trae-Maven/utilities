package io.github.trae.utilities;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class UtilHash {

    public static byte[] hashToBytes(final String algorithm, final byte[] bytes) {
        try {
            return MessageDigest.getInstance(algorithm).digest(bytes);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Unsupported algorithm: " + algorithm, e);
        }
    }

    public static byte[] hashToBytes(final String algorithm, final String string) {
        return hashToBytes(algorithm, string.getBytes(StandardCharsets.UTF_8));
    }

    public static String hashToString(final String algorithm, final byte[] bytes) {
        return toHex(hashToBytes(algorithm, bytes));
    }

    public static String hashToString(final String algorithm, final String string) {
        return hashToString(algorithm, string.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean verify(final String algorithm, final byte[] plainBytes, final byte[] storedBytes) {
        return MessageDigest.isEqual(storedBytes, hashToBytes(algorithm, plainBytes));
    }

    public static boolean verify(final String algorithm, final String plainString, final String storedString) {
        final byte[] storedBytes = fromHex(storedString);
        final byte[] computed = hashToBytes(algorithm, plainString);

        return MessageDigest.isEqual(storedBytes, computed);
    }

    public static String hmac(final String algorithm, final String key, final String input) {
        try {
            final Mac mac = Mac.getInstance(algorithm);

            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm));

            final byte[] hashBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));

            return UtilBase64.encodeToString(hashBytes);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to compute HMAC using %s".formatted(algorithm), e);
        }
    }

    private static String toHex(final byte[] bytes) {
        final StringBuilder sb = new StringBuilder(bytes.length * 2);

        for (final byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }

        return sb.toString();
    }

    private static byte[] fromHex(final String hex) {
        final int len = hex.length();
        if (len % 2 != 0) {
            throw new IllegalArgumentException("Invalid hex string");
        }

        final byte[] out = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            out[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
        }

        return out;
    }
}