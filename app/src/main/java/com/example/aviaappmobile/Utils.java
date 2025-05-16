package com.example.aviaappmobile;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

public class Utils {

    // Паттерн для формата даты: yyyy-MM-dd HH:mm
    private static final Pattern DATE_TIME_PATTERN =
            Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}$");

    public static Pattern getDateTimePattern() {
        return DATE_TIME_PATTERN;
    }

    // Проверка, что дата может быть распарсена
    public static boolean isValidDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            sdf.setLenient(false);
            sdf.parse(dateStr);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // Проверка только букв и пробелов
    public static boolean isOnlyLetters(String text) {
        return text != null && text.matches("[a-zA-Zа-яА-ЯёЁ\\s]+");
    }
}