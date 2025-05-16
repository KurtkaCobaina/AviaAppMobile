package com.example.aviaappmobile;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

interface ApiService {

    // Airports
    @GET("airports")
    Call<List<Airport>> getAirports();

    @POST("airports")
    Call<Void> createAirport(@Body Airport airport);

    @PUT("airports/{id}")
    Call<Void> updateAirport(@Path("id") int id, @Body Airport airport);

    @DELETE("airports/{id}")
    Call<Void> deleteAirport(@Path("id") int id);

    // Bookings
    @GET("bookings")
    Call<List<Booking>> getBookings();

    @POST("bookings")
    Call<Void> createBooking(@Body Booking booking);

    @PUT("bookings/{id}")
    Call<Void> updateBooking(@Path("id") int id, @Body Booking booking);

    @DELETE("bookings/{id}")
    Call<Void> deleteBooking(@Path("id") int id);

    // Flights
    @GET("flights")
    Call<List<Flight>> getFlights();

    @POST("flights")
    Call<Void> createFlight(@Body Flight flight);

    @PUT("flights/{id}")
    Call<Void> updateFlight(@Path("id") int id, @Body Flight flight);

    @DELETE("flights/{id}")
    Call<Void> deleteFlight(@Path("id") int id);

    // PaymentCard
    @GET("paymentcard")
    Call<List<PaymentCard>> getPaymentCards();



    @POST("/paymentcard")
    Call<Map<String, Object>> createPaymentCard(@Body PaymentCard paymentCard);

    @PUT("paymentcard/{id}")
    Call<Void> updatePaymentCard(@Path("id") String id, @Body PaymentCard paymentCard);

    @DELETE("paymentcard/{id}")
    Call<Void> deletePaymentCard(@Path("id") String id);

    // Payments
    @GET("payments")
    Call<List<Payment>> getPayments();

    @POST("payments")
    Call<Void> createPayment(@Body Payment payment);

    @PUT("payments/{id}")
    Call<Void> updatePayment(@Path("id") int id, @Body Payment payment);

    @DELETE("payments/{id}")
    Call<Void> deletePayment(@Path("id") int id);

    // Users
    @GET("users")
    Call<List<User>> getUsers();

    @POST("users")
    Call<Void> createUser(@Body User user);

    @PUT("users/{id}")
    Call<Void> updateUser(@Path("id") int id, @Body User user);

    @DELETE("users/{id}")
    Call<Void> deleteUser(@Path("id") int id);
    @PUT("users/update-password") // Убираем начальный слэш
    Call<Void> resetPassword(@Body Map<String, String> body);

    @PUT("flights/{flightID}/seats")
    Call<Void> updateFlightSeats(@Path("flightID") int flightID, @Body Map<String, Integer> body);
    @PUT("/bookings/{id}/status")
    Call<Void> updateBookingStatus(@Path("id") int bookingID, @Body Map<String, String> status);
    @PUT("users/{id}/password")
    Call<Void> updatePassword(
            @Path("id") int userId,
            @Body Map<String, String> passwordData
    );
}
