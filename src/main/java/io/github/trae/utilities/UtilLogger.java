package io.github.trae.utilities;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import java.util.logging.Logger;

/**
 * Static utility wrapper around {@link Logger} providing a single
 * configurable logger instance shared across the application.
 *
 * <p>Defaults to {@link Logger#getGlobal()}. Call {@link #setLogger(Logger)}
 * to replace with a custom logger (e.g. the plugin's logger) during
 * initialization.</p>
 */
@UtilityClass
public class UtilLogger {

    /**
     * The shared logger instance. Defaults to {@link Logger#getGlobal()}.
     */
    @Getter
    @Setter
    private static Logger logger = Logger.getGlobal();

    /**
     * Logs a {@link java.util.logging.Level#SEVERE SEVERE} message.
     *
     * @param message the message to log
     */
    public static void severe(final String message) {
        getLogger().severe(message);
    }

    /**
     * Logs a {@link java.util.logging.Level#WARNING WARNING} message.
     *
     * @param message the message to log
     */
    public static void warning(final String message) {
        getLogger().warning(message);
    }

    /**
     * Logs a {@link java.util.logging.Level#INFO INFO} message.
     *
     * @param message the message to log
     */
    public static void info(final String message) {
        getLogger().info(message);
    }

    /**
     * Logs a {@link java.util.logging.Level#CONFIG CONFIG} message.
     *
     * @param message the message to log
     */
    public static void config(final String message) {
        getLogger().config(message);
    }

    /**
     * Logs a {@link java.util.logging.Level#FINE FINE} message.
     *
     * @param message the message to log
     */
    public static void fine(final String message) {
        getLogger().fine(message);
    }

    /**
     * Logs a {@link java.util.logging.Level#FINER FINER} message.
     *
     * @param message the message to log
     */
    public static void finer(final String message) {
        getLogger().finer(message);
    }

    /**
     * Logs a {@link java.util.logging.Level#FINEST FINEST} message.
     *
     * @param message the message to log
     */
    public static void finest(final String message) {
        getLogger().finest(message);
    }
}