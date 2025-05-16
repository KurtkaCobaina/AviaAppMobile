package com.example.aviaappmobile;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class ProfileActivityTest {

    // Регулярные выражения для валидации
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$");

    // EMAIL_PATTERN вместо android.util.Patterns.EMAIL_ADDRESS
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );

    private List<User> mockUsers; // Список пользователей для тестирования

    @Before
    public void setUp() {
        // Имитируем список пользователей
        mockUsers = new ArrayList<>();

        User user1 = new User();
        user1.setUserID(1);
        user1.setEmail("ivan@example.com");
        user1.setPhone("+79123456789");
        user1.setFirstName("Ivan");
        user1.setLastName("Petrov");

        User user2 = new User();
        user2.setUserID(2);
        user2.setEmail("petr@example.com");
        user2.setPhone("+79876543210");
        user2.setFirstName("Petr");
        user2.setLastName("Sidorov");

        mockUsers.add(user1);
        mockUsers.add(user2);
    }

    // === Тестирование форматов ===

    @Test
    public void testValidNameFormat() {
        assertTrue(NAME_PATTERN.matcher("Ivan").matches());
        assertTrue(NAME_PATTERN.matcher("Иван").matches());
        assertFalse(NAME_PATTERN.matcher("Ivan123").matches());
        assertFalse(NAME_PATTERN.matcher("Ivan Ivanov").matches());
        assertFalse(NAME_PATTERN.matcher("").matches());
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
    public void testValidEmailFormat() {
        assertTrue(EMAIL_PATTERN.matcher("test@example.com").matches());
        assertTrue(EMAIL_PATTERN.matcher("user.name+tag@sub.domain.com").matches());
        assertFalse(EMAIL_PATTERN.matcher("testexample.com").matches());
        assertFalse(EMAIL_PATTERN.matcher("test@com").matches());
        assertFalse(EMAIL_PATTERN.matcher("test").matches());
        assertFalse(EMAIL_PATTERN.matcher("").matches());
    }

    // === Валидация данных ===

    @Test
    public void testValidateEmail() {
        assertTrue(isValidEmail("test@example.com"));
        assertTrue(isValidEmail("user.name+tag@sub.domain.com"));
        assertFalse(isValidEmail("testexample.com"));
        assertFalse(isValidEmail("test@com"));
        assertFalse(isValidEmail("test"));
        assertFalse(isValidEmail(""));
        assertFalse(isValidEmail(null));
    }

    @Test
    public void testValidatePhone() {
        assertTrue(isValidPhone("+79123456789"));
        assertFalse(isValidPhone("89123456789"));
        assertFalse(isValidPhone("+712345678"));
        assertFalse(isValidPhone("+712345678901"));
        assertFalse(isValidPhone(""));
        assertFalse(isValidPhone(null));
    }

    @Test
    public void testFirstNameLengthLimit() {
        StringBuilder longName = new StringBuilder();
        for (int i = 0; i < 65; i++) longName.append("a");
        assertEquals(65, longName.length());
        assertFalse(isValidNameLength(longName.toString()));
    }

    @Test
    public void testValidFirstNameLength() {
        assertTrue(isValidNameLength("Ivan"));
    }

    @Test
    public void testCheckEmailUniqueness_IsUnique() {
        String newEmail = "newuser@example.com";
        boolean isUnique = checkEmailUniqueness(newEmail, 1);
        assertTrue(isUnique);
    }

    @Test
    public void testCheckEmailUniqueness_NotUnique() {
        String existingEmail = "ivan@example.com";
        boolean isUnique = checkEmailUniqueness(existingEmail, 2); // другой пользователь
        assertFalse(isUnique);
    }

    // === Вспомогательные методы из ProfileActivity ===

    private boolean isValidNameLength(String name) {
        return name != null && name.length() <= 64;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        if (!email.contains("@") || !email.contains(".")) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean checkEmailUniqueness(String email, int currentUserId) {
        for (User user : mockUsers) {
            if (user.getUserID() != currentUserId &&
                    user.getEmail().equalsIgnoreCase(email)) {
                return false;
            }
        }
        return true;
    }

    // === Вспомогательный класс User для тестирования ===

    private static class User {
        private int userID;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;

        public int getUserID() {
            return userID;
        }

        public void setUserID(int userID) {
            this.userID = userID;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}