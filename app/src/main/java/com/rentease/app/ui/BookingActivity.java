package com.rentease.app.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.Booking;
import com.rentease.app.model.Property;
import com.rentease.app.model.User;
import com.rentease.app.util.ImageUtil;
import com.rentease.app.util.SessionManager;
import com.rentease.app.util.Ui;

import java.util.Calendar;
import java.util.Locale;

public class BookingActivity extends AppCompatActivity {
    private EditText date, name, phone, msg;
    private String propertyId, propertyName, rent;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_booking);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        propertyId = getIntent().getStringExtra("propertyId");
        propertyName = getIntent().getStringExtra("propertyName");
        rent = getIntent().getStringExtra("rent");

        TextView tvProperty = findViewById(R.id.property);
        TextView tvPrice = findViewById(R.id.tvBookingPrice);
        TextView tvLoc = findViewById(R.id.tvBookingLocation);
        ImageView ivBookingProperty = findViewById(R.id.ivBookingProperty);

        if (tvProperty != null) tvProperty.setText(propertyName != null ? propertyName : "2 BHK Apartment");
        if (tvPrice != null) tvPrice.setText("₹" + (rent != null ? rent : "25,000") + " / month");

        if (propertyId != null) {
            Property prop = dbHelper.getPropertyById(propertyId);
            if (prop != null) {
                if (tvLoc != null) tvLoc.setText("📍 " + (prop.location != null ? prop.location : "Mumbai"));
                if (ivBookingProperty != null && prop.imageUrl != null) {
                    ImageUtil.loadImage(ivBookingProperty, prop.imageUrl);
                }
            }
        }

        // Prevent Double Booking - Check if already booked
        if (dbHelper.isPropertyBooked(propertyId, propertyName)) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Already Booked")
                    .setMessage("This property (\"" + (propertyName != null ? propertyName : "Selected Property") + "\") has already been booked!\n\nDouble booking is strictly not allowed.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .setPositiveButton("Go Back", (dialog, which) -> finish())
                    .show();
            return;
        }

        date = findViewById(R.id.date);
        name = findViewById(R.id.name);
        phone = findViewById(R.id.phone);
        msg = findViewById(R.id.msg);

        // Date picker dialog
        View layoutDatePicker = findViewById(R.id.layoutDatePicker);
        View.OnClickListener dateClickListener = v -> {
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d1) -> {
                String formattedDate = String.format(Locale.getDefault(), "%02d/%02d/%04d", d1, m + 1, y);
                date.setText(formattedDate);
            }, year, month, day);
            dpd.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            dpd.show();
        };

        if (layoutDatePicker != null) layoutDatePicker.setOnClickListener(dateClickListener);
        if (date != null) date.setOnClickListener(dateClickListener);

        // Pre-fill date with tomorrow by default
        Calendar defaultDate = Calendar.getInstance();
        defaultDate.add(Calendar.DAY_OF_MONTH, 1);
        if (date != null && date.getText().toString().trim().isEmpty()) {
            date.setText(String.format(Locale.getDefault(), "%02d/%02d/%04d",
                    defaultDate.get(Calendar.DAY_OF_MONTH),
                    defaultDate.get(Calendar.MONTH) + 1,
                    defaultDate.get(Calendar.YEAR)));
        }

        if (sessionManager.isLoggedIn()) {
            name.setText(sessionManager.getUserName());
            User u = dbHelper.getUserByEmail(sessionManager.getUserEmail());
            if (u != null && u.getPhone() != null && !u.getPhone().isEmpty()) {
                phone.setText(u.getPhone());
            }
        }

        RadioGroup rgPayment = findViewById(R.id.rgPaymentMethod);
        Button btnSend = findViewById(R.id.btnSend);
        TextView tvTotal = findViewById(R.id.tvTotalPayable);

        if (rgPayment != null && btnSend != null) {
            rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rbUpi) {
                    btnSend.setText("⚡ Pay ₹2,000 via UPI & Confirm");
                    if (tvTotal != null) tvTotal.setText("₹2,000 (UPI)");
                    Ui.toast(this, "Selected: UPI (Google Pay / PhonePe)");
                } else if (checkedId == R.id.rbCard) {
                    btnSend.setText("💳 Pay ₹2,000 via Card & Confirm");
                    if (tvTotal != null) tvTotal.setText("₹2,000 (Card)");
                    Ui.toast(this, "Selected: Debit / Credit Card");
                } else if (checkedId == R.id.rbNetBanking) {
                    btnSend.setText("🏦 Pay ₹2,000 via NetBanking & Confirm");
                    if (tvTotal != null) tvTotal.setText("₹2,000 (NetBanking)");
                    Ui.toast(this, "Selected: Net Banking (All Banks)");
                } else if (checkedId == R.id.rbCashVisit) {
                    btnSend.setText("💵 Confirm Booking (Pay on Visit)");
                    if (tvTotal != null) tvTotal.setText("₹2,000 (Pay on Visit)");
                    Ui.toast(this, "Selected: Pay Token on Physical Visit");
                }
            });
        }

        if (btnSend != null) {
            btnSend.setOnClickListener(v -> book());
        }
    }

    private void book() {
        String n = name.getText().toString().trim();
        String p = phone.getText().toString().trim();
        String d = date.getText().toString().trim();
        String m = msg.getText().toString().trim();

        if (n.isEmpty()) {
            name.setError("Please enter your name");
            name.requestFocus();
            Ui.toast(this, "Please enter your name");
            return;
        }
        if (p.isEmpty()) {
            phone.setError("Please enter phone number");
            phone.requestFocus();
            Ui.toast(this, "Please enter your phone number");
            return;
        }
        if (d.isEmpty()) {
            date.setError("Please select move-in date");
            Ui.toast(this, "Please select move-in date");
            return;
        }

        // Double Booking Prevention
        if (dbHelper.isPropertyBooked(propertyId, propertyName)) {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Already Booked")
                    .setMessage("This property has already been booked. Double booking is strictly not allowed!")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setCancelable(false)
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
            return;
        }

        // Determine Payment Method
        RadioGroup rg = findViewById(R.id.rgPaymentMethod);
        int checkedId = rg != null ? rg.getCheckedRadioButtonId() : R.id.rbUpi;
        String paymentMethod;
        String txnId = "TXN-" + (100000 + (int)(Math.random() * 900000));
        String paymentBadge;

        if (checkedId == R.id.rbCard) {
            paymentMethod = "Debit/Credit Card";
            paymentBadge = "Paid: ₹2,000 Token via Card (" + txnId + ")";
        } else if (checkedId == R.id.rbNetBanking) {
            paymentMethod = "Net Banking";
            paymentBadge = "Paid: ₹2,000 Token via NetBanking (" + txnId + ")";
        } else if (checkedId == R.id.rbCashVisit) {
            paymentMethod = "Pay on Physical Visit";
            paymentBadge = "Pay Token on Physical Visit (₹2,000)";
        } else {
            paymentMethod = "UPI (Google Pay / PhonePe)";
            paymentBadge = "Paid: ₹2,000 Token via UPI (" + txnId + ")";
        }

        String fullMessage = m.isEmpty() ? paymentBadge : (m + " | " + paymentBadge);

        String uid = sessionManager.getUserId();
        if (uid == null || uid.trim().isEmpty()) {
            uid = "user_rohit";
        }

        Booking booking = new Booking();
        booking.propertyId = propertyId;
        booking.propertyName = propertyName;
        booking.userId = uid;
        booking.userName = n;
        booking.phone = p;
        booking.moveInDate = d;
        booking.message = fullMessage;
        booking.status = "Confirmed";

        long result = dbHelper.insertBooking(booking);
        if (result > 0) {
            showReceiptDialog(n, p, d, paymentMethod, txnId);
        } else {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle("Booking Failed")
                    .setMessage("This property has already been booked or is no longer available. Double booking is not allowed.")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .show();
        }
    }

    private void showReceiptDialog(String tenantName, String tenantPhone, String moveInDate,
                                   String paymentMethod, String txnId) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_payment_receipt, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(false)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvReceiptTxnId = dialogView.findViewById(R.id.tvReceiptTxnId);
        TextView tvReceiptProperty = dialogView.findViewById(R.id.tvReceiptProperty);
        TextView tvReceiptMoveIn = dialogView.findViewById(R.id.tvReceiptMoveIn);
        TextView tvReceiptTenant = dialogView.findViewById(R.id.tvReceiptTenant);
        TextView tvReceiptPhone = dialogView.findViewById(R.id.tvReceiptPhone);
        TextView tvReceiptAmount = dialogView.findViewById(R.id.tvReceiptAmount);
        TextView tvReceiptMethod = dialogView.findViewById(R.id.tvReceiptMethod);
        Button btnReceiptViewBookings = dialogView.findViewById(R.id.btnReceiptViewBookings);
        Button btnReceiptClose = dialogView.findViewById(R.id.btnReceiptClose);

        if (tvReceiptTxnId != null) tvReceiptTxnId.setText("Txn ID: " + txnId);
        if (tvReceiptProperty != null) tvReceiptProperty.setText(propertyName != null ? propertyName : "Rental Property");
        if (tvReceiptMoveIn != null) tvReceiptMoveIn.setText("🗓️ Move-in Date: " + moveInDate);
        if (tvReceiptTenant != null) tvReceiptTenant.setText("👤 " + tenantName);
        if (tvReceiptPhone != null) tvReceiptPhone.setText("📞 " + tenantPhone);
        if (tvReceiptAmount != null) tvReceiptAmount.setText("₹2,000");
        if (tvReceiptMethod != null) tvReceiptMethod.setText("Payment Mode: " + paymentMethod);

        if (btnReceiptViewBookings != null) {
            btnReceiptViewBookings.setOnClickListener(v -> {
                dialog.dismiss();
                android.content.Intent intent = new android.content.Intent(this, MyBookingsActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (btnReceiptClose != null) {
            btnReceiptClose.setOnClickListener(v -> {
                dialog.dismiss();
                finish();
            });
        }

        dialog.show();
    }
}
