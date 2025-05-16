package com.example.aviaappmobile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Objects;
import java.util.regex.Pattern;

public class ProfileActivity extends AppCompatActivity {
    private SharedPreferences sharedPreferences; // For managing login state
    private static final String PREFS_NAME = "UserPrefs";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_EMAIL = "userEmail"; // For storing user email
    private EditText nameInput, lastNameInput, emailInput, phoneInput; // Fields for displaying user data
    private int userId; // Current user ID

    // Regular expressions for validation
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я]+$"); // Letters only, no spaces
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$"); // +7 followed by 10 digits

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get references to input fields
        nameInput = findViewById(R.id.name_input);
        lastNameInput = findViewById(R.id.last_name_input);
        emailInput = findViewById(R.id.email_input);
        phoneInput = findViewById(R.id.phone_input);

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
        toolbarTitle.setText("Your Profile");

        // WindowInsets setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation setup
        NavigationUtils.setupNavigation(this, R.id.nav_login);
        findViewById(R.id.past_flights_btn).setOnClickListener(v -> {
            startActivity(new Intent(this, PastFlightsActivity.class));
        });

        // Add handler for the logout button
        findViewById(R.id.logout_button).setOnClickListener(v -> logout());

        // Add handler for the "Save Changes" button
        findViewById(R.id.save_button).setOnClickListener(v -> saveUserData());

        // Load user data
        loadUserData();
    }

    /**
     * Method to load user data
     */
    private void loadUserData() {
        // Retrieve saved user email
        String userEmail = sharedPreferences.getString(USER_EMAIL, null);
        if (TextUtils.isEmpty(userEmail)) {
            Toast.makeText(this, "Error: User email not found", Toast.LENGTH_SHORT).show();
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
                    // Search for user by email
                    for (User user : users) {
                        if (user.getEmail().equals(userEmail)) {
                            // Save user ID
                            userId = user.getUserID();
                            // Populate fields with user data
                            nameInput.setText(user.getFirstName());
                            lastNameInput.setText(user.getLastName());
                            emailInput.setText(user.getEmail());
                            phoneInput.setText(user.getPhone());
                            return;
                        }
                    }
                    // If user is not found
                    Toast.makeText(ProfileActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                // Handle network error
                Toast.makeText(ProfileActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method to save changes to user data
     */
    private void saveUserData() {
        // Retrieve entered data
        String name = nameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        // Validate first name length
        if (name.length() > 20) {
            Toast.makeText(this, "First name must be no longer than 20 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate last name length
        if (lastName.length() > 20) {
            Toast.makeText(this, "Last name must be no longer than 20 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate first name
        if (!NAME_PATTERN.matcher(name).matches()) {
            Toast.makeText(this, "First name should contain only letters and no spaces", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate last name
        if (!NAME_PATTERN.matcher(lastName).matches()) {
            Toast.makeText(this, "Last name should contain only letters and no spaces", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate email
        if (!email.contains("@") || !email.contains(".")) {
            Toast.makeText(this, "Email must contain '@' and '.'", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            Toast.makeText(this, "Phone number must start with '+7' followed by 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check email uniqueness
        checkEmailUniqueness(email, () -> {
            // Create a User object with current data
            User updatedUser = new User();
            updatedUser.setUserID(userId);
            updatedUser.setFirstName(name);
            updatedUser.setLastName(lastName);
            updatedUser.setEmail(email);
            updatedUser.setPhone(phone);

            // Call API to update user data
            ApiService apiService = ApiClient.getApiService();
            Call<Void> call = apiService.updateUser(userId, updatedUser);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Update email in SharedPreferences if it changed
                        String currentEmail = sharedPreferences.getString(USER_EMAIL, null);
                        if (updatedUser.getEmail() != null && !currentEmail.equals(updatedUser.getEmail())) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(USER_EMAIL, updatedUser.getEmail());
                            editor.apply();
                        }
                        // Notify the user
                        Toast.makeText(ProfileActivity.this, "Data successfully saved", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Error saving data", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Handle network error
                    Toast.makeText(ProfileActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Method to log out
     */
    private void logout() {
        // Clear login state
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(IS_LOGGED_IN, false); // Reset login flag
        editor.remove(USER_EMAIL); // Remove user email
        editor.apply();

        // Notify the user
        Toast.makeText(this, "You have successfully logged out", Toast.LENGTH_SHORT).show();

        // Navigate to the login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear activity stack
        startActivity(intent);
        finish(); // Close the current activity
    }

    /**
     * Method to check if email is unique
     */
    private void checkEmailUniqueness(String email, Runnable onSuccess) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<User>> call = apiService.getUsers(); // Assume this method exists
        call.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<User> users = response.body();
                    boolean isUnique = true;
                    // Check if another user has the same email
                    for (User user : users) {
                        if (user.getUserID() != userId && user.getEmail().equalsIgnoreCase(email)) {
                            isUnique = false;
                            break;
                        }
                    }
                    if (isUnique) {
                        // Email is unique, proceed
                        onSuccess.run();
                    } else {
                        Toast.makeText(ProfileActivity.this, "This email is already registered", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ProfileActivity.this, "Error checking email uniqueness", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}