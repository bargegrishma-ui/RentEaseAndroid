package com.rentease.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.Booking;
import com.rentease.app.model.Property;
import com.rentease.app.util.ImageUtil;

import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking);
        void onViewProperty(Booking booking);
    }

    private final Context context;
    private final List<Booking> bookings;
    private final OnBookingActionListener listener;
    private final DatabaseHelper dbHelper;

    public BookingAdapter(Context context, List<Booking> bookings, OnBookingActionListener listener) {
        this.context = context;
        this.bookings = bookings;
        this.listener = listener;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking b = bookings.get(position);

        holder.tvBookingId.setText("📋 RESERVATION #BK-" + (b.id != null ? b.id : String.valueOf(position + 1)));
        holder.tvPropertyName.setText(b.propertyName != null ? b.propertyName : "Rental Property");
        holder.tvMoveInDate.setText("🗓️ Move-in: " + (b.moveInDate != null && !b.moveInDate.isEmpty() ? b.moveInDate : "Immediate"));

        String tenant = b.userName != null ? b.userName : "Guest Tenant";
        String phone = b.phone != null && !b.phone.isEmpty() ? " • 📞 " + b.phone : "";
        holder.tvTenant.setText("👤 " + tenant + phone);

        // Status pill
        String st = b.status != null ? b.status : "Pending";
        if (st.equalsIgnoreCase("Confirmed")) {
            holder.tvStatus.setText("🟢 Confirmed");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
        } else if (st.equalsIgnoreCase("Pending")) {
            holder.tvStatus.setText("🟡 Pending");
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending);
        } else {
            holder.tvStatus.setText(st);
            holder.tvStatus.setBackgroundResource(R.drawable.bg_status_expired);
        }

        // Payment Status Badge
        boolean isPaymentInfoOnly = false;
        if (holder.tvPayment != null) {
            if (b.message != null && b.message.contains("Paid")) {
                holder.tvPayment.setText("💳 " + b.message.substring(b.message.indexOf("Paid")));
                if (b.message.trim().startsWith("Paid")) {
                    isPaymentInfoOnly = true;
                }
            } else if (b.message != null && b.message.contains("Visit")) {
                holder.tvPayment.setText("💵 Token to be Paid on Visit");
                if (b.message.trim().startsWith("Pay Token") || b.message.trim().startsWith("Visit")) {
                    isPaymentInfoOnly = true;
                }
            } else {
                int bookingNum = 100 + position * 17 + 3;
                holder.tvPayment.setText("💳 Token Paid: ₹2,000 via UPI • TXN-BK" + bookingNum);
            }
        }

        // Custom Message (Only shown if tenant provided an additional note)
        if (b.message != null && !b.message.trim().isEmpty() && !isPaymentInfoOnly) {
            holder.layoutMessage.setVisibility(View.VISIBLE);
            holder.tvMessage.setText("💬 " + b.message.trim());
        } else {
            holder.layoutMessage.setVisibility(View.GONE);
        }

        // Load the actual real property photo and details
        Property prop = null;
        if (b.propertyId != null && !b.propertyId.trim().isEmpty()) {
            prop = dbHelper.getPropertyById(b.propertyId.trim());
        }
        if (prop == null && b.propertyName != null && !b.propertyName.trim().isEmpty()) {
            prop = dbHelper.getPropertyByName(b.propertyName.trim());
        }

        if (prop != null && prop.imageUrl != null && !prop.imageUrl.trim().isEmpty()) {
            ImageUtil.loadImage(holder.ivProperty, prop.imageUrl);
        } else if (b.propertyName != null && !b.propertyName.trim().isEmpty()) {
            ImageUtil.loadImage(holder.ivProperty, b.propertyName);
        } else {
            holder.ivProperty.setImageResource(R.drawable.prop_real_2bhk);
        }

        if (prop != null && prop.location != null && !prop.location.trim().isEmpty()) {
            holder.tvPropertyName.setText(b.propertyName + " (" + prop.location + ")");
        } else {
            holder.tvPropertyName.setText(b.propertyName != null ? b.propertyName : "Rental Property");
        }

        holder.btnViewProperty.setOnClickListener(v -> {
            if (listener != null) listener.onViewProperty(b);
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewProperty(b);
        });

        holder.btnCancelBooking.setOnClickListener(v -> {
            if (listener != null) listener.onCancelBooking(b);
        });
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    static class BookingViewHolder extends RecyclerView.ViewHolder {
        TextView tvBookingId, tvStatus, tvPropertyName, tvMoveInDate, tvTenant, tvMessage, tvPayment;
        ImageView ivProperty;
        View layoutMessage;
        Button btnCancelBooking, btnViewProperty;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBookingId = itemView.findViewById(R.id.tvBookingId);
            tvStatus = itemView.findViewById(R.id.tvBookingStatus);
            tvPropertyName = itemView.findViewById(R.id.tvBookingPropertyName);
            tvMoveInDate = itemView.findViewById(R.id.tvBookingMoveInDate);
            tvTenant = itemView.findViewById(R.id.tvBookingTenant);
            tvPayment = itemView.findViewById(R.id.tvBookingPayment);
            tvMessage = itemView.findViewById(R.id.tvBookingMessage);
            ivProperty = itemView.findViewById(R.id.ivBookingProperty);
            layoutMessage = itemView.findViewById(R.id.layoutMessage);
            btnCancelBooking = itemView.findViewById(R.id.btnCancelBooking);
            btnViewProperty = itemView.findViewById(R.id.btnViewProperty);
        }
    }
}
