package com.example.aviaappmobile;

import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class PaymentActivityTest {

    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{4} \\d{4} \\d{4} \\d{4}$");
    private static final Pattern EXPIRY_DATE_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])\\/(?:[2-9][0-9])$");
    private static final Pattern CVC_PATTERN = Pattern.compile("^\\d{3}$");

    @Before
    public void setUp() {
        // Initialization if needed
    }

    @Test
    public void testValidCardNumberFormat() {
        assertTrue(CARD_NUMBER_PATTERN.matcher("1234 5678 9012 3456").matches());
        assertFalse(CARD_NUMBER_PATTERN.matcher("1234567890123456").matches()); // No spaces
        assertFalse(CARD_NUMBER_PATTERN.matcher("1234 5678 9012 345").matches()); // Only 15 digits
        assertFalse(CARD_NUMBER_PATTERN.matcher("1234 5678 9012 34567").matches()); // 17 digits
        assertFalse(CARD_NUMBER_PATTERN.matcher("abcd efgh ijkl mnop").matches()); // Letters
        assertFalse(CARD_NUMBER_PATTERN.matcher("").matches());
    }

    @Test
    public void testValidExpiryDateFormat() {
        assertTrue(EXPIRY_DATE_PATTERN.matcher("12/25").matches()); // OK
        assertTrue(EXPIRY_DATE_PATTERN.matcher("01/99").matches()); // OK

        assertFalse(EXPIRY_DATE_PATTERN.matcher("13/25").matches()); // Bad month
        assertFalse(EXPIRY_DATE_PATTERN.matcher("00/25").matches()); // Bad month
        assertFalse(EXPIRY_DATE_PATTERN.matcher("12/19").matches()); // Bad year
        assertFalse(EXPIRY_DATE_PATTERN.matcher("1/25").matches());  // No leading zero
        assertFalse(EXPIRY_DATE_PATTERN.matcher("1225").matches());  // Missing slash
        assertFalse(EXPIRY_DATE_PATTERN.matcher("").matches());      // Empty
    }

    @Test
    public void testValidCvcFormat() {
        assertTrue(CVC_PATTERN.matcher("123").matches());
        assertFalse(CVC_PATTERN.matcher("12").matches());
        assertFalse(CVC_PATTERN.matcher("1234").matches());
        assertFalse(CVC_PATTERN.matcher("abc").matches());
        assertFalse(CVC_PATTERN.matcher("").matches());
    }

    @Test
    public void testIsCardExpiryValid_NotExpired() {
        String validExpiry = "12/99"; // Far future
        assertTrue(isCardExpiryValid(validExpiry));
    }

    @Test
    public void testIsCardExpiryValid_CurrentMonthValid() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
        Calendar cal = Calendar.getInstance();
        String currentExpiry = sdf.format(cal.getTime());
        assertTrue(isCardExpiryValid(currentExpiry));
    }

    @Test
    public void testIsCardExpiryValid_CurrentMonthInvalid() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/yy");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MONTH, -1);
        String pastExpiry = sdf.format(cal.getTime());
        assertFalse(isCardExpiryValid(pastExpiry));
    }

    @Test
    public void testIsCardExpiryValid_ExpiredYear() {
        String expiredExpiry = "12/20";
        Calendar cal = Calendar.getInstance();
        int currentYear = cal.get(Calendar.YEAR) % 100; // Get last two digits of year
        if (currentYear > 20) {
            assertFalse(isCardExpiryValid(expiredExpiry));
        } else {
            // If current year is still 2020, this test will pass differently
            assertTrue(isCardExpiryValid(expiredExpiry));
        }
    }

    // === Helper Methods copied from PaymentActivity for testing ===

    /**
     * Checks if the card expiry date is valid (not expired)
     * @param expiryDate in format MM/YY
     * @return true if not expired, false otherwise
     */
    private boolean isCardExpiryValid(String expiryDate) {
        String[] parts = expiryDate.split("/");
        int month = Integer.parseInt(parts[0]);
        int year = Integer.parseInt(parts[1]);

        // Adjust to full year (e.g., 25 -> 2025)
        int currentYear = new Date().getYear() % 100; // Last two digits of current year
        int currentMonth = new Date().getMonth() + 1; // getMonth() returns 0-based index

        if (year < currentYear) {
            return false;
        } else if (year == currentYear) {
            return month >= currentMonth;
        } else {
            return true; // Year is in future
        }
    }
}