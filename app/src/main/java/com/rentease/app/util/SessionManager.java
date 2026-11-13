package com.rentease.app.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;

import com.rentease.app.ui.LoginActivity;

public class SessionManager {
    private static final String PREF_NAME = "RentEaseSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";
    private static final String KEY_IS_EXPIRED = "isExpired";

    public static final long DEFAULT_SESSION_MAX_AGE_MS = 24 * 60 * 60 * 1000L; // 24 hours

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;
    private final Context context;

    public SessionManager(Context context) {
        this.context = context;
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    public void createLoginSession(String userId, String email, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putLong(KEY_LOGIN_TIMESTAMP, System.currentTimeMillis());
        editor.putBoolean(KEY_IS_EXPIRED, false);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public boolean isSessionExpired() {
        return isSessionExpired(DEFAULT_SESSION_MAX_AGE_MS);
    }

    public boolean isSessionExpired(long maxAgeMillis) {
        if (!isLoggedIn()) return true;
        if (pref.getBoolean(KEY_IS_EXPIRED, false)) return true;
        long loginTime = pref.getLong(KEY_LOGIN_TIMESTAMP, 0L);
        if (loginTime > 0L && (System.currentTimeMillis() - loginTime) > maxAgeMillis) {
            return true;
        }
        return false;
    }

    public void expireSession() {
        editor.putBoolean(KEY_IS_EXPIRED, true);
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.apply();
    }

    public String getUserId() {
        return pref.getString(KEY_USER_ID, "");
    }

    public String getUserEmail() {
        return pref.getString(KEY_USER_EMAIL, "");
    }

    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "");
    }

    public void logoutUser() {
        pref.edit().clear().commit();
    }

    public static void promptLogout(Activity activity) {
        if (activity == null || activity.isFinishing()) return;
        new AlertDialog.Builder(activity)
                .setTitle("Logout")
                .setMessage("Are you sure you want to log out of RentEase?")
                .setPositiveButton("Logout", (dialog, which) -> logout(activity))
                .setNegativeButton("Cancel", null)
                .show();
    }

    public static void logout(Activity activity) {
        if (activity == null) return;
        SessionManager session = new SessionManager(activity);
        session.logoutUser();
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }
}
