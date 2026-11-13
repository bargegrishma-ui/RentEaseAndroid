package com.rentease.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.User;
import com.rentease.app.util.NavigationHelper;
import com.rentease.app.util.SessionManager;
import com.rentease.app.util.Ui;

public class ProfileActivity extends AppCompatActivity {
    private SessionManager sessionManager;
    private DatabaseHelper dbHelper;
    private TextView tvEmail, tvProfileName;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        dbHelper = DatabaseHelper.getInstance(this);

        tvEmail = findViewById(R.id.email);
        tvProfileName = findViewById(R.id.tvProfileName);

        updateProfileDisplay();

        findViewById(R.id.edit).setOnClickListener(v -> showEditProfileDialog());
        findViewById(R.id.myProps).setOnClickListener(v -> startActivity(new Intent(this, PropertyListActivity.class)));
        
        if (findViewById(R.id.myBookings) != null) {
            findViewById(R.id.myBookings).setOnClickListener(v -> 
                startActivity(new Intent(this, MyBookingsActivity.class)));
        }

        if (findViewById(R.id.messages) != null) {
            findViewById(R.id.messages).setOnClickListener(v -> 
                startActivity(new Intent(this, MessagesActivity.class)));
        }



        View btnChangePassword = findViewById(R.id.btnChangePassword);
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(v -> showChangePasswordDialog());
        }

        View btnDeleteAccount = findViewById(R.id.btnDeleteAccount);
        if (btnDeleteAccount != null) {
            btnDeleteAccount.setOnClickListener(v -> showDeleteAccountDialog());
        }


        findViewById(R.id.logout).setOnClickListener(v -> SessionManager.promptLogout(this));

        // Setup bottom navigation
        NavigationHelper.setupBottomNav(this, NavigationHelper.TAB_PROFILE);
    }

    private void updateProfileDisplay() {
        String emailStr = sessionManager.getUserEmail();
        String nameStr = sessionManager.getUserName();
        User u = (emailStr != null && !emailStr.trim().isEmpty()) ? dbHelper.getUserByEmail(emailStr.trim()) : null;
        if (u != null) {
            if (u.getName() != null && !u.getName().trim().isEmpty()) {
                nameStr = u.getName().trim();
            }
        }
        if (tvProfileName != null) {
            tvProfileName.setText(nameStr != null && !nameStr.trim().isEmpty() ? nameStr : "User Profile");
        }
        if (tvEmail != null) {
            String phoneInfo = (u != null && u.getPhone() != null && !u.getPhone().isEmpty()) ? " • 📞 " + u.getPhone() : "";
            tvEmail.setText((emailStr != null && !emailStr.trim().isEmpty() ? emailStr : "No email registered") + phoneInfo);
        }
    }

    private void showEditProfileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Profile");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etName = new EditText(this);
        etName.setHint("Name");
        etName.setText(sessionManager.getUserName());
        layout.addView(etName);

        User currentUser = dbHelper.getUserByEmail(sessionManager.getUserEmail());
        final EditText etPhone = new EditText(this);
        etPhone.setHint("Phone Number");
        if (currentUser != null && currentUser.getPhone() != null) {
            etPhone.setText(currentUser.getPhone());
        }
        layout.addView(etPhone);

        builder.setView(layout);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newPhone = etPhone.getText().toString().trim();
            if (!newName.isEmpty()) {
                dbHelper.updateUser(sessionManager.getUserId(), newName, newPhone);
                sessionManager.createLoginSession(sessionManager.getUserId(), sessionManager.getUserEmail(), newName);
                updateProfileDisplay();
                Ui.toast(this, "Profile updated successfully");
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Password");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final EditText etPass = new EditText(this);
        etPass.setHint("New Password");
        etPass.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        layout.addView(etPass);

        builder.setView(layout);
        builder.setPositiveButton("Update", (dialog, which) -> {
            String newPass = etPass.getText().toString().trim();
            if (newPass.length() >= 6) {
                dbHelper.updatePassword(sessionManager.getUserEmail(), newPass);
                Ui.toast(this, "Password updated successfully in SQLite!");
            } else {
                Ui.toast(this, "Password must be at least 6 characters");
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteAccountDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to permanently delete your account (" + sessionManager.getUserEmail() + ") from the SQLite database?\n\nThis will remove your record from the 'users' table.")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("Yes, Delete", (dialog, which) -> {
                    String userId = sessionManager.getUserId();
                    int deleted = dbHelper.deleteUser(userId);
                    if (deleted > 0) {
                        Ui.toast(this, "User deleted from SQLite database!");
                        sessionManager.logoutUser();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        Ui.toast(this, "Failed to delete user account");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
