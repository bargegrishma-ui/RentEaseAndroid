package com.rentease.app.util;

import android.content.Context;
import android.content.Intent;
import com.rentease.app.ui.LoginActivity;

public class AuthGuard {
    public static boolean check(Context c) {
        SessionManager session = new SessionManager(c);
        if (!session.isLoggedIn() || session.isSessionExpired()) {
            session.logoutUser();
            Intent intent = new Intent(c, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            c.startActivity(intent);
            return false;
        }
        return true;
    }
}
