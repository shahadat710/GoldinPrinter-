package com.goldwin.printbridge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import woyou.aidlservice.jiuiv5.IWoyouService;

public class MainActivity extends Activity {

    private IWoyouService printerService;
    private TextView statusText;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            printerService = IWoyouService.Stub.asInterface(service);
            statusText.setText("Printer service: CONNECTED ✓");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            printerService = null;
            statusText.setText("Printer service: disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(60, 60, 60, 60);
        layout.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("GoldWin Print Bridge");
        title.setTextSize(22);
        title.setPadding(0, 0, 0, 20);
        layout.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("This app is installed correctly.");
        subtitle.setTextSize(14);
        subtitle.setPadding(0, 0, 0, 30);
        layout.addView(subtitle);

        statusText = new TextView(this);
        statusText.setText("Printer service: connecting…");
        statusText.setTextSize(14);
        statusText.setPadding(0, 0, 0, 30);
        layout.addView(statusText);

        Button testButton = new Button(this);
        testButton.setText("Test Print Now");
        testButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doTestPrint();
            }
        });
        layout.addView(testButton);

        setContentView(layout);

        try {
            Intent bindIntent = new Intent();
            bindIntent.setPackage("woyou.aidlservice.jiuiv5");
            bindIntent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
            boolean bound = bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
            if (!bound) {
                statusText.setText("Printer service: COULD NOT BIND ✗\n(is this a genuine Sunmi device?)");
            }
        } catch (Exception e) {
            statusText.setText("Printer service: ERROR ✗\n" + e.getMessage());
        }
    }

    private void doTestPrint() {
        if (printerService == null) {
            Toast.makeText(this, "Printer service not connected yet", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            printerService.printerInit(null);
            printerService.setFontWeight(true, null);
            printerService.printSpecFormatText("GoldWin Print Bridge\n", "", 30, 1, null);
            printerService.setFontWeight(false, null);
            printerService.printSpecFormatText("Manual test — it works!\n", "", 22, 1, null);
            printerService.lineWrap(4, null);
            Toast.makeText(this, "Test print sent!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Print failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (printerService != null) {
                unbindService(connection);
            }
        } catch (Exception ignored) {
        }
    }
}
