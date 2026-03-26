package io.github.trae.utilities;

import com.google.common.flogger.FluentLogger;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * Static utility wrapper around {@link FluentLogger} providing a single
 * configurable logger instance shared across the application.
 *
 * <p>Defaults to {@link FluentLogger#forEnclosingClass()}. Call
 * {@link #setLogger(FluentLogger)} to replace with a custom logger
 * during initialization.</p>
 */
@UtilityClass
public class UtilLogger {

    /**
     * The shared logger instance. Defaults to {@link FluentLogger#forEnclosingClass()}.
     */
    @Getter
    @Setter
    private static FluentLogger logger = FluentLogger.forEnclosingClass();

    /**
     * Logs a {@link java.util.logging.Level#SEVERE SEVERE} message.
     *
     * @param message the message to log
     */
    public static void severe(final String message) {
        getLogger().atSevere().log("%s", message);
    }

    /**
     * Logs a {@link java.util.logging.Level#WARNING WARNING} message.
     *
     * @param message the message to log
     */
    public static void warning(final String message) {
        getLogger().atWarning().log("%s", message);
    }

    /**
     * Logs a {@link java.util.logging.Level#INFO INFO} message.
     *
     * @param message the message to log
     */
    public static void info(final String message) {
        getLogger().atInfo().log("%s", message);
    }

    /**
     * Logs a {@link java.util.logging.Level#CONFIG CONFIG} message.
     *
     * @param message the message to log
     */
    public static void config(final String message) {
        getLogger().atConfig().log("%s", message);
    }

    /**
     * Logs a {@link java.util.logging.Level#FINE FINE} message.
     *
     * @param message the message to log
     */
    public static void fine(final String message) {
        getLogger().atFine().log("%s", message);
    }

    /**
     * Logs a {@link java.util.logging.Level#FINER FINER} message.
     *
     * @param message the message to log
     */
    public static void finer(final String message) {
        getLogger().atFiner().log("%s", message);
    }

    /**
     * Logs a {@link java.util.logging.Level#FINEST FINEST} message.
     *
     * @param message the message to log
     */
    public static void finest(final String message) {
        getLogger().atFinest().log("%s", message);
    }
}