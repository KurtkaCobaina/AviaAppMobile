package com.example.aviaappmobile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
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
import java.util.List;
import java.util.Objects;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PastFlightsActivity extends AppCompatActivity {
    private LinearLayout pastFlightsContainer;
    private TextView noPastFlightsMessage;
    private static final String PREFS_NAME = "UserPrefs";
    private static final String USER_ID = "userID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_past_flights);

        pastFlightsContainer = findViewById(R.id.past_flights_container);
        noPastFlightsMessage = findViewById(R.id.no_past_flights_message);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Booking History");

        // Handle window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup navigation
        NavigationUtils.setupNavigation(this, R.id.nav_history);

        loadPastBookings();
    }

    private void loadPastBookings() {
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int userID = sharedPreferences.getInt(USER_ID, -1);
        if (userID == -1) {
            Toast.makeText(this, "Error: User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = ApiClient.getApiService();
        Call<List<Booking>> call = apiService.getBookings();
        call.enqueue(new Callback<List<Booking>>() {
            @Override
            public void onResponse(Call<List<Booking>> call, Response<List<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body();
                    Call<List<Flight>> flightsCall = apiService.getFlights();
                    flightsCall.enqueue(new Callback<List<Flight>>() {
                        @Override
                        public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                List<Flight> flights = response.body();
                                boolean hasPastFlights = false;

                                for (Booking booking : bookings) {
                                    if (booking.getUserID() == userID && "confirmed".equalsIgnoreCase(booking.getStatus())) {
                                        Flight flight = findFlightById(flights, booking.getFlightID());
                                        if (flight != null) {
                                            try {
                                                Date departureTime = flight.getDepartureTime();
                                                Date currentDate = new Date();
                                                if (departureTime.before(currentDate)) {
                                                    hasPastFlights = true;
                                                    pastFlightsContainer.addView(createPastFlightItem(booking, flight));
                                                }
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                if (!hasPastFlights) {
                                    noPastFlightsMessage.setVisibility(View.VISIBLE);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<List<Flight>> call, Throwable t) {
                            Toast.makeText(PastFlightsActivity.this, "Error loading flights", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<List<Booking>> call, Throwable t) {
                Toast.makeText(PastFlightsActivity.this, "Error loading bookings", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Flight findFlightById(List<Flight> flights, int flightID) {
        for (Flight flight : flights) {
            if (flight.getFlightID() == flightID) {
                return flight;
            }
        }
        return null;
    }

    private CardView createPastFlightItem(Booking booking, Flight flight) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
        CardView cardView = new CardView(this);
        ConstraintLayout layout = new ConstraintLayout(this);

        cardView.setRadius(12f);
        cardView.setCardElevation(6f);
        cardView.setCardBackgroundColor(getColor(android.R.color.white));

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(12, 12, 12, 12);
        cardView.setLayoutParams(layoutParams);
        layout.setPadding(24, 24, 24, 24);

        // Flight Number
        TextView flightNumber = new TextView(this);
        flightNumber.setId(View.generateViewId());
        flightNumber.setText("Flight No: " + flight.getFlightNumber());
        flightNumber.setTextSize(18f);
        flightNumber.setTextColor(getColor(android.R.color.black));
        flightNumber.setTypeface(null, android.graphics.Typeface.BOLD);

        ConstraintLayout.LayoutParams flightNumberLp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        flightNumberLp.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
        flightNumberLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        flightNumberLp.leftMargin = 8;
        layout.addView(flightNumber, flightNumberLp);

        // Departure Date
        TextView departureDate = new TextView(this);
        departureDate.setId(View.generateViewId());
        departureDate.setText(sdf.format(flight.getDepartureTime()));
        departureDate.setTextSize(14f);
        departureDate.setTextColor(getColor(android.R.color.darker_gray));

        ConstraintLayout.LayoutParams departureDateLp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        departureDateLp.startToStart = flightNumber.getId();
        departureDateLp.topToBottom = flightNumber.getId();
        departureDateLp.topMargin = 4;
        layout.addView(departureDate, departureDateLp);

        // Booking Date
        TextView bookingDate = new TextView(this);
        bookingDate.setId(View.generateViewId());
        bookingDate.setText(sdf.format(booking.getBookingDate()));
        bookingDate.setTextSize(14f);
        bookingDate.setTextColor(getColor(android.R.color.darker_gray));

        ConstraintLayout.LayoutParams bookingDateLp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        bookingDateLp.startToStart = departureDate.getId();
        bookingDateLp.topToBottom = departureDate.getId();
        bookingDateLp.topMargin = 4;
        layout.addView(bookingDate, bookingDateLp);

        // Seats Count
        TextView seatsCount = new TextView(this);
        seatsCount.setId(View.generateViewId());
        seatsCount.setText("Seats: " + booking.getAmount());
        seatsCount.setTextSize(14f);
        seatsCount.setTextColor(getColor(android.R.color.darker_gray));

        ConstraintLayout.LayoutParams seatsCountLp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        seatsCountLp.startToStart = bookingDate.getId();
        seatsCountLp.topToBottom = bookingDate.getId();
        seatsCountLp.topMargin = 4;
        layout.addView(seatsCount, seatsCountLp);

        // Price per Seat
        TextView pricePerSeat = new TextView(this);
        pricePerSeat.setId(View.generateViewId());
        pricePerSeat.setText(flight.getPrice() + " ₽/seat");
        pricePerSeat.setTextSize(14f);
        pricePerSeat.setTextColor(getColor(android.R.color.darker_gray));

        ConstraintLayout.LayoutParams pricePerSeatLp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        pricePerSeatLp.startToStart = seatsCount.getId();
        pricePerSeatLp.topToBottom = seatsCount.getId();
        pricePerSeatLp.topMargin = 4;
        layout.addView(pricePerSeat, pricePerSeatLp);

        // Total Price
        TextView totalPrice = new TextView(this);
        totalPrice.setId(View.generateViewId());
        totalPrice.setText((booking.getAmount() * flight.getPrice()) + " ₽");
        totalPrice.setTextSize(16f);
        totalPrice.setTextColor(getColor(android.R.color.black));
        totalPrice.setTypeface(null, android.graphics.Typeface.BOLD);

        ConstraintLayout.LayoutParams totalPriceLp = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
        );
        totalPriceLp.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
        totalPriceLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
        layout.addView(totalPrice, totalPriceLp);

        cardView.addView(layout);
        return cardView;
    }
}