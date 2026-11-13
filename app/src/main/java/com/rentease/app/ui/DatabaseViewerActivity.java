package com.rentease.app.ui;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;

public class DatabaseViewerActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private LinearLayout layoutRecords;
    private TextView tvTableSummary, tvDbInfo;
    private String currentTable = "properties";

    private Button btnTableProperties, btnTableUsers, btnTableBookings, btnTableMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database_viewer);

        dbHelper = DatabaseHelper.getInstance(this);
        db = dbHelper.getReadableDatabase();

        layoutRecords = findViewById(R.id.layoutRecords);
        tvTableSummary = findViewById(R.id.tvTableSummary);
        tvDbInfo = findViewById(R.id.tvDbInfo);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnRefresh).setOnClickListener(v -> loadTable(currentTable));

        btnTableProperties = findViewById(R.id.btnTableProperties);
        btnTableUsers = findViewById(R.id.btnTableUsers);
        btnTableBookings = findViewById(R.id.btnTableBookings);
        btnTableMessages = findViewById(R.id.btnTableMessages);

        btnTableProperties.setOnClickListener(v -> loadTable("properties"));
        btnTableUsers.setOnClickListener(v -> loadTable("users"));
        btnTableBookings.setOnClickListener(v -> loadTable("bookings"));
        btnTableMessages.setOnClickListener(v -> loadTable("messages"));

        loadTable("properties");
    }

    private void loadTable(String tableName) {
        currentTable = tableName;
        highlightActiveButton(tableName);

        layoutRecords.removeAllViews();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + tableName, null);
            if (cursor == null) {
                tvTableSummary.setText("Table: " + tableName + " (Unable to open)");
                return;
            }

            int count = cursor.getCount();
            tvTableSummary.setText("Table: " + tableName + " (" + count + " record" + (count == 1 ? "" : "s") + ")");

            if (count == 0) {
                TextView empty = new TextView(this);
                empty.setText("No records in '" + tableName + "' table yet.");
                empty.setPadding(20, 40, 20, 40);
                empty.setTextColor(getColor(R.color.text_gray));
                layoutRecords.addView(empty);
                return;
            }

            String[] columns = cursor.getColumnNames();
            int rowIndex = 1;

            while (cursor.moveToNext()) {
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setBackgroundResource(R.drawable.bg_card);
                card.setPadding(24, 18, 24, 20);

                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.setMargins(0, 8, 0, 10);
                card.setLayoutParams(lp);

                // Row Header
                TextView tvRowHeader = new TextView(this);
                tvRowHeader.setText("Row #" + rowIndex);
                tvRowHeader.setTypeface(null, Typeface.BOLD);
                tvRowHeader.setTextColor(getColor(R.color.green));
                tvRowHeader.setTextSize(15);
                tvRowHeader.setPadding(0, 0, 0, 8);
                card.addView(tvRowHeader);

                // Column values
                StringBuilder sb = new StringBuilder();
                for (String col : columns) {
                    int colIdx = cursor.getColumnIndex(col);
                    String val = cursor.getString(colIdx);
                    if (val == null) val = "NULL";
                    sb.append("• ").append(col).append(": ").append(val).append("\n");
                }

                TextView tvContent = new TextView(this);
                tvContent.setText(sb.toString().trim());
                tvContent.setTextSize(13);
                tvContent.setTextColor(getColor(R.color.text_dark));
                tvContent.setLineSpacing(4f, 1.1f);
                card.addView(tvContent);

                layoutRecords.addView(card);
                rowIndex++;
            }

        } catch (Exception e) {
            e.printStackTrace();
            tvTableSummary.setText("Error reading table " + tableName + ": " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void highlightActiveButton(String tableName) {
        btnTableProperties.setAlpha(tableName.equals("properties") ? 1.0f : 0.6f);
        btnTableUsers.setAlpha(tableName.equals("users") ? 1.0f : 0.6f);
        btnTableBookings.setAlpha(tableName.equals("bookings") ? 1.0f : 0.6f);
        btnTableMessages.setAlpha(tableName.equals("messages") ? 1.0f : 0.6f);
    }
}
