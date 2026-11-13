package com.rentease.app.ui;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.Property;
import com.rentease.app.util.ImageUtil;
import com.rentease.app.util.NavigationHelper;
import com.rentease.app.util.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private final List<Property> properties = new ArrayList<>();
    private LinearLayout list;
    private EditText search;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private TextView tvGreeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        sessionManager = new SessionManager(this);
        if (!sessionManager.isLoggedIn() || sessionManager.isSessionExpired()) {
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        dbHelper = DatabaseHelper.getInstance(this);

        list = findViewById(R.id.propertyList);
        search = findViewById(R.id.search);
        
        tvGreeting = findViewById(R.id.tvGreeting);
        if (tvGreeting != null) {
            String name = sessionManager.getUserName();
            if (name != null && !name.trim().isEmpty()) {
                tvGreeting.setText("Hello, " + name + " 👋");
            }
        }

        View btnLogout = findViewById(R.id.btnLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> SessionManager.promptLogout(this));
        }



        View btnMenu = findViewById(R.id.btnMenu);
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v -> startActivity(new Intent(this, DrawerActivity.class)));
        }

        View btnViewAll = findViewById(R.id.btnViewAll);
        if (btnViewAll != null) {
            btnViewAll.setOnClickListener(v -> {
                if (search != null) search.setText("");
                render("");
            });
        }

        // Setup 5-tab Bottom Navigation
        NavigationHelper.setupBottomNav(this, NavigationHelper.TAB_HOME);

        if (search != null) {
            search.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { render(s.toString()); }
                @Override public void afterTextChanged(Editable s) { }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProperties();
    }

    private void loadProperties() {
        properties.clear();
        properties.addAll(dbHelper.getAllProperties());
        render(search != null && search.getText() != null ? search.getText().toString() : "");
    }

    private void render(String query) {
        list.removeAllViews();
        String q = query == null ? "" : query.trim().toLowerCase();

        for (Property p : properties) {
            String searchable = ((p.name == null ? "" : p.name) + " "
                    + (p.location == null ? "" : p.location) + " "
                    + (p.type == null ? "" : p.type)).toLowerCase();
            if (q.isEmpty() || searchable.contains(q)) addCard(p);
        }

        if (list.getChildCount() == 0) {
            TextView empty = new TextView(this);
            empty.setText("No properties found.");
            empty.setPadding(20, 40, 20, 40);
            empty.setTextColor(getColor(R.color.text_gray));
            list.addView(empty);
        }
    }

    private void addCard(Property p) {
        float density = getResources().getDisplayMetrics().density;

        CardView card = new CardView(this);
        card.setRadius(16 * density);
        card.setCardElevation(2 * density);
        card.setCardBackgroundColor(0xFFFFFFFF);
        card.setUseCompatPadding(true);

        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardLp.setMargins(0, (int)(4 * density), 0, (int)(12 * density));
        card.setLayoutParams(cardLp);

        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);

        // Image Container with Heart Icon Overlay
        FrameLayout frame = new FrameLayout(this);
        int imgHeight = (int) (150 * density);
        frame.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, imgHeight));

        ImageView iv = new ImageView(this);
        iv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iv.setBackgroundColor(0xFFEEEEEE);
        ImageUtil.loadImage(iv, p.imageUrl);
        frame.addView(iv);

        // Heart favorite button on top right of image
        ImageView ivHeart = new ImageView(this);
        int heartSize = (int) (36 * density);
        FrameLayout.LayoutParams heartLp = new FrameLayout.LayoutParams(heartSize, heartSize);
        heartLp.gravity = Gravity.END | Gravity.TOP;
        heartLp.setMargins(0, (int)(10 * density), (int)(12 * density), 0);
        ivHeart.setLayoutParams(heartLp);
        ivHeart.setPadding((int)(6 * density), (int)(6 * density), (int)(6 * density), (int)(6 * density));
        ivHeart.setBackgroundResource(R.drawable.bg_input);

        com.rentease.app.util.SessionManager sm = new com.rentease.app.util.SessionManager(this);
        String currentUid = sm.getUserId();
        android.content.SharedPreferences favPrefs = getSharedPreferences("RentEaseFavorites", MODE_PRIVATE);
        String favKey = "fav_" + (currentUid.isEmpty() ? "guest" : currentUid) + "_" + p.id;
        final boolean[] isFav = {favPrefs.getBoolean(favKey, false)}; // DEFAULT IS FALSE
        ivHeart.setImageResource(isFav[0] ? R.drawable.ic_heart_red : R.drawable.ic_heart_gray);

        ivHeart.setOnClickListener(v -> {
            isFav[0] = !isFav[0];
            favPrefs.edit().putBoolean(favKey, isFav[0]).apply();
            ivHeart.setImageResource(isFav[0] ? R.drawable.ic_heart_red : R.drawable.ic_heart_gray);
            com.rentease.app.util.Ui.toast(this, isFav[0] ? "Added to Favorites" : "Removed from Favorites");
        });
        frame.addView(ivHeart);

        content.addView(frame);

        // Property Information section
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding((int)(16 * density), (int)(12 * density), (int)(16 * density), (int)(14 * density));

        // Title & Status
        TextView tvTitle = new TextView(this);
        String statusLabel = "";
        boolean isBooked = p.isBooked() || dbHelper.isPropertyBooked(p.id, p.name);
        if (isBooked) {
            statusLabel = "  [BOOKED]";
        } else if (p.isExpired()) {
            statusLabel = "  [EXPIRED]";
        }
        tvTitle.setText(safe(p.name) + statusLabel);
        tvTitle.setTextSize(17);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(getColor((p.isExpired() || isBooked) ? R.color.text_gray : R.color.text_dark));
        info.addView(tvTitle);

        // Location
        TextView tvLoc = new TextView(this);
        tvLoc.setText("📍 " + safe(p.location) + "  •  " + safe(p.type));
        tvLoc.setTextSize(13);
        tvLoc.setTextColor(getColor(R.color.text_gray));
        tvLoc.setPadding(0, (int)(4 * density), 0, (int)(6 * density));
        info.addView(tvLoc);

        // Rent Price
        TextView tvPrice = new TextView(this);
        tvPrice.setText("₹" + safe(p.rent) + " / month" + (p.isExpired() ? "  (Listing Inactive)" : ""));
        tvPrice.setTextSize(15);
        tvPrice.setTypeface(null, Typeface.BOLD);
        tvPrice.setTextColor(p.isExpired() ? 0xFFEF4444 : getColor(R.color.green));
        info.addView(tvPrice);

        content.addView(info);
        card.addView(content);

        card.setOnClickListener(v -> {
            Intent i = new Intent(this, PropertyDetailsActivity.class);
            i.putExtra("propertyId", p.id);
            startActivity(i);
        });
        list.addView(card);
    }

    private String safe(String value) { return value == null ? "" : value; }
}
