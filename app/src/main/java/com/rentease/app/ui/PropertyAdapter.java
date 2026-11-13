package com.rentease.app.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rentease.app.R;
import com.rentease.app.model.Property;
import com.rentease.app.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    public interface OnPropertyActionListener {
        void onViewDetails(Property property);
        void onEdit(Property property);
        void onExpire(Property property);
        void onDelete(Property property);
    }

    private final Context context;
    private List<Property> propertyList;
    private final OnPropertyActionListener listener;

    public PropertyAdapter(Context context, List<Property> propertyList, OnPropertyActionListener listener) {
        this.context = context;
        this.propertyList = propertyList != null ? propertyList : new ArrayList<>();
        this.listener = listener;
    }

    public void updateList(List<Property> newList) {
        this.propertyList = newList != null ? newList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        Property property = propertyList.get(position);

        holder.tvName.setText(safe(property.name));
        holder.tvLocation.setText("📍 " + safe(property.location));
        holder.tvType.setText(safe(property.type));
        holder.tvRent.setText("₹" + safe(property.rent) + " / month");

        if (holder.tvStatus != null) {
            if (property.isExpired()) {
                holder.tvStatus.setText("Expired");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_expired);
            } else {
                holder.tvStatus.setText("Active");
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_active);
            }
        }

        if (property.imageUrl != null && !property.imageUrl.trim().isEmpty()) {
            holder.layoutCardImage.setVisibility(View.VISIBLE);
            ImageUtil.loadImage(holder.ivCardImage, property.imageUrl);
        } else {
            holder.layoutCardImage.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(property);
        });

        holder.btnViewDetails.setOnClickListener(v -> {
            if (listener != null) listener.onViewDetails(property);
        });

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(property);
        });

        if (holder.btnExpire != null) {
            if (property.isExpired()) {
                holder.btnExpire.setText("Activate");
                holder.btnExpire.setTextColor(context.getColor(R.color.green));
            } else {
                holder.btnExpire.setText("Expire");
                holder.btnExpire.setTextColor(0xFFD32F2F);
            }
            holder.btnExpire.setOnClickListener(v -> {
                if (listener != null) listener.onExpire(property);
            });
        }

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(property);
        });
    }

    @Override
    public int getItemCount() {
        return propertyList.size();
    }

    private String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "N/A" : str;
    }

    public static class PropertyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvLocation, tvType, tvRent, tvStatus;
        Button btnViewDetails, btnEdit, btnExpire, btnDelete;
        FrameLayout layoutCardImage;
        ImageView ivCardImage;

        public PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvPropertyName);
            tvLocation = itemView.findViewById(R.id.tvPropertyLocation);
            tvType = itemView.findViewById(R.id.tvPropertyType);
            tvRent = itemView.findViewById(R.id.tvPropertyRent);
            tvStatus = itemView.findViewById(R.id.tvPropertyStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnExpire = itemView.findViewById(R.id.btnExpire);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            layoutCardImage = itemView.findViewById(R.id.layoutCardImage);
            ivCardImage = itemView.findViewById(R.id.ivCardImage);
        }
    }
}
