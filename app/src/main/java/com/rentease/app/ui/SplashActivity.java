package com.rentease.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.util.SessionManager;

public class SplashActivity extends AppCompatActivity {

    private boolean navigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button btnGetStarted = findViewById(R.id.btnGetStarted);
        if (btnGetStarted != null) {
            btnGetStarted.setOnClickListener(v -> proceedNext());
        }

        // Auto advance after 3 seconds if not clicked
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing() && !navigated) {
                proceedNext();
            }
        }, 3000);
    }

    private void proceedNext() {
        if (navigated) return;
        navigated = true;

        SessionManager sessionManager = new SessionManager(SplashActivity.this);
        Intent intent;
        if (sessionManager.isLoggedIn() && !sessionManager.isSessionExpired()) {
            intent = new Intent(SplashActivity.this, HomeActivity.class);
        } else {
            sessionManager.logoutUser();
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}