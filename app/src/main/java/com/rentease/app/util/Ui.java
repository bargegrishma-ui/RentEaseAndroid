package com.rentease.app.util;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;
import com.google.android.material.snackbar.Snackbar;

public class Ui {
    public static void toast(Context c, String s){ Toast.makeText(c,s,Toast.LENGTH_SHORT).show(); }
    public static void snack(View v, String s){ Snackbar.make(v,s,Snackbar.LENGTH_LONG).show(); }
}
