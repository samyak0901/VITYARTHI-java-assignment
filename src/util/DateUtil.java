package util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for parsing, formatting, and calculating dates.
 * Demonstrates modern Java 8+ java.time package usage.
 */
public final class DateUtil {
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DISPLAY_DATE_FORMAT = "dd MMM yyyy";
    public static final String DISPLAY_DATETIME_FORMAT = "dd MMM yyyy, hh:mm a";

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATE_FORMAT);
    private static final DateTimeFormatter DISPLAY_DT_FORMATTER = DateTimeFormatter.ofPattern(DISPLAY_DATETIME_FORMAT);
    private static final DateTimeFormatter ISO_DT_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private DateUtil() {
        // Prevent instantiation
    }

    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(DISPLAY_FORMATTER);
    }

    public static String formatDateIso(LocalDate date) {
        if (date == null) return "";
        return date.format(ISO_FORMATTER);
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return "N/A";
        return dateTime.format(DISPLAY_DT_FORMATTER);
    }

    public static String formatDateTimeIso(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return dateTime.format(ISO_DT_FORMATTER);
    }

    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateStr.trim();
        try {
            return LocalDate.parse(trimmed, ISO_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(trimmed, DISPLAY_FORMATTER);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateTimeStr.trim(), ISO_DT_FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateTimeStr.trim(), DISPLAY_DT_FORMATTER);
            } catch (DateTimeParseException ex) {
                return LocalDateTime.now();
            }
        }
    }

    public static boolean isValidDate(String dateStr) {
        return parseDate(dateStr) != null;
    }

    public static long daysUntil(LocalDate date) {
        if (date == null) return 0;
        return ChronoUnit.DAYS.between(LocalDate.now(), date);
    }
}
