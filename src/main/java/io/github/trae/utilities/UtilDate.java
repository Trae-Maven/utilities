package io.github.trae.utilities;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Formats epoch millisecond timestamps, such as those from {@link System#currentTimeMillis()},
 * into human-readable date and time strings.
 * <p>
 * Every formatter is resolved against the system default time zone. {@link DateTimeFormatter} is
 * immutable and thread-safe, so the shared formatters are safe to use from any thread.
 */
@UtilityClass
public class UtilDate {

    /**
     * The separator placed between date parts when none is given.
     */
    private static final String DEFAULT_DATE_SEPARATOR = "/";

    /**
     * Formats a time on the 24-hour clock, e.g. {@code 18:05:42}.
     */
    private static final DateTimeFormatter TWENTY_FOUR_HOUR_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * Formats a time on the 12-hour clock with its period, e.g. {@code 06:05:42 PM}. Pinned to
     * {@link Locale#ENGLISH} so the period always renders as {@code AM} or {@code PM}.
     */
    private static final DateTimeFormatter TWELVE_HOUR_TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss a", Locale.ENGLISH).withZone(ZoneId.systemDefault());

    /**
     * Date formatters built so far, keyed by their separator, so each separator's pattern is only
     * parsed once.
     */
    private static final Map<String, DateTimeFormatter> DATE_FORMATTER_MAP = new ConcurrentHashMap<>();

    /**
     * Formats a timestamp as its date and time.
     *
     * @param systemTime the timestamp in epoch milliseconds
     * @param separator  the text placed between the day, month and year
     * @param twelveHour whether to use the 12-hour clock with {@code AM}/{@code PM} rather than the
     *                   24-hour clock
     * @return the formatted date and time, e.g. {@code 29-09-2026 06:05:42 PM}
     */
    public static String formatDateAndTime(final long systemTime, final String separator, final boolean twelveHour) {
        return "%s %s".formatted(formatDate(systemTime, separator), formatTime(systemTime, twelveHour));
    }

    /**
     * Formats a timestamp as its date and time, separating the date parts with {@code /} and using
     * the 24-hour clock.
     *
     * @param systemTime the timestamp in epoch milliseconds
     * @return the formatted date and time, e.g. {@code 29/09/2026 18:05:42}
     */
    public static String formatDateAndTime(final long systemTime) {
        return formatDateAndTime(systemTime, DEFAULT_DATE_SEPARATOR, false);
    }

    /**
     * Formats a timestamp as its date only.
     *
     * @param systemTime the timestamp in epoch milliseconds
     * @param separator  the text placed between the day, month and year
     * @return the formatted date, e.g. {@code 29.09.2026}
     */
    public static String formatDate(final long systemTime, final String separator) {
        return DATE_FORMATTER_MAP.computeIfAbsent(separator, UtilDate::createDateFormatter).format(Instant.ofEpochMilli(systemTime));
    }

    /**
     * Formats a timestamp as its date only, separating the parts with {@code /}.
     *
     * @param systemTime the timestamp in epoch milliseconds
     * @return the formatted date, e.g. {@code 29/09/2026}
     */
    public static String formatDate(final long systemTime) {
        return formatDate(systemTime, DEFAULT_DATE_SEPARATOR);
    }

    /**
     * Formats a timestamp as its time only.
     *
     * @param systemTime the timestamp in epoch milliseconds
     * @param twelveHour whether to use the 12-hour clock with {@code AM}/{@code PM} rather than the
     *                   24-hour clock
     * @return the formatted time, e.g. {@code 06:05:42 PM} or {@code 18:05:42}
     */
    public static String formatTime(final long systemTime, final boolean twelveHour) {
        return (twelveHour ? TWELVE_HOUR_TIME_FORMATTER : TWENTY_FOUR_HOUR_TIME_FORMATTER).format(Instant.ofEpochMilli(systemTime));
    }

    /**
     * Formats a timestamp as its time only, on the 24-hour clock.
     *
     * @param systemTime the timestamp in epoch milliseconds
     * @return the formatted time, e.g. {@code 18:05:42}
     */
    public static String formatTime(final long systemTime) {
        return formatTime(systemTime, false);
    }

    /**
     * Builds a day, month, year formatter joined by the given separator.
     * <p>
     * The separator is quoted as a literal so letters in it are never read as pattern symbols. An
     * empty separator is left unquoted, since {@code ''} in a pattern means a literal apostrophe.
     *
     * @param separator the text placed between the day, month and year
     * @return the date formatter
     */
    private static DateTimeFormatter createDateFormatter(final String separator) {
        final String literal = separator.isEmpty() ? separator : "'%s'".formatted(separator.replace("'", "''"));

        return DateTimeFormatter.ofPattern("dd%sMM%syyyy".formatted(literal, literal)).withZone(ZoneId.systemDefault());
    }
}