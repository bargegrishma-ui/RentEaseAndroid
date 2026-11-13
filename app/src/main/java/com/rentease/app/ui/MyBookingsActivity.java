package com.rentease.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.Booking;
import com.rentease.app.util.NavigationHelper;
import com.rentease.app.util.SessionManager;
import com.rentease.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class MyBookingsActivity extends AppCompatActivity implements BookingAdapter.OnBookingActionListener {

    private RecyclerView recyclerView;
    private View layoutEmptyState;
    private TextView tvBookingCount;
    private BookingAdapter adapter;
    private DatabaseHelper dbHelper;
    private final List<Booking> bookingList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_bookings);

        dbHelper = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyclerViewBookings);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        tvBookingCount = findViewById(R.id.tvBookingCount);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BookingAdapter(this, bookingList, this);
        recyclerView.setAdapter(adapter);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> SessionManager.promptLogout(this));
        }

        Button btnExplore = findViewById(R.id.btnExploreProperties);
        if (btnExplore != null) {
            btnExplore.setOnClickListener(v -> {
                Intent i = new Intent(this, HomeActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(i);
                finish();
            });
        }

        NavigationHelper.setupBottomNav(this, NavigationHelper.TAB_BOOKINGS);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();
    }

    private void loadBookings() {
        bookingList.clear();
        SessionManager sessionManager = new SessionManager(this);
        String currentUserId = sessionManager.getUserId();
        String currentUserName = sessionManager.getUserName();

        List<Booking> userBookings = new ArrayList<>();
        if (currentUserId != null && !currentUserId.trim().isEmpty()) {
            userBookings = dbHelper.getBookingsByUserId(currentUserId.trim());
        }
        if (userBookings.isEmpty() && currentUserName != null && !currentUserName.trim().isEmpty()) {
            userBookings = dbHelper.getBookingsByUserName(currentUserName.trim());
        }
        if (userBookings.isEmpty()) {
            userBookings = dbHelper.getAllBookings();
        }
        bookingList.addAll(userBookings);
        adapter.notifyDataSetChanged();

        int count = bookingList.size();
        if (tvBookingCount != null) {
            tvBookingCount.setText(count + " active reservation" + (count == 1 ? "" : "s"));
        }

        if (bookingList.isEmpty()) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            layoutEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCancelBooking(Booking booking) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel the booking for \"" + (booking.propertyName != null ? booking.propertyName : "this property") + "\"?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {
                    int deleted = dbHelper.deleteBooking(booking.id);
                    if (deleted > 0) {
                        Ui.toast(this, "Booking cancelled successfully from SQLite!");
                        loadBookings();
                    } else {
                        Ui.toast(this, "Failed to cancel booking");
                    }
                })
                .setNegativeButton("Keep Booking", null)
                .show();
    }

    @Override
    public void onViewProperty(Booking booking) {
        if (booking == null) return;
        Intent intent = new Intent(this, PropertyDetailsActivity.class);
        if (booking.propertyId != null && !booking.propertyId.trim().isEmpty()) {
            intent.putExtra("propertyId", booking.propertyId.trim());
        }
        if (booking.propertyName != null && !booking.propertyName.trim().isEmpty()) {
            intent.putExtra("propertyName", booking.propertyName.trim());
        }
        startActivity(intent);
    }
}
