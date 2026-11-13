package com.rentease.app.util;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.rentease.app.R;
import com.rentease.app.ui.AddPropertyActivity;
import com.rentease.app.ui.BookingActivity;
import com.rentease.app.ui.HomeActivity;
import com.rentease.app.ui.MessagesActivity;
import com.rentease.app.ui.MyBookingsActivity;
import com.rentease.app.ui.ProfileActivity;
import com.rentease.app.ui.PropertyListActivity;

public class NavigationHelper {

    public static final int TAB_HOME = 0;
    public static final int TAB_BOOKINGS = 1;
    public static final int TAB_ADD = 2;
    public static final int TAB_MESSAGES = 3;
    public static final int TAB_PROFILE = 4;

    public static void setupBottomNav(Activity activity, int currentTab) {
        View bottomNav = activity.findViewById(R.id.bottomNavigation);
        if (bottomNav == null) return;

        View tabHome = activity.findViewById(R.id.tabHome);
        View tabBookings = activity.findViewById(R.id.tabBookings);
        View tabAdd = activity.findViewById(R.id.tabAdd);
        View tabMessages = activity.findViewById(R.id.tabMessages);
        View tabProfile = activity.findViewById(R.id.tabProfile);

        TextView tvHome = activity.findViewById(R.id.tvTabHome);
        TextView tvBookings = activity.findViewById(R.id.tvTabBookings);
        TextView tvAdd = activity.findViewById(R.id.tvTabAdd);
        TextView tvMessages = activity.findViewById(R.id.tvTabMessages);
        TextView tvProfile = activity.findViewById(R.id.tvTabProfile);

        ImageView ivHome = activity.findViewById(R.id.ivTabHome);
        ImageView ivBookings = activity.findViewById(R.id.ivTabBookings);
        ImageView ivAdd = activity.findViewById(R.id.ivTabAdd);
        ImageView ivMessages = activity.findViewById(R.id.ivTabMessages);
        ImageView ivProfile = activity.findViewById(R.id.ivTabProfile);

        int activeColor = ContextCompat.getColor(activity, R.color.green);
        int inactiveColor = ContextCompat.getColor(activity, R.color.text_gray);

        // Reset colors
        if (tvHome != null) tvHome.setTextColor(currentTab == TAB_HOME ? activeColor : inactiveColor);
        if (tvBookings != null) tvBookings.setTextColor(currentTab == TAB_BOOKINGS ? activeColor : inactiveColor);
        if (tvAdd != null) tvAdd.setTextColor(currentTab == TAB_ADD ? activeColor : inactiveColor);
        if (tvMessages != null) tvMessages.setTextColor(currentTab == TAB_MESSAGES ? activeColor : inactiveColor);
        if (tvProfile != null) tvProfile.setTextColor(currentTab == TAB_PROFILE ? activeColor : inactiveColor);

        if (ivHome != null) ivHome.setColorFilter(currentTab == TAB_HOME ? activeColor : inactiveColor);
        if (ivBookings != null) ivBookings.setColorFilter(currentTab == TAB_BOOKINGS ? activeColor : inactiveColor);
        if (ivAdd != null) ivAdd.setColorFilter(currentTab == TAB_ADD ? activeColor : inactiveColor);
        if (ivMessages != null) ivMessages.setColorFilter(currentTab == TAB_MESSAGES ? activeColor : inactiveColor);
        if (ivProfile != null) ivProfile.setColorFilter(currentTab == TAB_PROFILE ? activeColor : inactiveColor);

        // Click listeners
        if (tabHome != null) {
            tabHome.setOnClickListener(v -> {
                if (currentTab != TAB_HOME) {
                    Intent i = new Intent(activity, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    activity.startActivity(i);
                }
            });
        }

        if (tabBookings != null) {
            tabBookings.setOnClickListener(v -> {
                if (currentTab != TAB_BOOKINGS) {
                    Intent i = new Intent(activity, MyBookingsActivity.class);
                    activity.startActivity(i);
                }
            });
        }

        if (tabAdd != null) {
            tabAdd.setOnClickListener(v -> {
                if (currentTab != TAB_ADD) {
                    Intent i = new Intent(activity, AddPropertyActivity.class);
                    activity.startActivity(i);
                }
            });
        }

        if (tabMessages != null) {
            tabMessages.setOnClickListener(v -> {
                if (currentTab != TAB_MESSAGES) {
                    Intent i = new Intent(activity, MessagesActivity.class);
                    activity.startActivity(i);
                }
            });
        }

        if (tabProfile != null) {
            tabProfile.setOnClickListener(v -> {
                if (currentTab != TAB_PROFILE) {
                    Intent i = new Intent(activity, ProfileActivity.class);
                    activity.startActivity(i);
                }
            });
        }
    }
}
