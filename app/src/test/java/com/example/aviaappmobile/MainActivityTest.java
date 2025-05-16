package com.example.aviaappmobile;

import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class MainActivityTest {

    @Test
    public void testOnlyLettersPattern_ValidInput() {
        assertTrue(MainActivity.getOnlyLettersPattern().matcher("New York").matches());
        assertTrue(MainActivity.getOnlyLettersPattern().matcher("Москва").matches());
    }

    @Test
    public void testOnlyLettersPattern_InvalidInput() {
        assertFalse(MainActivity.getOnlyLettersPattern().matcher("Moscow123").matches());
        assertFalse(MainActivity.getOnlyLettersPattern().matcher("Los Angeles!").matches());
    }

    @Test
    public void testDatePattern_ValidFormat() {
        assertTrue(MainActivity.getDatePattern().matcher("2025-12-31").matches());
        assertTrue(MainActivity.getDatePattern().matcher("2024-01-01").matches());
    }

    @Test
    public void testDatePattern_InvalidFormat() {
        assertFalse(MainActivity.getDatePattern().matcher("31-12-2025").matches());
        assertFalse(MainActivity.getDatePattern().matcher("2025/12/31").matches());
    }
}