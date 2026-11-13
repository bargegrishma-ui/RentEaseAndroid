package com.rentease.app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.Property;
import com.rentease.app.util.ImageUtil;
import com.rentease.app.util.Ui;

import java.util.List;

public class PropertyDetailsActivity extends AppCompatActivity {
    private Property p;
    private TextView details, tvDetailStatus, tvDetailName, tvDetailLocation, tvDetailRent;
    private TextView tvBeds, tvBaths, tvSqft;
    private ImageView ivPropertyImage, btnFavorite;
    private Button btnBook, btnCallOwner, btnEditProperty, btnExpireProperty, btnDeleteProperty;
    private DatabaseHelper dbHelper;
    private String propertyId;
    private boolean isFav = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_property_details);

        dbHelper = DatabaseHelper.getInstance(this);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        propertyId = getIntent().getStringExtra("propertyId");
        if (propertyId == null || propertyId.trim().isEmpty()) {
            propertyId = "1";
        }

        com.rentease.app.util.SessionManager sm = new com.rentease.app.util.SessionManager(this);
        String userId = sm.getUserId();
        android.content.SharedPreferences favPrefs = getSharedPreferences("RentEaseFavorites", MODE_PRIVATE);
        String favKey = "fav_" + (userId.isEmpty() ? "guest" : userId) + "_" + propertyId;
        isFav = favPrefs.getBoolean(favKey, false); // Default is NOT favorite

        btnFavorite = findViewById(R.id.btnFavorite);
        if (btnFavorite != null) {
            btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_red : R.drawable.ic_heart_gray);
            btnFavorite.setOnClickListener(v -> {
                isFav = !isFav;
                favPrefs.edit().putBoolean(favKey, isFav).apply();
                btnFavorite.setImageResource(isFav ? R.drawable.ic_heart_red : R.drawable.ic_heart_gray);
                Ui.toast(this, isFav ? "Added to Favorites" : "Removed from Favorites");
            });
        }

        details = findViewById(R.id.details);
        tvDetailName = findViewById(R.id.tvDetailName);
        tvDetailLocation = findViewById(R.id.tvDetailLocation);
        tvDetailRent = findViewById(R.id.tvDetailRent);
        tvBeds = findViewById(R.id.tvBeds);
        tvBaths = findViewById(R.id.tvBaths);
        tvSqft = findViewById(R.id.tvSqft);
        ivPropertyImage = findViewById(R.id.ivPropertyImage);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);

        btnBook = findViewById(R.id.btnBook);
        btnCallOwner = findViewById(R.id.btnCallOwner);
        btnEditProperty = findViewById(R.id.btnEditProperty);
        btnExpireProperty = findViewById(R.id.btnExpireProperty);
        btnDeleteProperty = findViewById(R.id.btnDeleteProperty);

        propertyId = getIntent().getStringExtra("propertyId");

        TextView tvMapAddress = findViewById(R.id.tvMapAddress);
        Button btnOpenMap = findViewById(R.id.btnOpenMap);
        Button btnChatOwner = findViewById(R.id.btnChatOwner);

        if (btnCallOwner != null) {
            btnCallOwner.setOnClickListener(v -> {
                new AlertDialog.Builder(this)
                        .setTitle("Contact Owner")
                        .setMessage("Owner Phone: +91 98765 43210\n\nWould you like to initiate a call?")
                        .setPositiveButton("Call", (dialog, which) -> {
                            try {
                                Intent callIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:+919876543210"));
                                startActivity(callIntent);
                            } catch (Exception e) {
                                Ui.toast(this, "Calling +91 98765 43210");
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        if (btnChatOwner != null) {
            btnChatOwner.setOnClickListener(v -> {
                Property prop = dbHelper.getPropertyById(propertyId);
                String propName = prop != null ? prop.name : "Property";
                String propLoc = prop != null ? prop.location : "";
                Intent chatIntent = new Intent(this, MessagesActivity.class);
                chatIntent.putExtra("initialMessage", "Hello, is \"" + propName + "\" in " + propLoc + " available for visit?");
                startActivity(chatIntent);
            });
        }

        View.OnClickListener mapClickListener = v -> {
            String loc = (p != null && p.location != null && !p.location.isEmpty()) ? p.location : "Mumbai, Maharashtra";
            Ui.toast(this, "📍 Opening Google Maps for " + loc + "...");
            try {
                Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(loc));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                startActivity(mapIntent);
            } catch (Exception e) {
                try {
                    Intent webMapIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://www.google.com/maps/search/?api=1&query=" + Uri.encode(loc)));
                    startActivity(webMapIntent);
                } catch (Exception ignored) {
                    Ui.toast(this, "Location: " + loc);
                }
            }
        };

        if (btnOpenMap != null) {
            btnOpenMap.setOnClickListener(mapClickListener);
        }
        View ivMap = findViewById(R.id.ivMapPreview);
        if (ivMap != null) {
            ivMap.setOnClickListener(mapClickListener);
        }

        btnBook.setOnClickListener(v -> {
            if (p == null) return;
            if (p.isExpired()) {
                Ui.toast(this, "This property listing is expired and cannot be booked");
                return;
            }
            Intent i = new Intent(this, BookingActivity.class);
            i.putExtra("propertyId", p.id);
            i.putExtra("propertyName", p.name);
            i.putExtra("rent", p.rent);
            startActivity(i);
        });

        btnEditProperty.setOnClickListener(v -> {
            if (p == null) return;
            Intent intent = new Intent(this, AddPropertyActivity.class);
            intent.putExtra("isEditMode", true);
            intent.putExtra("propertyId", p.id);
            intent.putExtra("name", p.name);
            intent.putExtra("location", p.location);
            intent.putExtra("type", p.type);
            intent.putExtra("rent", p.rent);
            intent.putExtra("description", p.description);
            intent.putExtra("imageUrl", p.imageUrl);
            intent.putExtra("status", p.status);
            startActivity(intent);
        });

        btnExpireProperty.setOnClickListener(v -> {
            if (p == null) return;
            boolean isExpired = p.isExpired();
            String title = isExpired ? "Activate Property" : "Expire Property";
            String msg = isExpired
                    ? "Do you want to re-activate \"" + p.name + "\" as available for rent?"
                    : "Do you want to mark \"" + p.name + "\" as Expired?";
            String confirmText = isExpired ? "Activate" : "Expire";

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(msg)
                    .setPositiveButton(confirmText, (dialog, which) -> {
                        String newStatus = isExpired ? "Active" : "Expired";
                        int updated = dbHelper.updatePropertyStatus(p.id, newStatus);
                        if (updated > 0) {
                            Ui.toast(this, "Property marked as " + newStatus);
                            loadProperty();
                        } else {
                            Ui.toast(this, "Failed to update property status");
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        btnDeleteProperty.setOnClickListener(v -> {
            if (p == null) return;
            new AlertDialog.Builder(this)
                    .setTitle("Delete Property")
                    .setMessage("Are you sure you want to delete \"" + p.name + "\"?")
                    .setPositiveButton("Delete", (dialog, which) -> {
                        int deleted = dbHelper.deleteProperty(p.id);
                        if (deleted > 0) {
                            Ui.toast(this, "Property deleted successfully");
                            finish();
                        } else {
                            Ui.toast(this, "Failed to delete property");
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProperty();
    }

    private void loadProperty() {
        if (propertyId != null && !propertyId.trim().isEmpty()) {
            p = dbHelper.getPropertyById(propertyId.trim());
        }

        String extraName = getIntent().getStringExtra("propertyName");
        if (p == null && extraName != null && !extraName.trim().isEmpty()) {
            p = dbHelper.getPropertyByName(extraName.trim());
        }

        if (p == null && extraName != null && !extraName.trim().isEmpty()) {
            String[] words = extraName.trim().split(" ");
            for (String w : words) {
                if (w.length() > 2) {
                    List<Property> matches = dbHelper.searchProperties(w);
                    if (!matches.isEmpty()) {
                        p = matches.get(0);
                        break;
                    }
                }
            }
        }

        if (p == null) {
            List<Property> all = dbHelper.getAllProperties();
            if (!all.isEmpty()) {
                p = all.get(0);
            }
        }

        if (p == null) {
            p = new Property();
            p.id = "1";
            p.name = extraName != null ? extraName : "2 BHK Apartment";
            p.location = "Powai, Mumbai";
            p.rent = "25,000";
            p.type = "2BHK";
            p.description = "Spacious modern apartment with dedicated parking, modular kitchen, and 24/7 security.";
            p.imageUrl = "preset:apartment";
            p.status = "Active";
        }

        if (tvDetailName != null) tvDetailName.setText(safe(p.name));
        if (tvDetailLocation != null) tvDetailLocation.setText("📍 " + safe(p.location));
        if (tvDetailRent != null) tvDetailRent.setText("₹" + safe(p.rent) + " / month");

        TextView tvMapAddress = findViewById(R.id.tvMapAddress);
        if (tvMapAddress != null) tvMapAddress.setText(safe(p.location) + ", Maharashtra");

        // Spec parsing (defaults based on type)
        String type = p.type != null ? p.type.toUpperCase() : "";
        if (tvBeds != null) {
            if (type.contains("1BHK")) tvBeds.setText("1 Bed");
            else if (type.contains("3BHK")) tvBeds.setText("3 Beds");
            else if (type.contains("STUDIO")) tvBeds.setText("1 Bed");
            else tvBeds.setText("2 Beds");
        }
        if (tvBaths != null) {
            if (type.contains("1BHK")) tvBaths.setText("1 Bath");
            else if (type.contains("3BHK")) tvBaths.setText("3 Baths");
            else if (type.contains("STUDIO")) tvBaths.setText("1 Bath");
            else tvBaths.setText("2 Baths");
        }
        if (tvSqft != null) {
            if (type.contains("1BHK")) tvSqft.setText("600 Sq Ft");
            else if (type.contains("3BHK")) tvSqft.setText("1400 Sq Ft");
            else if (type.contains("STUDIO")) tvSqft.setText("450 Sq Ft");
            else tvSqft.setText("950 Sq Ft");
        }

        if (p.imageUrl != null && !p.imageUrl.trim().isEmpty()) {
            ImageUtil.loadImage(ivPropertyImage, p.imageUrl);
        } else {
            ImageUtil.loadImage(ivPropertyImage, "preset:apartment");
        }

        boolean isExpired = p.isExpired();
        boolean isBooked = p.isBooked() || dbHelper.isPropertyBooked(p.id, p.name);

        if (tvDetailStatus != null) {
            if (isBooked) {
                tvDetailStatus.setText("Booked");
                tvDetailStatus.setBackgroundResource(R.drawable.bg_status_expired);
            } else if (isExpired) {
                tvDetailStatus.setText("Expired");
                tvDetailStatus.setBackgroundResource(R.drawable.bg_status_expired);
            } else {
                tvDetailStatus.setText("Active");
                tvDetailStatus.setBackgroundResource(R.drawable.bg_status_active);
            }
        }

        if (btnExpireProperty != null) {
            btnExpireProperty.setText(isExpired ? "Re-activate" : "Expire");
            btnExpireProperty.setTextColor(isExpired ? getColor(R.color.green) : getColor(R.color.amber));
            if (isBooked) {
                btnExpireProperty.setEnabled(false);
                btnExpireProperty.setAlpha(0.5f);
            } else {
                btnExpireProperty.setEnabled(true);
                btnExpireProperty.setAlpha(1.0f);
            }
        }

        if (btnBook != null) {
            if (isBooked) {
                btnBook.setText("Already Booked");
                btnBook.setEnabled(false);
                btnBook.setAlpha(0.6f);
            } else if (isExpired) {
                btnBook.setText("Listing Expired");
                btnBook.setEnabled(false);
                btnBook.setAlpha(0.6f);
            } else {
                btnBook.setText("Book Now");
                btnBook.setEnabled(true);
                btnBook.setAlpha(1.0f);
            }
        }

        if (details != null) {
            details.setText(p.description != null && !p.description.trim().isEmpty() 
                    ? p.description 
                    : "Spacious rental property with modern amenities, 24x7 water and electricity, dedicated parking, and prime neighborhood connectivity.");
        }
    }

    private String safe(String str) {
        return (str == null || str.trim().isEmpty()) ? "N/A" : str;
    }
}
