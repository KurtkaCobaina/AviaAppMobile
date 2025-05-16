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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchResultActivity extends AppCompatActivity {
    private LinearLayout flightsContainer;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String IS_LOGGED_IN = "isLoggedIn";
    private static final String USER_ID = "userID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        // Check if the user is logged in
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Search Result");

        // Handle WindowInsets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup navigation
        NavigationUtils.setupNavigation(this, R.id.nav_home);

        // Container for displaying results
        flightsContainer = findViewById(R.id.flights_container);
        TextView noFlightsMessage = findViewById(R.id.no_flights_message);

        // Get data from intent
        List<Flight> flights = (List<Flight>) getIntent().getSerializableExtra("flights");
        if (flights != null && !flights.isEmpty()) {
            fetchAirportsAndDisplayFlights(flights, isLoggedIn);
        } else {
            noFlightsMessage.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Method to load airport data and display flights
     */
    private void fetchAirportsAndDisplayFlights(List<Flight> flights, boolean isLoggedIn) {
        ApiService apiService = ApiClient.getApiService();
        Call<List<Airport>> airportsCall = apiService.getAirports();
        airportsCall.enqueue(new Callback<List<Airport>>() {
            @Override
            public void onResponse(Call<List<Airport>> call, Response<List<Airport>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Airport> airports = response.body();
                    Map<Integer, String> airportCityMap = new HashMap<>();
                    for (Airport airport : airports) {
                        airportCityMap.put(airport.getAirportID(), airport.getCity());
                    }
                    displayFlights(flights, airportCityMap, isLoggedIn);
                } else {
                    Toast.makeText(SearchResultActivity.this, "Error fetching airport data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Airport>> call, Throwable t) {
                Toast.makeText(SearchResultActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Method to display flights
     */
    private void displayFlights(List<Flight> flights, Map<Integer, String> airportCityMap, boolean isLoggedIn) {
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault());

        for (Flight flight : flights) {
            if (flight.getDepartureTime().after(currentDate)) {
                flightsContainer.addView(createFlightCard(flight, airportCityMap, dateFormat, isLoggedIn));
            }
        }
    }

    /**
     * Method to create a card view for a single flight
     */
    private CardView createFlightCard(Flight flight, Map<Integer, String> airportCityMap,
                                      SimpleDateFormat dateFormat, boolean isLoggedIn) {
        // Create CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dpToPx(12));
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(dpToPx(6));
        cardView.setRadius(dpToPx(12));
        cardView.setCardBackgroundColor(getResources().getColor(android.R.color.white));

        // Create ConstraintLayout for card content
        ConstraintLayout constraintLayout = new ConstraintLayout(this);
        constraintLayout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        cardView.addView(constraintLayout);

        // Departure City
        TextView departureCity = createTextView(
                airportCityMap.get(flight.getDepartureAirportID()),
                18, true, android.R.color.black);
        ConstraintLayout.LayoutParams depCityParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        depCityParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        depCityParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        departureCity.setLayoutParams(depCityParams);
        constraintLayout.addView(departureCity);

        // Arrival City
        TextView arrivalCity = createTextView(
                airportCityMap.get(flight.getArrivalAirportID()),
                18, true, android.R.color.black);
        ConstraintLayout.LayoutParams arrCityParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        arrCityParams.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        arrCityParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        arrivalCity.setLayoutParams(arrCityParams);
        constraintLayout.addView(arrivalCity);

        // Departure Time
        TextView departureTime = createTextView(
                dateFormat.format(flight.getDepartureTime()),
                14, false, R.color.dark_gray);
        ConstraintLayout.LayoutParams depTimeParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        depTimeParams.topToBottom = departureCity.getId();
        depTimeParams.startToStart = departureCity.getId();
        depTimeParams.topMargin = dpToPx(4);
        departureTime.setLayoutParams(depTimeParams);
        constraintLayout.addView(departureTime);

        // Arrival Time
        TextView arrivalTime = createTextView(
                dateFormat.format(flight.getArrivalTime()),
                14, false, R.color.dark_gray);
        ConstraintLayout.LayoutParams arrTimeParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        arrTimeParams.topToBottom = arrivalCity.getId();
        arrTimeParams.endToEnd = arrivalCity.getId();
        arrTimeParams.topMargin = dpToPx(4);
        arrivalTime.setLayoutParams(arrTimeParams);
        constraintLayout.addView(arrivalTime);

        // Price
        TextView price = createTextView(
                flight.getPrice() + " ₽",
                16, true, android.R.color.black);
        ConstraintLayout.LayoutParams priceParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        priceParams.topToBottom = departureTime.getId();
        priceParams.startToStart = departureTime.getId();
        priceParams.topMargin = dpToPx(8);
        price.setLayoutParams(priceParams);
        constraintLayout.addView(price);

        // Available Seats
        TextView seatsAvailable = createTextView(
                "Available seats: " + flight.getSeatsAvailable(),
                14, false, R.color.dark_gray);
        ConstraintLayout.LayoutParams seatsParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        seatsParams.topToBottom = price.getId();
        seatsParams.startToStart = price.getId();
        seatsParams.topMargin = dpToPx(4);
        seatsAvailable.setLayoutParams(seatsParams);
        constraintLayout.addView(seatsAvailable);

        // Book Button
        Button bookButton = new Button(this);
        bookButton.setText(isLoggedIn ? "Book" : "Login to book");
        bookButton.setTextSize(16);
        bookButton.setTextColor(getResources().getColor(android.R.color.white));
        bookButton.setBackground(getResources().getDrawable(isLoggedIn ? R.drawable.rounded_button : R.drawable.rounded_button_gray));
        bookButton.setHeight(dpToPx(48));
        bookButton.setAllCaps(false);
        ConstraintLayout.LayoutParams buttonParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.topToBottom = seatsAvailable.getId();
        buttonParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        buttonParams.topMargin = dpToPx(16);
        bookButton.setLayoutParams(buttonParams);

        // Button click handler
        bookButton.setOnClickListener(v -> handleBooking(flight, isLoggedIn));
        constraintLayout.addView(bookButton);

        return cardView;
    }

    private TextView createTextView(String text, int textSizeSp, boolean bold, int textColorRes) {
        TextView textView = new TextView(this);
        textView.setId(View.generateViewId());
        textView.setText(text);
        textView.setTextSize(textSizeSp);
        textView.setTextColor(getResources().getColor(textColorRes));
        if (bold) {
            textView.setTypeface(null, Typeface.BOLD);
        }
        return textView;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void handleBooking(Flight flight, boolean isLoggedIn) {
        if (!isLoggedIn) {
            Toast.makeText(this, "You need to log in to make a booking", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        int seatsCount = getIntent().getIntExtra("seatsCount", 1);
        int updatedSeatsAvailable = flight.getSeatsAvailable() - seatsCount;

        if (updatedSeatsAvailable < 0) {
            Toast.makeText(this, "Not enough available seats on this flight", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userID = sharedPreferences.getInt(USER_ID, -1);
        if (userID == -1) {
            Toast.makeText(this, "Error: User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Booking booking = new Booking();
        booking.setUserID(userID);
        booking.setFlightID(flight.getFlightID());
        booking.setBookingDate(new Date());
        booking.setStatus("booked");
        booking.setAmount(seatsCount);

        ApiService apiService = ApiClient.getApiService();
        Call<Void> createBookingCall = apiService.createBooking(booking);
        createBookingCall.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Map<String, Integer> body = new HashMap<>();
                    body.put("seatsAvailable", updatedSeatsAvailable);
                    Call<Void> updateSeatsCall = apiService.updateFlightSeats(flight.getFlightID(), body);
                    updateSeatsCall.enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Call<Void> call, Response<Void> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(SearchResultActivity.this, "Booking created successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(SearchResultActivity.this, BookingActivity.class));
                                reloadFlights();
                            } else {
                                Toast.makeText(SearchResultActivity.this, "Failed to update seat count", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Void> call, Throwable t) {
                            Toast.makeText(SearchResultActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(SearchResultActivity.this, "Failed to create booking", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SearchResultActivity.this, "Server connection error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void reloadFlights() {
        flightsContainer.removeAllViews();
        List<Flight> flights = (List<Flight>) getIntent().getSerializableExtra("flights");
        if (flights != null && !flights.isEmpty()) {
            SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isLoggedIn = sharedPreferences.getBoolean(IS_LOGGED_IN, false);
            fetchAirportsAndDisplayFlights(flights, isLoggedIn);
        }
    }
}