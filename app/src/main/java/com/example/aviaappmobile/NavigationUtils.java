package com.example.aviaappmobile;

import android.app.Activity;
import android.content.Intent;
import android.widget.ImageButton;

public class NavigationUtils {

    public static void setupNavigation(Activity activity, int activeNavId) {
        // Находим кнопки навигации
        ImageButton navHome = activity.findViewById(R.id.nav_home);
        ImageButton navLogin = activity.findViewById(R.id.nav_login);
        ImageButton navBookings = activity.findViewById(R.id.nav_bookings);
        ImageButton navHistory = activity.findViewById(R.id.nav_history);

        // Сбрасываем состояние всех кнопок
        navHome.setSelected(false);
        navLogin.setSelected(false);
        navBookings.setSelected(false);
        navHistory.setSelected(false);

        // Устанавливаем активную кнопку
        if (activeNavId == R.id.nav_home) {
            navHome.setSelected(true);
        } else if (activeNavId == R.id.nav_login) {
            navLogin.setSelected(true);
        } else if (activeNavId == R.id.nav_bookings) {
            navBookings.setSelected(true);
        } else if (activeNavId == R.id.nav_history) {
            navHistory.setSelected(true);
        }

        // Настраиваем обработчики кликов
        navHome.setOnClickListener(v -> {
            if (!(activity instanceof MainActivity)) {
                activity.startActivity(new Intent(activity, MainActivity.class));
                activity.finish();
            }
        });

        navLogin.setOnClickListener(v -> {
            if (!(activity instanceof LoginActivity) &&
                    !(activity instanceof ProfileActivity) &&
                    !(activity instanceof RegistrationActivity) &&
                    !(activity instanceof PasswordChangeActivity) &&
                    !(activity instanceof SupportActivity)) {
                activity.startActivity(new Intent(activity, LoginActivity.class));
                activity.finish();
            }
        });

        navBookings.setOnClickListener(v -> {
            if (!(activity instanceof BookingActivity) &&
                    !(activity instanceof SearchResultActivity) &&
                    !(activity instanceof PaymentActivity)) {
                activity.startActivity(new Intent(activity, BookingActivity.class));
                activity.finish();
            }
        });

        navHistory.setOnClickListener(v -> {
            if (!(activity instanceof HistoryActivity)) {
                activity.startActivity(new Intent(activity, HistoryActivity.class));
                activity.finish();
            }
        });
    }
}