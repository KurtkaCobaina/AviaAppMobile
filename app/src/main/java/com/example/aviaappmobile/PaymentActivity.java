package com.example.aviaappmobile;

import android.content.Intent;
import android.os.Bundle;
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
import java.util.Map;
import java.util.Date;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.Objects;
import java.util.regex.Pattern;

public class PaymentActivity extends AppCompatActivity {

    private EditText cardNumberInput, expiryDateInput, cvcInput;
    private Button payButton;
    private int bookingID;

    // Regular expressions for validation
    private static final Pattern CARD_NUMBER_PATTERN = Pattern.compile("^\\d{4} \\d{4} \\d{4} \\d{4}$"); // 16 digits with spaces
    private static final Pattern EXPIRY_DATE_PATTERN = Pattern.compile("^(0[1-9]|1[0-2])\\/(?:[2-9][0-9])$"); // MM/YY (01-12)/(20-99)
    private static final Pattern CVC_PATTERN = Pattern.compile("^\\d{3}$"); // 3 digits

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment);

        // Retrieve bookingID from Intent
        bookingID = getIntent().getIntExtra("bookingID", -1);
        if (bookingID == -1) {
            Toast.makeText(this, "Error: Booking ID not found", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity
            return;
        }

        // Initialize UI elements
        cardNumberInput = findViewById(R.id.card_number_input);
        expiryDateInput = findViewById(R.id.expiry_date_input);
        cvcInput = findViewById(R.id.cvc_input);
        payButton = findViewById(R.id.pay_button);

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
        toolbarTitle.setText("Payment");

        // WindowInsets setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation setup
        NavigationUtils.setupNavigation(this, R.id.nav_bookings);

        // "Pay" button handler
        payButton.setOnClickListener(v -> processPayment());
    }

    /**
     * Method to process payment
     */
    private void processPayment() {
        String cardNumber = cardNumberInput.getText().toString().trim();
        String expiryDate = expiryDateInput.getText().toString().trim();
        String cvc = cvcInput.getText().toString().trim();
        // Check for empty fields
        if (cardNumber.isEmpty()) {
            Toast.makeText(this, "Card number cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (expiryDate.isEmpty()) {
            Toast.makeText(this, "Expiry date cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cvc.isEmpty()) {
            Toast.makeText(this, "CVC cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }
        // Validate input data
        if (!CARD_NUMBER_PATTERN.matcher(cardNumber).matches()) {
            Toast.makeText(this, "Invalid card number. Format: 1234 5678 9012 3456", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!EXPIRY_DATE_PATTERN.matcher(expiryDate).matches()) {
            Toast.makeText(this, "Invalid expiry date. Format: MM/YY (e.g., 12/25)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!CVC_PATTERN.matcher(cvc).matches()) {
            Toast.makeText(this, "Invalid CVC. Must be 3 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if card has expired
        if (!isCardExpiryValid(expiryDate)) {
            Toast.makeText(this, "Card has expired", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a PaymentCard object
        PaymentCard paymentCard = new PaymentCard();
        paymentCard.setCardNumber(cardNumber);
        paymentCard.setGoodThroghDate(expiryDate);
        paymentCard.setCVC(Integer.parseInt(cvc));

        // Call API to save card data
        ApiService apiService = ApiClient.getApiService();
        Call<Map<String, Object>> saveCardCall = apiService.createPaymentCard(paymentCard);
        saveCardCall.enqueue(new Callback<Map<String, Object>>() {
            @Override
            public void onResponse(Call<Map<String, Object>> call, Response<Map<String, Object>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int newCardId = ((Double) response.body().get("CardId")).intValue();

                    Payment payment = new Payment();
                    payment.setBookingID(bookingID);
                    payment.setCardId(newCardId);
                    payment.setPaymentDate(new Date());
                    payment.setPaymentMethod("Credit Card");
                    payment.setPaymentStatus("Completed");

                    Call<Void> savePaymentCall = apiService.createPayment(payment);
                    savePaymentCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                updateBookingStatus();
                            } else {
                                Toast.makeText(PaymentActivity.this, "Error saving payment", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(PaymentActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(PaymentActivity.this, "Error saving card data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, Object>> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }
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
    /**
     * Method to update booking status
     */
    private void updateBookingStatus() {
        ApiService apiService = ApiClient.getApiService();

        // Create an object to send the new status
        Map<String, String> statusUpdate = new HashMap<>();
        statusUpdate.put("status", "confirmed");

        // Call API to update booking status
        Call<Void> updateBookingCall = apiService.updateBookingStatus(bookingID, statusUpdate);
        updateBookingCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(PaymentActivity.this, "Payment completed successfully and booking confirmed", Toast.LENGTH_SHORT).show();
                    navigateToHistory();
                } else {
                    Toast.makeText(PaymentActivity.this, "Error updating booking status", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(PaymentActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Navigate to the booking history screen
     */
    private void navigateToHistory() {
        Intent intent = new Intent(this, HistoryActivity.class);
        startActivity(intent);
        finish(); // Close the current activity
    }
}