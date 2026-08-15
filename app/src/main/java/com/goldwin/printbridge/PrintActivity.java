package com.goldwin.printbridge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

public class PrintActivity extends Activity {

    private static final String TAG = "GoldWinPrintBridge";
    private IWoyouService printerService;
    private String pendingJson;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            printerService = IWoyouService.Stub.asInterface(service);
            doPrint();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            printerService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri uri = getIntent().getData();
        if (uri == null) {
            Log.e(TAG, "No data in intent — nothing to print.");
            finish();
            return;
        }

        String encoded = uri.getQueryParameter("data");
        if (encoded == null) {
            Log.e(TAG, "No 'data' query parameter — nothing to print.");
            finish();
            return;
        }

        try {
            byte[] decoded = Base64.decode(encoded, Base64.URL_SAFE | Base64.NO_WRAP);
            pendingJson = new String(decoded, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode receipt data", e);
            finish();
            return;
        }

        try {
            Intent bindIntent = new Intent();
            bindIntent.setPackage("woyou.aidlservice.jiuiv5");
            bindIntent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
            boolean bound = bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
            if (!bound) {
                Log.e(TAG, "Could not bind to Sunmi printer service — is this a Sunmi device with the printer service installed?");
                finish();
            }
        } catch (Exception e) {
            Log.e(TAG, "bindService threw an exception", e);
            finish();
        }
    }

    private void doPrint() {
        if (printerService == null || pendingJson == null) {
            finishAndClose();
            return;
        }
        try {
            JSONObject root = new JSONObject(pendingJson);

            printerService.printerInit(null);

            JSONArray lines = root.optJSONArray("lines");
            if (lines != null) {
                for (int i = 0; i < lines.length(); i++) {
                    JSONObject line = lines.getJSONObject(i);
                    String text = line.optString("text", "");
                    int align = line.optInt("align", 0);
                    boolean bold = line.optBoolean("bold", false);
                    float size = (float) line.optDouble("size", 24);
                    if (bold) {
                        printerService.setFontWeight(true, null);
                    }
                    printerService.printSpecFormatText(text + "\n", "", size, align, null);
                    if (bold) {
                        printerService.setFontWeight(false, null);
                    }
                }
            }

            String qr = root.optString("qr", null);
            if (qr != null && qr.length() > 0) {
                printerService.lineWrap(1, null);
                printerService.printQRCode(qr, 6, 3, null);
            }

            printerService.lineWrap(4, null);
        } catch (Exception e) {
            Log.e(TAG, "Print failed", e);
        } finally {
            finishAndClose();
        }
    }

    private void finishAndClose() {
        try {
            if (printerService != null) {
                unbindService(connection);
            }
        } catch (Exception ignored) {
        }
        finish();
    }
}
