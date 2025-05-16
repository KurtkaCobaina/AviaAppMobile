package com.example.aviaappmobile;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {
    // Regular expressions for validation
    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я]+$"); // Letters only, no spaces
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+7\\d{10}$"); // +7 followed by 10 digits

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registration);

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
        toolbarTitle.setText("Registration");

        // WindowInsets setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation setup
        NavigationUtils.setupNavigation(this, R.id.nav_login);

        // Add handler for the "Login" button
        findViewById(R.id.login_button).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
        });

        // Add handler for the "Register" button
        findViewById(R.id.register_button).setOnClickListener(v -> {
            registerUser();
        });
    }

    /**
     * Method to register a user
     */
    private void registerUser() {
        // Get references to input fields
        EditText firstNameInput = findViewById(R.id.first_name_input);
        EditText lastNameInput = findViewById(R.id.last_name_input);
        EditText emailInput = findViewById(R.id.email_input);
        EditText phoneInput = findViewById(R.id.phone_input);
        EditText passwordInput = findViewById(R.id.password_input);
        EditText birthdateInput = findViewById(R.id.birthdate_input);

        // Retrieve entered data
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String birthdate = birthdateInput.getText().toString().trim();


// Проверка на пустые поля
        if (TextUtils.isEmpty(firstName)) {
            Toast.makeText(this, "First name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(lastName)) {
            Toast.makeText(this, "Last name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "Phone number cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(birthdate)) {
            Toast.makeText(this, "Date of birth cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Validate first name length
        if (firstName.length() > 20) {
            Toast.makeText(this, "First name must be no longer than 20 characters", Toast.LENGTH_SHORT).show();
            return;
        }

// Validate last name length
        if (lastName.length() > 20) {
            Toast.makeText(this, "Last name must be no longer than 20 characters", Toast.LENGTH_SHORT).show();
            return;
        }

// Validate first name
        if (!NAME_PATTERN.matcher(firstName).matches()) {
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
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate phone number
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            Toast.makeText(this, "Phone number must start with '+7' followed by 10 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate password length
        if (password.length() > 16) {
            Toast.makeText(this, "Password must be no longer than 16 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate date of birth format
        Date dateOfBirth = parseDate(birthdate);
        if (dateOfBirth == null) {
            Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate age (must be at least 18 years old)
        if (!isAdult(dateOfBirth)) {
            Toast.makeText(this, "You must be at least 18 years old", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if email is unique
        checkEmailUniqueness(email, () -> {
            // Hash the password using the PasswordHasher class
            String hashedPassword = PasswordHasher.hashPassword(password);
            if (hashedPassword == null) {
                Toast.makeText(this, "Error hashing password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a User object
            User newUser = new User();
            newUser.setFirstName(firstName);
            newUser.setLastName(lastName);
            newUser.setEmail(email);
            newUser.setPhone(phone);
            newUser.setPassword(hashedPassword);
            newUser.setDateOfBirth(dateOfBirth); // Set date of birth
            newUser.setRole("User"); // Set role to "User"

            // Call API to register the user
            ApiService apiService = ApiClient.getApiService();
            Call<Void> call = apiService.createUser(newUser);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Notify the user
                        Toast.makeText(RegistrationActivity.this, "Registration successful", Toast.LENGTH_SHORT).show();
                        // Navigate to the login screen
                        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "Error during registration", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Handle network error
                    Toast.makeText(RegistrationActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    /**
     * Method to parse a date string into a Date object
     */
    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // Date format
            sdf.setLenient(false); // Strict format checking
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Method to check if the user is at least 18 years old
     */
    private boolean isAdult(Date dateOfBirth) {
        Calendar dob = Calendar.getInstance();
        dob.setTime(dateOfBirth);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);

        // Adjust age if birthday hasn't occurred yet this year
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age >= 18;
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

                    // Check if another user already has this email
                    for (User user : users) {
                        if (user.getEmail().equalsIgnoreCase(email)) {
                            isUnique = false;
                            break;
                        }
                    }

                    if (isUnique) {
                        // Email is unique, proceed with registration
                        onSuccess.run();
                    } else {
                        Toast.makeText(RegistrationActivity.this, "This email is already registered", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegistrationActivity.this, "Error checking email uniqueness", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}