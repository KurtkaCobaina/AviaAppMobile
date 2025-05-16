package com.example.aviaappmobile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

    public static String hashPassword(String password) {
        try {
            // Создаем экземпляр MessageDigest для SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Вычисляем хэш от пароля, преобразованного в байты UTF-8
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            // Преобразуем массив байтов в шестнадцатеричную строку
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                // Добавляем каждый байт как двухсимвольное шестнадцатеричное значение
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }

            // Возвращаем результат в виде строки
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            // Обработка исключения, если алгоритм недоступен
            e.printStackTrace();
            return null; // или выбросить исключение, если это критично
        }
    }
}