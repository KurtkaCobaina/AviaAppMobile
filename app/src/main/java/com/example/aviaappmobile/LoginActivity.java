package com.example.aviaappmobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences; // For saving login state
    private static final String PREFS_NAME = "UserPrefs";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_ID = "userID"; // For storing user ID
    private static final String USER_EMAIL = "userEmail"; // For storing user email

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if the user is already logged in
        if (isUserLoggedIn()) {
            navigateToProfileActivity();
        }

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
        toolbarTitle.setText("Login");

        // WindowInsets setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation setup
        NavigationUtils.setupNavigation(this, R.id.nav_login);
        findViewById(R.id.register_link).setOnClickListener(v -> {
            startActivity(new Intent(this, RegistrationActivity.class));
        });

        findViewById(R.id.forgot_password_link).setOnClickListener(v -> {
            startActivity(new Intent(this, PasswordChangeActivity.class));
        });

        // Set up the login button
        findViewById(R.id.login_button).setOnClickListener(v -> {
            performLogin();
        });
    }

    /**
     * Method to perform login
     */
    private void performLogin() {
        // Get references to email and password input fields
        EditText emailField = findViewById(R.id.email_input); // Email field
        EditText passwordField = findViewById(R.id.password_input);

        // Retrieve entered data
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        // Validate entered data
        if (!isValidEmail(email)) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Hash the entered password
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) {
            Toast.makeText(this, "Error hashing password", Toast.LENGTH_SHORT).show();
            return;
        }

        // Call API to fetch user list
        ApiService apiService = ApiClient.getApiService();
        Call<List<User>> call = apiService.getUsers();

        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();

                    // Search for a user with matching email and password
                    boolean isAuthenticated = false;
                    for (User user : users) {
                        if (user.getEmail().equals(email) && user.getPassword().equals(hashedPassword)) {
                            // Check if the user is banned
                            if ("Banned".equals(user.getRole())) {
                                Toast.makeText(LoginActivity.this, "Your account has been banned", Toast.LENGTH_SHORT).show();
                                return; // Exit the method if the user is banned
                            }

                            isAuthenticated = true;

                            // Save login state, email, and UserID
                            saveLoginState(true, user.getUserID(), user.getEmail());
                            navigateToProfileActivity(); // Navigate to ProfileActivity
                            break;
                        }
                    }

                    // If no matching user is found
                    if (!isAuthenticated) {
                        Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // Handle network error
                Toast.makeText(LoginActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Validate email address
     */
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Hash password using SHA-256
     */
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

    /**
     * Save login state, UserID, and email
     */
    private void saveLoginState(boolean isLoggedIn, int userID, String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LOGGED_IN, isLoggedIn);
        editor.putInt(USER_ID, userID); // Save UserID
        editor.putString(USER_EMAIL, email); // Save email
        editor.apply();
    }

    /**
     * Check if the user is logged in
     */
    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(IS_LOGGED_IN, false);
    }

    /**
     * Navigate to ProfileActivity
     */
    private void navigateToProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}