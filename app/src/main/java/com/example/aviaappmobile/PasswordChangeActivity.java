package com.example.aviaappmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Objects;
import java.util.regex.Pattern;

public class PasswordChangeActivity extends AppCompatActivity {

    private EditText emailInput, phoneInput, newPasswordInput, confirmPasswordInput;
    private Button changePasswordButton;
    private int userId = -1;

    // Regular expression for phone number format validation
    private static final String PHONE_REGEX = "^[+]?[0-9]{10,13}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_password_change);
        findViewById(R.id.nav_login).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.nav_home).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_bookings).setOnClickListener(v -> startActivity(new Intent(this, BookingActivity.class)));
        findViewById(R.id.nav_history).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));

        // Initialize UI elements
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);
        newPasswordInput = findViewById(R.id.new_password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        changePasswordButton = findViewById(R.id.change_password_button);

        // Set up window insets listener for navigation
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Toolbar setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Change Password");

        // WindowInsets setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation setup
        NavigationUtils.setupNavigation(this, R.id.nav_login);

        // "Change Password" button handler
        changePasswordButton.setOnClickListener(v -> changePassword());

        // "Register" link handler
        findViewById(R.id.register_link).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
        });
    }

    private void changePassword() {
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String newPassword = newPasswordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(email)) {
            showToast("Enter email");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            showToast("Enter phone number");
            return;
        }
        if (!isValidPhoneNumber(phone)) {
            showToast("Invalid phone number format");
            return;
        }
        if (TextUtils.isEmpty(newPassword)) {
            showToast("Enter new password");
            return;
        }
        if (newPassword.length() > 16) { // Check password length
            showToast("Password must not exceed 16 characters");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            showToast("Passwords don't match");
            return;
        }

        // Hash the new password before sending it to the server
        String hashedNewPassword = PasswordHasher.hashPassword(newPassword);
        if (hashedNewPassword == null) {
            showToast("Error hashing password");
            return;
        }

        // Find user by email and phone to verify identity
        findUserByEmailAndPhone(email, phone, hashedNewPassword);
    }

    private boolean isValidPhoneNumber(String phone) {
        return Pattern.matches(PHONE_REGEX, phone);
    }

    private void findUserByEmailAndPhone(String email, String phone, String hashedNewPassword) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<User>> call = apiService.getUsers();

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    boolean userFound = false;
                    for (User user : response.body()) {
                        if (user.getEmail().equalsIgnoreCase(email)) {
                            if (user.getPhone().equals(phone)) {
                                userId = user.getUserID();
                                userFound = true;
                                updatePassword(userId, hashedNewPassword); // Pass userId and hashed password
                                break;
                            } else {
                                showToast("Incorrect phone number for this email");
                                return;
                            }
                        }
                    }
                    if (!userFound) {
                        showToast("User not found with this email");
                    }
                } else {
                    showToast("Error fetching users");
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                showToast("Server connection error");
            }
        });
    }

    private void updatePassword(int userId, String hashedNewPassword) {
        Map<String, String> passwordData = new HashMap<>();
        passwordData.put("newPassword", hashedNewPassword);

        ApiService apiService = ApiClient.getApiService();
        Call<Void> call = apiService.updatePassword(userId, passwordData);

        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    showToast("Password changed successfully");
                    navigateToLogin();
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        showToast("Failed to change password: " + errorBody);
                    } catch (Exception e) {
                        showToast("Failed to change password: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                showToast("Server connection error");
            }
        });
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void navigateToLogin() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}