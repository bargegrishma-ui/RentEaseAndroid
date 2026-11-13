package com.rentease.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.model.User;
import com.rentease.app.util.SessionManager;
import com.rentease.app.util.Ui;

public class LoginActivity extends AppCompatActivity {
    private EditText email, password;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = DatabaseHelper.getInstance(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            openHome();
            return;
        }

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        // Leave fields empty by default with placeholders, no prefilled credentials
        email.setText("");
        password.setText("");

        findViewById(R.id.btnLogin).setOnClickListener(v -> login());
        findViewById(R.id.btnRegister).setOnClickListener(v -> showRegisterDialog());
        findViewById(R.id.btnGoogle).setOnClickListener(v ->
                Ui.toast(this, "Offline Mode: Google Sign-In is disabled. Please use email & password."));
        View btnFacebook = findViewById(R.id.btnFacebook);
        if (btnFacebook != null) {
            btnFacebook.setOnClickListener(v ->
                    Ui.toast(this, "Offline Mode: Facebook Login is disabled. Please use email & password."));
        }
        View tvForgot = findViewById(R.id.tvForgotPassword);
        if (tvForgot != null) {
            tvForgot.setOnClickListener(v -> showForgotPasswordDialog());
        }
        View btnToggle = findViewById(R.id.btnTogglePassword);
        if (btnToggle != null) {
            btnToggle.setOnClickListener(v -> {
                if (password.getInputType() == (android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                    password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    password.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
                password.setSelection(password.getText().length());
            });
        }
    }

    private void showForgotPasswordDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_forgot_password, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etForgotEmail = dialogView.findViewById(R.id.etForgotEmail);
        EditText etForgotNewPass = dialogView.findViewById(R.id.etForgotNewPass);
        EditText etForgotConfirmPass = dialogView.findViewById(R.id.etForgotConfirmPass);
        View btnForgotCancel = dialogView.findViewById(R.id.btnForgotCancel);
        View btnForgotSubmit = dialogView.findViewById(R.id.btnForgotSubmit);

        // Pre-fill email if entered on login screen
        String curEmail = email.getText().toString().trim();
        if (!curEmail.isEmpty() && Patterns.EMAIL_ADDRESS.matcher(curEmail).matches()) {
            etForgotEmail.setText(curEmail);
            etForgotNewPass.requestFocus();
        }

        btnForgotCancel.setOnClickListener(v -> dialog.dismiss());

        btnForgotSubmit.setOnClickListener(v -> {
            String forgotEmail = etForgotEmail.getText().toString().trim();
            String newPass = etForgotNewPass.getText().toString();
            String confirmPass = etForgotConfirmPass.getText().toString();

            if (!Patterns.EMAIL_ADDRESS.matcher(forgotEmail).matches()) {
                etForgotEmail.setError("Enter a valid registered email");
                return;
            }

            if (!dbHelper.checkEmailExists(forgotEmail)) {
                etForgotEmail.setError("No account found with this email");
                Ui.toast(this, "Email is not registered in SQLite database.");
                return;
            }

            if (newPass.length() < 6) {
                etForgotNewPass.setError("Password must be at least 6 characters");
                return;
            }

            if (!newPass.equals(confirmPass)) {
                etForgotConfirmPass.setError("Passwords do not match");
                return;
            }

            boolean updated = dbHelper.updatePassword(forgotEmail, newPass);
            if (updated) {
                Ui.toast(this, "Password updated successfully in SQLite!");
                email.setText(forgotEmail);
                password.setText(newPass);
                dialog.dismiss();
            } else {
                Ui.toast(this, "Failed to update password. Please try again.");
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_register, null);
        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        EditText etRegName = dialogView.findViewById(R.id.etRegName);
        EditText etRegEmail = dialogView.findViewById(R.id.etRegEmail);
        EditText etRegPhone = dialogView.findViewById(R.id.etRegPhone);
        EditText etRegPassword = dialogView.findViewById(R.id.etRegPassword);
        View btnRegCancel = dialogView.findViewById(R.id.btnRegCancel);
        View btnRegSubmit = dialogView.findViewById(R.id.btnRegSubmit);

        // Pre-fill email if typed on login screen
        String curEmail = email.getText().toString().trim();
        if (!curEmail.isEmpty()) {
            etRegEmail.setText(curEmail);
        }

        btnRegCancel.setOnClickListener(v -> dialog.dismiss());

        btnRegSubmit.setOnClickListener(v -> {
            String name = etRegName.getText().toString().trim();
            String regEmail = etRegEmail.getText().toString().trim();
            String phone = etRegPhone.getText().toString().trim();
            String pass = etRegPassword.getText().toString();

            if (name.isEmpty()) {
                etRegName.setError("Enter full name");
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(regEmail).matches()) {
                etRegEmail.setError("Enter a valid email");
                return;
            }
            if (phone.length() < 10) {
                etRegPhone.setError("Enter a valid 10-digit phone number");
                return;
            }
            if (pass.length() < 6) {
                etRegPassword.setError("Password must be at least 6 characters");
                return;
            }

            if (dbHelper.checkEmailExists(regEmail)) {
                etRegEmail.setError("Email already exists");
                Ui.toast(this, "An account with this email already exists in SQLite.");
                return;
            }

            long newId = dbHelper.registerUser(name, regEmail, pass, phone);
            if (newId > 0) {
                dialog.dismiss();
                sessionManager.createLoginSession(String.valueOf(newId), regEmail, name);
                Ui.toast(this, "Account created with phone: " + phone);
                openHome();
            } else {
                Ui.toast(this, "Registration failed. Please try again.");
            }
        });

        dialog.show();
    }

    private boolean validate() {
        String e = email.getText().toString().trim();
        String p = password.getText().toString();

        if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
            email.setError("Enter a valid email");
            return false;
        }
        if (p.length() < 6) {
            password.setError("Password must contain at least 6 characters");
            return false;
        }
        return true;
    }

    private void login() {
        if (!validate()) return;

        String e = email.getText().toString().trim();
        String p = password.getText().toString();

        User user = dbHelper.checkUser(e, p);
        if (user != null) {
            sessionManager.createLoginSession(user.getId(), user.getEmail(), user.getName());
            Ui.toast(this, "Welcome back, " + user.getName() + "!");
            openHome();
        } else {
            Ui.toast(this, "Invalid email or password");
        }
    }

    private void openHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
