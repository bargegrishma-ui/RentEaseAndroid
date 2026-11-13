package com.rentease.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.Property;
import com.rentease.app.util.SessionManager;
import com.rentease.app.util.Ui;

import java.util.ArrayList;
import java.util.List;

public class PropertyListActivity extends AppCompatActivity implements PropertyAdapter.OnPropertyActionListener {

    private RecyclerView recyclerView;
    private TextView tvEmptyState;
    private PropertyAdapter adapter;
    private DatabaseHelper dbHelper;
    private final List<Property> propertyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_list);

        dbHelper = DatabaseHelper.getInstance(this);

        recyclerView = findViewById(R.id.recyclerView);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PropertyAdapter(this, propertyList, this);
        recyclerView.setAdapter(adapter);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        findViewById(R.id.btnAddProperty).setOnClickListener(v -> {
            startActivity(new Intent(this, AddPropertyActivity.class));
        });

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> SessionManager.promptLogout(this));
        }

        com.rentease.app.util.NavigationHelper.setupBottomNav(this, -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProperties();
    }

    private void loadProperties() {
        propertyList.clear();
        SessionManager sessionManager = new SessionManager(this);
        String currentUserId = sessionManager.getUserId();
        if (currentUserId != null && !currentUserId.trim().isEmpty()) {
            propertyList.addAll(dbHelper.getPropertiesByOwner(currentUserId.trim()));
        }
        adapter.notifyDataSetChanged();

        if (propertyList.isEmpty()) {
            if (tvEmptyState != null) {
                tvEmptyState.setText("No properties listed yet.\nTap '+ Add Property' above to post your property!");
                tvEmptyState.setVisibility(View.VISIBLE);
            }
            recyclerView.setVisibility(View.GONE);
        } else {
            if (tvEmptyState != null) tvEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onViewDetails(Property property) {
        Intent intent = new Intent(this, PropertyDetailsActivity.class);
        intent.putExtra("propertyId", property.id);
        startActivity(intent);
    }

    @Override
    public void onEdit(Property property) {
        Intent intent = new Intent(this, AddPropertyActivity.class);
        intent.putExtra("isEditMode", true);
        intent.putExtra("propertyId", property.id);
        intent.putExtra("name", property.name);
        intent.putExtra("location", property.location);
        intent.putExtra("type", property.type);
        intent.putExtra("rent", property.rent);
        intent.putExtra("description", property.description);
        intent.putExtra("imageUrl", property.imageUrl);
        intent.putExtra("status", property.status);
        startActivity(intent);
    }

    @Override
    public void onExpire(Property property) {
        boolean isExpired = property.isExpired();
        String actionTitle = isExpired ? "Activate Property" : "Expire Property";
        String actionMsg = isExpired
                ? "Do you want to re-activate \"" + property.name + "\" as available for rent?"
                : "Do you want to mark \"" + property.name + "\" as Expired?";
        String confirmBtn = isExpired ? "Activate" : "Expire";

        new AlertDialog.Builder(this)
                .setTitle(actionTitle)
                .setMessage(actionMsg)
                .setPositiveButton(confirmBtn, (dialog, which) -> {
                    String newStatus = isExpired ? "Active" : "Expired";
                    int updated = dbHelper.updatePropertyStatus(property.id, newStatus);
                    if (updated > 0) {
                        Ui.toast(this, "Property marked as " + newStatus);
                        loadProperties();
                    } else {
                        Ui.toast(this, "Failed to update property status");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDelete(Property property) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Property")
                .setMessage("Are you sure you want to delete \"" + property.name + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int deleted = dbHelper.deleteProperty(property.id);
                    if (deleted > 0) {
                        Ui.toast(this, "Property deleted successfully");
                        loadProperties();
                    } else {
                        Ui.toast(this, "Failed to delete property");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
