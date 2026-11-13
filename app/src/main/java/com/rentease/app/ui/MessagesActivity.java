package com.rentease.app.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.rentease.app.R;
import com.rentease.app.database.DatabaseHelper;
import com.rentease.app.util.NavigationHelper;

import java.util.List;
import java.util.Map;

public class MessagesActivity extends AppCompatActivity {
    private LinearLayout list;
    private DatabaseHelper dbHelper;
    private EditText etMessageInput;
    private Button btnSendMessage;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_messages);

        View btnBack = findViewById(R.id.btnBack);
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());

        dbHelper = DatabaseHelper.getInstance(this);
        list = findViewById(R.id.list);
        etMessageInput = findViewById(R.id.etMessageInput);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        // Send button listener
        if (btnSendMessage != null) {
            btnSendMessage.setOnClickListener(v -> sendMessage());
        }

        // Quick inquiry chip click listeners
        setupChips();

        // Check if opened from Property Details with an initial message
        String initMsg = getIntent().getStringExtra("initialMessage");
        if (initMsg != null && !initMsg.trim().isEmpty() && etMessageInput != null) {
            etMessageInput.setText(initMsg);
            etMessageInput.requestFocus();
        }

        loadMessages();

        // Setup bottom navigation
        NavigationHelper.setupBottomNav(this, NavigationHelper.TAB_MESSAGES);
    }

    private void setupChips() {
        View chipAvailable = findViewById(R.id.chipAvailable);
        View chipVisit = findViewById(R.id.chipVisit);
        View chipNegotiable = findViewById(R.id.chipNegotiable);
        View chipContact = findViewById(R.id.chipContact);

        if (chipAvailable != null) {
            chipAvailable.setOnClickListener(v -> setChipText("Hello, is this rental property currently available?"));
        }
        if (chipVisit != null) {
            chipVisit.setOnClickListener(v -> setChipText("Can we schedule a physical visit for this property this week?"));
        }
        if (chipNegotiable != null) {
            chipNegotiable.setOnClickListener(v -> setChipText("Is the monthly rent or security deposit negotiable?"));
        }
        if (chipContact != null) {
            chipContact.setOnClickListener(v -> setChipText("Could you please share the owner's direct contact phone number?"));
        }
    }

    private void setChipText(String text) {
        if (etMessageInput != null) {
            etMessageInput.setText(text);
            etMessageInput.setSelection(text.length());
            etMessageInput.requestFocus();
        }
    }

    private void sendMessage() {
        if (etMessageInput == null) return;
        String text = etMessageInput.getText().toString().trim();
        if (text.isEmpty()) {
            com.rentease.app.util.Ui.toast(this, "Please type a message first");
            return;
        }

        com.rentease.app.util.SessionManager sm = new com.rentease.app.util.SessionManager(this);
        String sender = sm.isLoggedIn() ? sm.getUserName() : "Me (Tenant)";

        long result = dbHelper.insertMessage(sender, text);
        if (result > 0) {
            etMessageInput.setText("");
            com.rentease.app.util.Ui.toast(this, "Message sent to owner!");
            loadMessages();

            // Auto-scroll to newly sent message
            final android.widget.ScrollView sv = findViewById(R.id.scrollViewMessages);
            if (sv != null) {
                sv.post(() -> sv.fullScroll(View.FOCUS_DOWN));
            }
        } else {
            com.rentease.app.util.Ui.toast(this, "Failed to send message");
        }
    }

    private void loadMessages() {
        list.removeAllViews();
        List<Map<String, String>> messages = dbHelper.getAllMessages();

        if (messages.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No messages yet.");
            empty.setTextColor(getColor(R.color.text_gray));
            empty.setPadding(20, 60, 20, 60);
            empty.setGravity(Gravity.CENTER);
            list.addView(empty);
            return;
        }

        float density = getResources().getDisplayMetrics().density;
        String[] timeLabels = {"10:30 AM", "Yesterday", "2 Days Ago", "3 Days Ago", "Last Week"};
        int index = 0;

        for (Map<String, String> m : messages) {
            String sender = m.get("senderName");
            String text = m.get("text");

            CardView card = new CardView(this);
            card.setRadius(14 * density);
            card.setCardElevation(1.5f * density);
            card.setCardBackgroundColor(0xFFFFFFFF);
            card.setUseCompatPadding(true);

            LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cardLp.setMargins(0, (int)(4 * density), 0, (int)(8 * density));
            card.setLayoutParams(cardLp);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding((int)(14 * density), (int)(14 * density), (int)(14 * density), (int)(14 * density));

            // Circular Avatar with Letter
            TextView tvAvatar = new TextView(this);
            int avatarSize = (int)(44 * density);
            LinearLayout.LayoutParams avLp = new LinearLayout.LayoutParams(avatarSize, avatarSize);
            avLp.setMargins(0, 0, (int)(12 * density), 0);
            tvAvatar.setLayoutParams(avLp);
            tvAvatar.setGravity(Gravity.CENTER);
            tvAvatar.setBackgroundResource(R.drawable.bg_green);
            tvAvatar.setTextColor(0xFFFFFFFF);
            tvAvatar.setTextSize(16);
            tvAvatar.setTypeface(null, Typeface.BOLD);
            String firstLetter = (sender != null && !sender.isEmpty()) ? String.valueOf(sender.charAt(0)).toUpperCase() : "U";
            tvAvatar.setText(firstLetter);
            row.addView(tvAvatar);

            // Middle Column: Sender & Preview
            LinearLayout info = new LinearLayout(this);
            info.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
            infoLp.setMargins(0, 0, (int)(10 * density), 0);
            info.setLayoutParams(infoLp);

            TextView tvSender = new TextView(this);
            tvSender.setText(sender != null ? sender : "Support");
            tvSender.setTextSize(15);
            tvSender.setTypeface(null, Typeface.BOLD);
            tvSender.setTextColor(getColor(R.color.text_dark));
            info.addView(tvSender);

            TextView tvText = new TextView(this);
            tvText.setText(text != null ? text : "");
            tvText.setTextSize(13);
            tvText.setTextColor(getColor(R.color.text_gray));
            tvText.setSingleLine(true);
            tvText.setEllipsize(android.text.TextUtils.TruncateAt.END);
            tvText.setPadding(0, (int)(3 * density), 0, 0);
            info.addView(tvText);

            row.addView(info);

            // Right Column: Time & Unread Badge (Non-wrapping, right-aligned)
            LinearLayout rightCol = new LinearLayout(this);
            rightCol.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams rightLp = new LinearLayout.LayoutParams(
                    (int)(76 * density), ViewGroup.LayoutParams.WRAP_CONTENT);
            rightCol.setLayoutParams(rightLp);
            rightCol.setGravity(Gravity.END);

            TextView tvTime = new TextView(this);
            String time = (index < timeLabels.length) ? timeLabels[index] : "Recent";
            tvTime.setText(time);
            tvTime.setTextSize(11);
            tvTime.setTextColor(getColor(R.color.text_gray));
            tvTime.setSingleLine(true);
            tvTime.setMaxLines(1);
            tvTime.setGravity(Gravity.END);
            rightCol.addView(tvTime);

            // Unread Dot Badge
            if (index == 0 || index == 1) {
                TextView tvBadge = new TextView(this);
                int badgeSize = (int)(18 * density);
                LinearLayout.LayoutParams badgeLp = new LinearLayout.LayoutParams(badgeSize, badgeSize);
                badgeLp.gravity = Gravity.END;
                badgeLp.setMargins(0, (int)(4 * density), 0, 0);
                tvBadge.setLayoutParams(badgeLp);
                tvBadge.setGravity(Gravity.CENTER);
                tvBadge.setBackgroundResource(R.drawable.bg_green);
                tvBadge.setTextColor(0xFFFFFFFF);
                tvBadge.setTextSize(10);
                tvBadge.setTypeface(null, Typeface.BOLD);
                tvBadge.setText("1");
                rightCol.addView(tvBadge);
            }

            final String finalSender = sender != null ? sender : "Property Owner";
            final String finalText = text != null ? text : "";
            final String finalTime = time;

            card.setOnClickListener(v -> showConversationDialog(finalSender, finalText, finalTime));

            row.addView(rightCol);
            card.addView(row);
            list.addView(card);
            index++;
        }
    }

    private void showConversationDialog(String sender, String messageText, String time) {
        float density = getResources().getDisplayMetrics().density;
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding((int)(20 * density), (int)(16 * density), (int)(20 * density), (int)(12 * density));

        // Header: Sender & Time
        TextView tvTitle = new TextView(this);
        tvTitle.setText("💬 Chat with " + sender);
        tvTitle.setTextSize(17);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setTextColor(getColor(R.color.text_dark));
        layout.addView(tvTitle);

        TextView tvTime = new TextView(this);
        tvTime.setText("Received • " + time);
        tvTime.setTextSize(11);
        tvTime.setTextColor(getColor(R.color.text_gray));
        tvTime.setPadding(0, (int)(2 * density), 0, (int)(10 * density));
        layout.addView(tvTime);

        // Message Bubble
        LinearLayout bubble = new LinearLayout(this);
        bubble.setBackgroundResource(R.drawable.bg_input);
        bubble.setPadding((int)(14 * density), (int)(12 * density), (int)(14 * density), (int)(12 * density));
        LinearLayout.LayoutParams bubbleLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        bubbleLp.setMargins(0, 0, 0, (int)(14 * density));
        bubble.setLayoutParams(bubbleLp);

        TextView tvContent = new TextView(this);
        tvContent.setText(messageText);
        tvContent.setTextSize(14);
        tvContent.setTextColor(getColor(R.color.text_dark));
        bubble.addView(tvContent);
        layout.addView(bubble);

        // Reply Input
        final EditText etReply = new EditText(this);
        etReply.setHint("Write a reply to " + sender + "...");
        etReply.setBackgroundResource(R.drawable.bg_input);
        etReply.setTextSize(14);
        etReply.setPadding((int)(12 * density), (int)(10 * density), (int)(12 * density), (int)(10 * density));
        layout.addView(etReply);

        builder.setView(layout);
        builder.setPositiveButton("Send Reply", (dialog, which) -> {
            String reply = etReply.getText().toString().trim();
            if (!reply.isEmpty()) {
                com.rentease.app.util.SessionManager sm = new com.rentease.app.util.SessionManager(this);
                String user = sm.isLoggedIn() ? sm.getUserName() : "Me";
                dbHelper.insertMessage(user, reply);
                com.rentease.app.util.Ui.toast(this, "Reply sent to " + sender + "!");
                loadMessages();
            }
        });
        builder.setNegativeButton("Close", null);
        builder.show();
    }
}
