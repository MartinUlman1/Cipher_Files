package cz.ntt.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

@UtilityClass
public class DateUtils {
    /**
     *
     * @param date Date entered
     * @return It checks the date if it is written in the correct format, otherwise it throws false
     */
    public static boolean isValidDate(String date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withResolverStyle(ResolverStyle.STRICT);
        try {
            dateTimeFormatter.parse(date);
        } catch (DateTimeParseException dateTimeParseException) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param date Date entered
     * @return Modifies the date to the correct format
     */
    public static String getFormattedDateAsString(LocalDate date) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withResolverStyle(ResolverStyle.STRICT);
        return date.format(dateTimeFormatter);
    }

    /**
     *
     * @param date Date entered
     * @param dateFormat
     * @return Passes date as String
     */
    public static LocalDateTime getDateString(String date, String dateFormat) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(dateFormat).withResolverStyle(ResolverStyle.SMART);
        return LocalDateTime.parse(date, dateTimeFormatter);

    }

}
