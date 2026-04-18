package io.github.trae.utilities.enums;

import io.github.trae.utilities.UtilString;
import io.github.trae.utilities.enums.interfaces.ITimeUnit;
import lombok.Getter;

/**
 * Represents a unit of time with its equivalent duration in milliseconds.
 *
 * <p>Provides best-fit resolution and human-readable formatting of millisecond durations.
 */
@Getter
public enum TimeUnit implements ITimeUnit {

    MILLISECONDS(1L),
    SECONDS(1_000L),
    MINUTES(60_000L),
    HOURS(3_600_000L),
    DAYS(86_400_000L),
    WEEKS(604_800_000L),
    MONTHS(2_629_800_000L),
    YEARS(31_557_600_000L);

    private final String name;
    private final long duration;

    TimeUnit(final long duration) {
        this.name = UtilString.clean(this.name());
        this.duration = duration;
    }

    /**
     * Returns the largest {@link TimeUnit} whose duration fits into {@code duration}.
     *
     * @param duration a duration in milliseconds
     * @return the best-fit {@link TimeUnit}
     */
    public static TimeUnit getByDuration(final long duration) {
        TimeUnit bestTimeUnit = MILLISECONDS;

        for (final TimeUnit timeUnit : values()) {
            if (duration >= timeUnit.getDuration()) {
                bestTimeUnit = timeUnit;
            }
        }

        return bestTimeUnit;
    }

    /**
     * Formats a millisecond duration into a human-readable string.
     *
     * @param duration a duration in milliseconds
     * @param trim     decimal places to include; {@code 0} produces whole numbers
     * @return a formatted string such as {@code "5 minutes"} or {@code "1 hour"}
     */
    public static String format(final long duration, final int trim) {
        final TimeUnit timeUnit = getByDuration(duration);

        final double value = (double) duration / timeUnit.getDuration();
        final long rounded = Math.round(value);

        final String label = timeUnit.label(trim == 0 ? rounded : value);

        return trim <= 0 ? "%s %s".formatted(rounded, label) : ("%." + trim + "f %s").formatted(value, label);
    }

    /**
     * Returns the singular or plural label for this unit based on {@code value}.
     *
     * @param value the numeric value to test for singularity
     * @return {@code "minute"} for {@code 1.0}, {@code "minutes"} otherwise
     */
    @Override
    public String label(final double value) {
        final String label = this.name().toLowerCase();

        return value == 1.0D ? label.substring(0, label.length() - 1) : label;
    }
}