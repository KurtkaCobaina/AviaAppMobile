

package com.example.aviaappmobile;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;
public class HistoryActivityTest {

    @Test
    public void testDateTimePattern_ValidFormat() {
        Pattern pattern = Utils.getDateTimePattern();

        assertTrue(pattern.matcher("2025-12-31 23:59").matches());
        assertTrue(pattern.matcher("2024-01-01 00:00").matches());
        assertTrue(pattern.matcher("1999-05-15 12:30").matches());
    }



    @Test
    public void testIsValidDate_ValidDates() {
        assertTrue(Utils.isValidDate("2025-12-31 23:59"));
        assertTrue(Utils.isValidDate("2024-01-01 00:00"));
        assertTrue(Utils.isValidDate("1999-05-15 12:30"));
    }

    @Test
    public void testIsValidDate_InvalidDates() {
        assertFalse(Utils.isValidDate("2025-13-01 00:00")); // Несуществующий месяц
        assertFalse(Utils.isValidDate("2025-02-30 00:00")); // Нет 30 февраля
        assertFalse(Utils.isValidDate("2025-12-31 24:60")); // Неверное время
        assertFalse(Utils.isValidDate("not-a-date"));       // Не дата вообще
    }
}