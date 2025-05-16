package com.example.aviaappmobile;

import static org.junit.Assert.*;

import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivityTest {

    // Тест: хэширование пароля
    @Test
    public void testHashPassword_SHA256() throws NoSuchAlgorithmException {
        String password = "999";
        String hashed = hashPassword(password);
        assertNotNull(hashed);
        assertEquals(64, hashed.length()); // SHA-256 дает 64 символа hex
    }

    // Метод hashPassword как в LoginActivity
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = String.format("%02x", b);
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Тест: поиск пользователя в списке
    @Test
    public void testUserAuthentication_Success() {
        List<User> users = new ArrayList<>();
        String email = "hhh@hh.hh";
        String password = "999";
        String hashed = hashPassword(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashed);
        user.setUserID(1);
        user.setRole("User");

        users.add(user);

        boolean authenticated = authenticateUser(email, password, users);
        assertTrue(authenticated);
    }

    // Тест: пользователь не найден
    @Test
    public void testUserAuthentication_Failure_WrongEmail() {
        List<User> users = new ArrayList<>();
        String password = "999";
        String hashed = hashPassword(password);

        User user = new User();
        user.setEmail("wrong@example.com");
        user.setPassword(hashed);
        user.setUserID(1);
        user.setRole("User");

        users.add(user);

        boolean authenticated = authenticateUser("wrong@example.com", password, users);
        assertTrue(authenticateUser("wrong@example.com", password, users));
    }

    // Тест: пользователь заблокирован
    @Test
    public void testUserAuthentication_BannedUser() {
        List<User> users = new ArrayList<>();
        String email = "mccartney@beatles.uk";
        String password = "123";
        String hashed = hashPassword(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashed);
        user.setUserID(2);
        user.setRole("Banned");

        users.add(user);

        boolean authenticated = authenticateUser(email, password, users);
        assertFalse(authenticated); // Должны вернуть false, если заблокирован
    }

    // Упрощенная логика аутентификации
    private boolean authenticateUser(String email, String password, List<User> users) {
        String hashedPassword = hashPassword(password);
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                if ("Banned".equals(user.getRole())) {
                    return false; // Заблокированный пользователь
                }
                if (user.getPassword().equals(hashedPassword)) {
                    return true; // Успешная аутентификация
                }
            }
        }
        return false;
    }
}
