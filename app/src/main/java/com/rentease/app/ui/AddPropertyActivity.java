package com.rentease.app.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.Property;
import com.rentease.app.util.ImageUtil;
import com.rentease.app.util.SessionManager;
import com.rentease.app.util.Ui;

public class AddPropertyActivity extends AppCompatActivity {
    private EditText name, location, type, rent, desc, etImageUrl;
    private TextView tvTitle;
    private Button btnPost, btnPresetVilla, btnPresetApartment, btnPresetStudio, btnPresetHouse;
    private Button btnPreviewUrl, btnPickGallery, btnClearImage;
    private FrameLayout layoutImagePicker;
    private ImageView ivPropertyImage;
    private LinearLayout layoutUploadPrompt;

    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;
    private boolean isEditMode = false;
    private String propertyId;
    private String selectedImagePath = "";
    private String currentStatus = "Active";

    private final ActivityResultLauncher<String> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            (Uri uri) -> {
                if (uri != null) {
                    String savedPath = ImageUtil.saveImageToInternalStorage(this, uri);
                    if (savedPath != null) {
                        selectedImagePath = savedPath;
                        if (etImageUrl != null) etImageUrl.setText(savedPath);
                        ImageUtil.loadImage(ivPropertyImage, selectedImagePath);
                        layoutUploadPrompt.setVisibility(View.GONE);
                    } else {
                        Ui.toast(this, "Failed to process image");
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_property);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        tvTitle = findViewById(R.id.tvTitle);
        name = findViewById(R.id.name);
        location = findViewById(R.id.location);
        type = findViewById(R.id.type);
        rent = findViewById(R.id.rent);
        desc = findViewById(R.id.desc);
        btnPost = findViewById(R.id.btnPost);

        layoutImagePicker = findViewById(R.id.layoutImagePicker);
        ivPropertyImage = findViewById(R.id.ivPropertyImage);
        layoutUploadPrompt = findViewById(R.id.layoutUploadPrompt);

        etImageUrl = findViewById(R.id.etImageUrl);
        btnPreviewUrl = findViewById(R.id.btnPreviewUrl);
        btnPickGallery = findViewById(R.id.btnPickGallery);
        btnClearImage = findViewById(R.id.btnClearImage);

        btnPresetVilla = findViewById(R.id.btnPresetVilla);
        btnPresetApartment = findViewById(R.id.btnPresetApartment);
        btnPresetStudio = findViewById(R.id.btnPresetStudio);
        btnPresetHouse = findViewById(R.id.btnPresetHouse);

        layoutImagePicker.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        if (btnPickGallery != null) {
            btnPickGallery.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        }

        if (btnPresetVilla != null) {
            btnPresetVilla.setOnClickListener(v -> selectPreset("preset:villa", "Villa"));
        }
        if (btnPresetApartment != null) {
            btnPresetApartment.setOnClickListener(v -> selectPreset("preset:apartment", "Apartment"));
        }
        if (btnPresetStudio != null) {
            btnPresetStudio.setOnClickListener(v -> selectPreset("preset:studio", "Studio"));
        }
        if (btnPresetHouse != null) {
            btnPresetHouse.setOnClickListener(v -> selectPreset("preset:house", "House"));
        }

        if (btnPreviewUrl != null) {
            btnPreviewUrl.setOnClickListener(v -> {
                String url = etImageUrl.getText().toString().trim();
                if (url.isEmpty()) {
                    Ui.toast(this, "Please enter an image URL or preset first");
                    return;
                }
                selectedImagePath = url;
                ImageUtil.loadImage(ivPropertyImage, selectedImagePath);
                layoutUploadPrompt.setVisibility(View.GONE);
                Ui.toast(this, "Image applied");
            });
        }

        if (btnClearImage != null) {
            btnClearImage.setOnClickListener(v -> {
                selectedImagePath = "";
                if (etImageUrl != null) etImageUrl.setText("");
                ivPropertyImage.setImageDrawable(null);
                layoutUploadPrompt.setVisibility(View.VISIBLE);
                Ui.toast(this, "Image cleared");
            });
        }

        isEditMode = getIntent().getBooleanExtra("isEditMode", false);
        if (isEditMode) {
            propertyId = getIntent().getStringExtra("propertyId");
            if (tvTitle != null) tvTitle.setText("Edit Property");
            btnPost.setText("Update Property");

            name.setText(getIntent().getStringExtra("name"));
            location.setText(getIntent().getStringExtra("location"));
            type.setText(getIntent().getStringExtra("type"));
            rent.setText(getIntent().getStringExtra("rent"));
            desc.setText(getIntent().getStringExtra("description"));

            selectedImagePath = getIntent().getStringExtra("imageUrl");
            if (selectedImagePath != null && !selectedImagePath.trim().isEmpty()) {
                if (etImageUrl != null) etImageUrl.setText(selectedImagePath);
                ImageUtil.loadImage(ivPropertyImage, selectedImagePath);
                layoutUploadPrompt.setVisibility(View.GONE);
            }

            String s = getIntent().getStringExtra("status");
            if (s != null && !s.trim().isEmpty()) {
                currentStatus = s;
            }
        }

        btnPost.setOnClickListener(v -> save());
    }

    private void selectPreset(String presetKey, String label) {
        selectedImagePath = presetKey;
        if (etImageUrl != null) etImageUrl.setText(presetKey);
        ImageUtil.loadImage(ivPropertyImage, selectedImagePath);
        layoutUploadPrompt.setVisibility(View.GONE);
        Ui.toast(this, "Selected " + label + " preset image");
    }

    private void save() {
        String n = name.getText().toString().trim();
        String loc = location.getText().toString().trim();
        String t = type.getText().toString().trim();
        String r = rent.getText().toString().trim();
        String d = desc.getText().toString().trim();

        if (n.isEmpty() || loc.isEmpty()) {
            Ui.toast(this, "Property name and location are required");
            return;
        }

        if (selectedImagePath.isEmpty() && etImageUrl != null && !etImageUrl.getText().toString().trim().isEmpty()) {
            selectedImagePath = etImageUrl.getText().toString().trim();
        }

        if (isEditMode) {
            Property prop = new Property(propertyId, n, loc, t, r, d, sessionManager.getUserId(), selectedImagePath, currentStatus);
            int updated = dbHelper.updateProperty(prop);
            if (updated > 0) {
                Ui.toast(this, "Property updated successfully");
                finish();
            } else {
                Ui.toast(this, "Failed to update property");
            }
        } else {
            String ownerId = sessionManager.getUserId();
            if (ownerId == null || ownerId.isEmpty()) ownerId = "1";
            Property prop = new Property(n, loc, t, r, d, ownerId, selectedImagePath);
            long insertedId = dbHelper.insertProperty(prop);
            if (insertedId > 0) {
                Ui.toast(this, "Property posted successfully");
                finish();
            } else {
                Ui.toast(this, "Failed to post property");
            }
        }
    }
}
