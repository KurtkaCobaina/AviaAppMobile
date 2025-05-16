package com.example.aviaappmobile;

import android.util.Patterns;

public class EmailValidator {

    /**
     * Проверяет, является ли строка корректным email-адресом.
     *
     * @param email email для проверки
     * @return true, если email валиден
     */
    public static boolean isValidEmail(String email) {
        return email != null && !email.trim().isEmpty() &&
                Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}