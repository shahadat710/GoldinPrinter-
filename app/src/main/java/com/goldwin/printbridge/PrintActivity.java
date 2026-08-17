package com.goldwin.printbridge;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import woyou.aidlservice.jiuiv5.IWoyouService;

public class PrintActivity extends Activity {

    private IWoyouService printerService;
    private String pendingJson;
    private final Handler timeoutHandler = new Handler(Looper.getMainLooper());
    private boolean finished = false;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            timeoutHandler.removeCallbacksAndMessages(null);
            toast("Connected to printer service ✓");
            printerService = IWoyouService.Stub.asInterface(binder);
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

        Uri data = getIntent().getData();
        if (data == null) {
            toast("GoldWin Bridge: no data in intent — nothing to print.");
            Log.e("GoldWinPrintBridge", "No data in intent — nothing to print.");
            finish();
            return;
        }

        String encoded = data.getQueryParameter("data");
        if (encoded == null) {
            toast("GoldWin Bridge: missing 'data' parameter.");
            Log.e("GoldWinPrintBridge", "No 'data' query parameter — nothing to print.");
            finish();
            return;
        }

        try {
            pendingJson = new String(Base64.decode(encoded, Base64.URL_SAFE), StandardCharsets.UTF_8);
        } catch (Exception e) {
            toast("GoldWin Bridge: failed to decode receipt data.");
            Log.e("GoldWinPrintBridge", "Failed to decode receipt data", e);
            finish();
            return;
        }

        Intent bindIntent = new Intent();
        bindIntent.setPackage("woyou.aidlservice.jiuiv5");
        bindIntent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");

        boolean bound;
        try {
            bound = bindService(bindIntent, connection, BIND_AUTO_CREATE);
        } catch (Exception e) {
            toast("GoldWin Bridge: bindService threw an exception — see details below.");
            Log.e("GoldWinPrintBridge", "bindService threw an exception", e);
            finish();
            return;
        }

        if (!bound) {
            toast("GoldWin Bridge: could not find the Sunmi/Woyou printer service on this device. This device's printer likely uses a different SDK — see chat for next steps.");
            Log.e("GoldWinPrintBridge", "Could not bind to woyou.aidlservice.jiuiv5 — this device likely doesn't run that service.");
            finish();
            return;
        }

        toast("Binding to printer service…");
        timeoutHandler.postDelayed(() -> {
            if (!finished && printerService == null) {
                toast("GoldWin Bridge: bound, but service never connected (timed out after 4s).");
                Log.e("GoldWinPrintBridge", "bindService() returned true but onServiceConnected never fired.");
                finishAndClose();
            }
        }, 4000);
    }

    private void doPrint() {
        if (printerService == null || pendingJson == null) {
            finishAndClose();
            return;
        }
        try {
            JSONObject payload = new JSONObject(pendingJson);
            printerService.printerInit(null);

            JSONArray lines = payload.optJSONArray("lines");
            if (lines != null) {
                for (int i = 0; i < lines.length(); i++) {
                    JSONObject line = lines.getJSONObject(i);
                    String text = line.optString("text", "");
                    int align = line.optInt("align", 0);
                    boolean bold = line.optBoolean("bold", false);
                    float size = (float) line.optDouble("size", 24);

                    if (bold) printerService.setFontWeight(true, null);
                    printerService.printSpecFormatText(text + "\n", "", size, align, null);
                    if (bold) printerService.setFontWeight(false, null);
                }
            }

            String qr = payload.optString("qr", "");
            if (qr != null && qr.length() > 0) {
                printerService.lineWrap(1, null);
                printerService.printQRCode(qr, 6, 3, null);
            }
            printerService.lineWrap(4, null);

            toast("Print sent ✓");
        } catch (Exception e) {
            toast("GoldWin Bridge: print failed — " + e.getMessage());
            Log.e("GoldWinPrintBridge", "Print failed", e);
        } finally {
            finishAndClose();
        }
    }

    private void finishAndClose() {
        finished = true;
        timeoutHandler.removeCallbacksAndMessages(null);
        try {
            if (printerService != null) unbindService(connection);
        } catch (Exception ignored) {}
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
