package com.goldwin.printbridge;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(48, 48, 48, 48);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("GoldWin Print Bridge");
        title.setTextSize(22);
        title.setTextColor(Color.parseColor("#1a1a2e"));
        title.setGravity(Gravity.CENTER);

        TextView status = new TextView(this);
        status.setText("\nInstalled and ready.\n\nThis app runs silently in the background\nwhenever GoldWin prints a ticket.\nYou don't need to open it manually.");
        status.setTextSize(15);
        status.setTextColor(Color.parseColor("#555555"));
        status.setGravity(Gravity.CENTER);

        root.addView(title);
        root.addView(status);
        setContentView(root);
    }
}
