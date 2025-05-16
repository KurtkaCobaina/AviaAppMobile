package com.example.aviaappmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private EditText departureCityInput;
    private EditText arrivalCityInput;
    private EditText departureDateInput;
    private EditText passengerCountInput;

    // Regular expressions for validation
    private static final Pattern ONLY_LETTERS_PATTERN = Pattern.compile("^[a-zA-Zа-яА-Я\\s]+$"); // Letters and spaces only
    private static final Pattern DATE_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$"); // Date format YYYY-MM-DD
    public static Pattern getOnlyLettersPattern() {
        return ONLY_LETTERS_PATTERN;
    }

    public static Pattern getDatePattern() {
        return DATE_PATTERN;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize input fields
        departureCityInput = findViewById(R.id.departure_city);
        arrivalCityInput = findViewById(R.id.arrival_city);
        departureDateInput = findViewById(R.id.departure_date);
        passengerCountInput = findViewById(R.id.passenger_count);

        // Navigation
        findViewById(R.id.nav_login).setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class)));
        findViewById(R.id.nav_home).setOnClickListener(v -> startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_bookings).setOnClickListener(v -> startActivity(new Intent(this, BookingActivity.class)));
        findViewById(R.id.nav_history).setOnClickListener(v -> startActivity(new Intent(this, HistoryActivity.class)));
        // Setup navigation
        NavigationUtils.setupNavigation(this, R.id.nav_home);
        // "Search for Tickets" button handler
        findViewById(R.id.search_button).setOnClickListener(v -> {
            String departureCity = departureCityInput.getText().toString().trim();
            String arrivalCity = arrivalCityInput.getText().toString().trim();
            String departureDate = departureDateInput.getText().toString().trim();
            String passengerCountStr = passengerCountInput.getText().toString().trim();

            // Validate required fields
            if (departureCity.isEmpty()) {
                Toast.makeText(this, "Enter departure city", Toast.LENGTH_SHORT).show();
                return;
            }
            // Validate no digits in city names
            if (!ONLY_LETTERS_PATTERN.matcher(departureCity).matches()) {
                Toast.makeText(this, "Departure city should contain only letters", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!arrivalCity.isEmpty() && !ONLY_LETTERS_PATTERN.matcher(arrivalCity).matches()) {
                Toast.makeText(this, "Arrival city should contain only letters", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate date format
            if (!departureDate.isEmpty() && !DATE_PATTERN.matcher(departureDate).matches()) {
                Toast.makeText(this, "Invalid date format. Use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate required fields
            if (departureCity.isEmpty()) {
                Toast.makeText(this, "Enter departure city", Toast.LENGTH_SHORT).show();
                return;
            }
            if (passengerCountStr.isEmpty()) {
                Toast.makeText(this, "Enter number of passengers", Toast.LENGTH_SHORT).show();
                return;
            }

            int passengerCount;
            try {
                passengerCount = Integer.parseInt(passengerCountStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number of passengers", Toast.LENGTH_SHORT).show();
                return;
            }

            // Добавленное ограничение: нельзя выбрать больше 10 пассажиров
            if (passengerCount > 10) {
                Toast.makeText(this, "You cannot book more than 10 tickets", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call API to search for flights
            fetchAirportsAndFlights(departureCity, arrivalCity, departureDate, passengerCount);
        });
    }

    // Method to fetch airports and flights
    private void fetchAirportsAndFlights(String departureCity, String arrivalCity, String departureDate, int passengerCount) {
        ApiService apiService = ApiClient.getApiService();

        // Fetch list of airports
        Call<List<Airport>> airportsCall = apiService.getAirports();
        airportsCall.enqueue(new Callback<List<Airport>>() {
            @Override
            public void onResponse(Call<List<Airport>> call, Response<List<Airport>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Airport> airports = response.body();
                    if (!airports.isEmpty()) {
                        // Create a map for quick airport lookup by ID
                        Map<Integer, String> airportCityMap = new HashMap<>();
                        for (Airport airport : airports) {
                            airportCityMap.put(airport.getAirportID(), airport.getCity());
                        }

                        // Fetch list of flights
                        Call<List<Flight>> flightsCall = apiService.getFlights();
                        flightsCall.enqueue(new Callback<List<Flight>>() {
                            @Override
                            public void onResponse(Call<List<Flight>> call, Response<List<Flight>> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    List<Flight> allFlights = response.body();
                                    if (!allFlights.isEmpty()) {
                                        // Filter flights
                                        List<Flight> filteredFlights = filterFlights(allFlights, airportCityMap, departureCity, arrivalCity, departureDate, passengerCount);

                                        if (!filteredFlights.isEmpty()) {
                                            // Pass data to SearchResultActivity
                                            Intent intent = new Intent(MainActivity.this, SearchResultActivity.class);
                                            intent.putExtra("flights", new ArrayList<>(filteredFlights)); // Use Serializable
                                            intent.putExtra("seatsCount", passengerCount); // Add number of seats
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(MainActivity.this, "No flights found", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(MainActivity.this, "No flights found", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Error fetching flights", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<List<Flight>> call, Throwable t) {
                                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(MainActivity.this, "No airports found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error fetching airports", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Airport>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to filter flights
    private List<Flight> filterFlights(List<Flight> flights, Map<Integer, String> airportCityMap,
                                       String departureCity, String arrivalCity, String departureDate, int passengerCount) {
        List<Flight> filteredFlights = new ArrayList<>();

        // Get current date
        Date currentDate = new Date();

        for (Flight flight : flights) {
            // Get departure and arrival cities from the map
            String flightDepartureCity = airportCityMap.get(flight.getDepartureAirportID());
            String flightArrivalCity = airportCityMap.get(flight.getArrivalAirportID());

            boolean matchesDepartureCity = flightDepartureCity != null && flightDepartureCity.equalsIgnoreCase(departureCity);
            boolean matchesArrivalCity = arrivalCity.isEmpty() || (flightArrivalCity != null && flightArrivalCity.equalsIgnoreCase(arrivalCity));
            boolean matchesDepartureDate = departureDate.isEmpty() || DateFormat.format("yyyy-MM-dd", flight.getDepartureTime()).toString().equals(departureDate);
            boolean matchesPassengerCount = flight.getSeatsAvailable() >= passengerCount;

            // Check that departure date is in the future
            boolean isFutureDate = flight.getDepartureTime().after(currentDate);

            // Add flight only if all conditions are met
            if (matchesDepartureCity && matchesArrivalCity && matchesDepartureDate && matchesPassengerCount && isFutureDate) {
                filteredFlights.add(flight);
            }
        }

        return filteredFlights;
    }
}