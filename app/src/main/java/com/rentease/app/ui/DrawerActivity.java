package com.rentease.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.util.SessionManager;
import com.rentease.app.util.Ui;

public class DrawerActivity extends AppCompatActivity {
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_drawer);

        sessionManager = new SessionManager(this);

        TextView tvName = findViewById(R.id.tvDrawerName);
        TextView emailTv = findViewById(R.id.email);

        if (sessionManager.isLoggedIn()) {
            if (tvName != null) tvName.setText(sessionManager.getUserName());
            if (emailTv != null) emailTv.setText(sessionManager.getUserEmail());
        }

        View btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) btnClose.setOnClickListener(v -> finish());

        findViewById(R.id.home).setOnClickListener(v -> go(HomeActivity.class));
        findViewById(R.id.add).setOnClickListener(v -> go(AddPropertyActivity.class));
        findViewById(R.id.props).setOnClickListener(v -> go(PropertyListActivity.class));
        
        View myBookings = findViewById(R.id.myBookings);
        if (myBookings != null) {
            myBookings.setOnClickListener(v -> go(MyBookingsActivity.class));
        }

        findViewById(R.id.messages).setOnClickListener(v -> go(MessagesActivity.class));
        findViewById(R.id.profile).setOnClickListener(v -> go(ProfileActivity.class));


        View btnSettings = findViewById(R.id.btnSettings);
        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> Ui.toast(this, "Settings (Local Offline Mode Active)"));
        }

        findViewById(R.id.logout).setOnClickListener(v -> SessionManager.promptLogout(this));
    }

    private void go(Class<?> c) {
        startActivity(new Intent(this, c));
        finish();
    }
}
