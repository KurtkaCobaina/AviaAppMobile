package com.example.aviaappmobile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {
    private LinearLayout bookingsContainer;
    private TextView noBookingsMessage;
    private TextView totalAmountTextView;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String USER_ID = "userID";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private List<Booking> userBookings = new ArrayList<>();
    private List<Flight> allFlights = new ArrayList<>();
    private Map<Integer, String> airportCityMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        // Check login status
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);
        if (!isLoggedIn) {
            Toast.makeText(this, "You need to log in to view your bookings", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Your Bookings");

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup navigation
        NavigationUtils.setupNavigation(this, R.id.nav_bookings);

        // Initialize UI elements
        bookingsContainer = findViewById(R.id.bookings_container);
        noBookingsMessage = findViewById(R.id.no_bookings_message);
        totalAmountTextView = findViewById(R.id.total_amount);

        // Load data
        loadBookingsFlightsAndAirports();
    }

    private void loadBookingsFlightsAndAirports() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userID = sharedPreferences.getInt(USER_ID, -1);
        if (userID == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();

        // Load bookings
        Call<List<Booking>> bookingsCall = apiService.getBookings();
        bookingsCall.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userBookings = filterBookingsByUserID(response.body(), userID);

                    // Load flights
                    Call<List<Flight>> flightsCall = apiService.getFlights();
                    flightsCall.enqueue(new Callback<List<Flight>>() {
                        @Override
                        public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                allFlights = response.body();

                                // Load airports
                                Call<List<Airport>> airportsCall = apiService.getAirports();
                                airportsCall.enqueue(new Callback<List<Airport>>() {
                                    @Override
                                    public void onResponse(Call<List<Airport>> call, Response<List<Airport>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            airportCityMap.clear();
                                            for (Airport airport : response.body()) {
                                                airportCityMap.put(airport.getAirportID(), airport.getCity());
                                            }
                                            updateUI();
                                        } else {
                                            showError("Failed to load airport data");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<Airport>> call, Throwable t) {
                                        showError("Server connection error");
                                    }
                                });
                            } else {
                                showError("Failed to load flight data");
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Flight>> call, Throwable t) {
                            showError("Server connection error");
                        }
                    });
                } else {
                    showError("Failed to load bookings");
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                showError("Server connection error");
            }
        });
    }

    private void updateUI() {
        bookingsContainer.removeAllViews();
        if (userBookings.isEmpty()) {
            noBookingsMessage.setVisibility(View.VISIBLE);
            totalAmountTextView.setVisibility(View.GONE);
            return;
        }
        noBookingsMessage.setVisibility(View.GONE);
        totalAmountTextView.setVisibility(View.VISIBLE);

        int totalAmount = 0;
        for (Booking booking : userBookings) {
            if ("booked".equalsIgnoreCase(booking.getStatus())) {
                Flight flight = findFlightById(allFlights, booking.getFlightID());
                if (flight != null) {
                    String departureCity = airportCityMap.get(flight.getDepartureAirportID());
                    String arrivalCity = airportCityMap.get(flight.getArrivalAirportID());
                    CardView bookingCard = createBookingCard(booking, flight, departureCity, arrivalCity);
                    bookingsContainer.addView(bookingCard);
                    totalAmount += booking.getAmount() * flight.getPrice();
                }
            }
        }
        totalAmountTextView.setText("Total amount to pay: " + totalAmount + " ₽");
    }

    private CardView createBookingCard(Booking booking, Flight flight, String departureCity, String arrivalCity) {
        // Create CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(dpToPx(12));
        cardView.setCardElevation(dpToPx(6));
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

        // Create ConstraintLayout inside CardView
        ConstraintLayout constraintLayout = new ConstraintLayout(this);
        constraintLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        cardView.addView(constraintLayout);

        // Flight Number
        TextView flightNumber = createTextView(flight.getFlightNumber(), 18, true);
        flightNumber.setId(View.generateViewId());
        ConstraintLayout.LayoutParams flightNumberParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        flightNumberParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        flightNumberParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        flightNumberParams.leftMargin = dpToPx(8);
        flightNumber.setLayoutParams(flightNumberParams);
        constraintLayout.addView(flightNumber);

        // Departure Date
        TextView departureDate = createTextView(formatDate(flight.getDepartureTime()), 14, false);
        departureDate.setId(View.generateViewId());
        ConstraintLayout.LayoutParams departureDateParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        departureDateParams.startToStart = flightNumber.getId();
        departureDateParams.topToBottom = flightNumber.getId();
        departureDateParams.topMargin = dpToPx(4);
        departureDate.setLayoutParams(departureDateParams);
        constraintLayout.addView(departureDate);

        // Seats
        TextView seats = createTextView("Seats: " + booking.getAmount(), 14, false);
        seats.setId(View.generateViewId());
        ConstraintLayout.LayoutParams seatsParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        seatsParams.startToStart = departureDate.getId();
        seatsParams.topToBottom = departureDate.getId();
        seatsParams.topMargin = dpToPx(4);
        seats.setLayoutParams(seatsParams);
        constraintLayout.addView(seats);

        // Price Per Seat
        TextView pricePerSeat = createTextView(flight.getPrice() + " ₽/seat", 14, false);
        pricePerSeat.setId(View.generateViewId());
        ConstraintLayout.LayoutParams pricePerSeatParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        pricePerSeatParams.startToStart = seats.getId();
        pricePerSeatParams.topToBottom = seats.getId();
        pricePerSeatParams.topMargin = dpToPx(4);
        pricePerSeat.setLayoutParams(pricePerSeatParams);
        constraintLayout.addView(pricePerSeat);

        // Total Price
        int totalPrice = booking.getAmount() * flight.getPrice();
        TextView totalPriceView = createTextView(totalPrice + " ₽", 16, true);
        totalPriceView.setId(View.generateViewId());
        ConstraintLayout.LayoutParams totalPriceParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        totalPriceParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        totalPriceParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        totalPriceView.setLayoutParams(totalPriceParams);
        constraintLayout.addView(totalPriceView);

        // Delete Button
        Button deleteButton = new Button(this);
        deleteButton.setId(View.generateViewId());
        deleteButton.setText("Delete");
        deleteButton.setTextSize(14);
        deleteButton.setTextColor(getResources().getColor(R.color.white));
        deleteButton.setBackground(getResources().getDrawable(R.drawable.rounded_button_pink));
        deleteButton.setHeight(dpToPx(40));
        ConstraintLayout.LayoutParams deleteButtonParams = new ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        deleteButtonParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        deleteButtonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        deleteButtonParams.topToBottom = pricePerSeat.getId();
        deleteButtonParams.topMargin = dpToPx(12);
        deleteButtonParams.width = 0;
        deleteButtonParams.horizontalWeight = 1f;
        deleteButton.setLayoutParams(deleteButtonParams);
        deleteButton.setOnClickListener(v -> deleteBooking(booking.getBookingID()));
        constraintLayout.addView(deleteButton);

        // Buy Button
        Button buyButton = new Button(this);
        buyButton.setId(View.generateViewId());
        buyButton.setText("Buy");
        buyButton.setTextSize(14);
        buyButton.setTextColor(getResources().getColor(R.color.white));
        buyButton.setBackground(getResources().getDrawable(R.drawable.rounded_button));
        buyButton.setHeight(dpToPx(40));
        ConstraintLayout.LayoutParams buyButtonParams = new ConstraintLayout.LayoutParams(
                0,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        buyButtonParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        buyButtonParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        buyButtonParams.topToBottom = deleteButton.getId();
        buyButtonParams.topMargin = dpToPx(8);
        buyButtonParams.width = 0;
        buyButtonParams.horizontalWeight = 1f;
        buyButton.setLayoutParams(buyButtonParams);
        buyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, PaymentActivity.class);
            intent.putExtra("bookingID", booking.getBookingID());
            startActivity(intent);
        });
        constraintLayout.addView(buyButton);

        return cardView;
    }

    private TextView createTextView(String text, int textSizeSp, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(textSizeSp);
        textView.setTextColor(getResources().getColor(R.color.black));
        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void buyAllBookings() {
        // Implement buy all functionality
        Toast.makeText(this, "Buy all bookings", Toast.LENGTH_SHORT).show();
    }

    private void deleteAllBookings() {
        // Implement delete all functionality
        Toast.makeText(this, "Delete all bookings", Toast.LENGTH_SHORT).show();
    }

    private List<Booking> filterBookingsByUserID(List<Booking> bookings, int userID) {
        List<Booking> filtered = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getUserID() == userID) {
                filtered.add(booking);
            }
        }
        return filtered;
    }

    private Flight findFlightById(List<Flight> flights, int flightID) {
        for (Flight flight : flights) {
            if (flight.getFlightID() == flightID) {
                return flight;
            }
        }
        return null;
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
        return sdf.format(date);
    }

    private void deleteBooking(int bookingID) {
        ApiService apiService = ApiClient.getApiService();

        // Step 1: Get all bookings
        Call<List<Booking>> getBookingsCall = apiService.getBookings();
        getBookingsCall.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    Booking booking = null;
                    for (Booking b : bookings) {
                        if (b.getBookingID() == bookingID) {
                            booking = b;
                            break;
                        }
                    }
                    if (booking == null) {
                        Toast.makeText(BookingActivity.this, "Booking not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int flightID = booking.getFlightID(); // Flight ID
                    int amount = booking.getAmount(); // Number of seats

                    // Step 2: Delete the booking
                    Call<Void> deleteBookingCall = apiService.deleteBooking(bookingID);
                    deleteBookingCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {

                                // Step 3: Get all flights
                                Call<List<Flight>> getFlightsCall = apiService.getFlights();
                                getFlightsCall.enqueue(new Callback<List<Flight>>() {
                                    @Override
                                    public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                                        if (response.isSuccessful() && response.body() != null) {
                                            List<Flight> flights = response.body();
                                            Flight flight = null;
                                            for (Flight f : flights) {
                                                if (f.getFlightID() == flightID) {
                                                    flight = f;
                                                    break;
                                                }
                                            }
                                            if (flight == null) {
                                                Toast.makeText(BookingActivity.this, "Flight not found", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            // Update available seats
                                            int updatedSeatsAvailable = flight.getSeatsAvailable() + amount;
                                            flight.setSeatsAvailable(updatedSeatsAvailable);

                                            // Step 4: Update the flight
                                            Call<Void> updateFlightCall = apiService.updateFlight(flightID, flight);
                                            updateFlightCall.enqueue(new Callback<Void>() {
                                                @Override
                                                public void onResponse(Call<Void> call, Response<Void> response) {
                                                    if (response.isSuccessful()) {
                                                        Toast.makeText(BookingActivity.this, "Booking successfully deleted", Toast.LENGTH_SHORT).show();
                                                        reloadBookingsFlightsAndAirports(); // Reload data
                                                    } else {
                                                        Toast.makeText(BookingActivity.this, "Failed to update flight", Toast.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<Void> call, Throwable t) {
                                                    Toast.makeText(BookingActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            Toast.makeText(BookingActivity.this, "Failed to retrieve flights", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<List<Flight>> call, Throwable t) {
                                        Toast.makeText(BookingActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } else {
                                Toast.makeText(BookingActivity.this, "Failed to delete booking", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(BookingActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(BookingActivity.this, "Failed to retrieve bookings", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method to reload booking, flight, and airport data
     */
    private void reloadBookingsFlightsAndAirports() {
        bookingsContainer.removeAllViews(); // Clear container
        loadBookingsFlightsAndAirports(); // Reload data
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}