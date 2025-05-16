package com.example.aviaappmobile;

import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class RegistrationActivityTest {

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$");

    @Before
    public void setUp() {
        // Можно оставить пустым
    }



    // Упрощённая копия Patterns.EMAIL_ADDRESS для тестирования
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    // === Тесты на валидацию полей ===

    @Test
    public void testValidName() {
        assertTrue(NAME_PATTERN.matcher("Ivan").matches());
        assertTrue(NAME_PATTERN.matcher("Иван").matches());
        assertFalse(NAME_PATTERN.matcher("Ivan123").matches());
        assertFalse(NAME_PATTERN.matcher("Ivan Ivanov").matches());
        assertFalse(NAME_PATTERN.matcher("").matches());
    }

    @Test
    public void testValidEmailFormat() {
        assertTrue(EMAIL_PATTERN.matcher("test@example.com").matches());
        assertTrue(EMAIL_PATTERN.matcher("user.name+tag@sub.domain.com").matches());
        assertFalse(EMAIL_PATTERN.matcher("testexample.com").matches());
        assertFalse(EMAIL_PATTERN.matcher("test@com").matches());
        assertFalse(EMAIL_PATTERN.matcher("test").matches());
    }

    @Test
    public void testValidPhoneFormat() {
        assertTrue(PHONE_PATTERN.matcher("+79123456789").matches());
        assertFalse(PHONE_PATTERN.matcher("89123456789").matches());
        assertFalse(PHONE_PATTERN.matcher("+712345678").matches());
        assertFalse(PHONE_PATTERN.matcher("+712345678901").matches());
        assertFalse(PHONE_PATTERN.matcher("").matches());
    }

    @Test
    public void testParseDate_ValidFormat() throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date validDate = sdf.parse("2000-01-01");
        assertNotNull(validDate);
    }

    @Test
    public void testParseDate_InvalidFormat() {
        assertNull(parseDate("01-01-2000"));
        assertNull(parseDate("invalid_date"));
    }

    @Test
    public void testIsAdult() {
        assertTrue(isAdult(parseDate("2000-01-01")));
        assertFalse(isAdult(parseDate("2020-01-01")));
    }
    @Test
    public void testPasswordLength() {
        StringBuilder longPassword = new StringBuilder();
        for (int i = 0; i < 129; i++) longPassword.append("a");
        assertTrue(isValidPassword("password123"));
        assertFalse(isValidPassword(longPassword.toString()));
    }

    private boolean isValidPassword(String password) {
        return password != null && password.length() <= 128;
    }
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            sdf.setLenient(false);
            return sdf.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAdult(Date dob) {
        if (dob == null) return false;
        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(dob);
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age >= 18;
    }
}