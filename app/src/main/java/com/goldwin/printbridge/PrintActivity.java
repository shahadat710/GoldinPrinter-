package com.goldwin.printbridge;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.telpo.tps550.api.printer.UsbThermalPrinter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

/**
 * Handles goldwinprint://receipt?data=... intents from the GoldWin web app.
 *
 * This device is a Telpo M1 POS terminal. Telpo printers do NOT use the
 * Sunmi/Woyou AIDL system service — they use Telpo's own native SDK
 * (telpo_api.jar), called directly in-process via UsbThermalPrinter.
 * That's why the earlier Sunmi-style version never worked on this device.
 */
public class PrintActivity extends Activity {

    private UsbThermalPrinter printer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Uri data = getIntent().getData();
        if (data == null) {
            toast("GoldWin Bridge: no data in intent — nothing to print.");
            finish();
            return;
        }

        String encoded = data.getQueryParameter("data");
        if (encoded == null) {
            toast("GoldWin Bridge: missing 'data' parameter.");
            finish();
            return;
        }

        String pendingJson;
        try {
            pendingJson = new String(Base64.decode(encoded, Base64.URL_SAFE), StandardCharsets.UTF_8);
        } catch (Exception e) {
            toast("GoldWin Bridge: failed to decode receipt data.");
            Log.e("GoldWinPrintBridge", "Failed to decode receipt data", e);
            finish();
            return;
        }

        doPrint(pendingJson);
    }

    private void doPrint(String pendingJson) {
        try {
            printer = new UsbThermalPrinter(this);
            printer.start(0);
            printer.reset();
            printer.setLeftIndent(0);
            printer.setLineSpace(0);
            printer.setGray(5);

            JSONObject payload = new JSONObject(pendingJson);
            JSONArray lines = payload.optJSONArray("lines");

            if (lines != null) {
                for (int i = 0; i < lines.length(); i++) {
                    JSONObject line = lines.getJSONObject(i);
                    String text = line.optString("text", "");
                    int align = line.optInt("align", 0); // 0=left, 1=center, 2=right
                    float sizeRaw = (float) line.optDouble("size", 24);

                    int telpoAlign;
                    if (align == 1) telpoAlign = UsbThermalPrinter.ALGIN_MIDDLE;
                    else if (align == 2) telpoAlign = UsbThermalPrinter.ALGIN_RIGHT;
                    else telpoAlign = UsbThermalPrinter.ALGIN_LEFT;

                    int telpoSize = nearestSize(sizeRaw);

                    printer.setTextSize(telpoSize);
                    printer.setAlgin(telpoAlign);
                    printer.addString(text);
                    printer.printString();
                }
            }

            printer.walkPaper(4);
            printer.stop();

            toast("Print sent ✓");
        } catch (Exception e) {
            String msg = String.valueOf(e);
            if (msg.contains("NoPaperException")) {
                toast("GoldWin Bridge: printer is out of paper.");
            } else if (msg.contains("OverHeatException")) {
                toast("GoldWin Bridge: printer overheated, let it cool down.");
            } else {
                toast("GoldWin Bridge: print failed — " + msg);
            }
            Log.e("GoldWinPrintBridge", "Print failed", e);
            try { if (printer != null) printer.stop(); } catch (Exception ignored) {}
        } finally {
            finish();
        }
    }

    private int nearestSize(float size) {
        int[] allowed = {18, 24, 34, 44, 54, 64};
        int best = allowed[0];
        float bestDiff = Math.abs(size - best);
        for (int a : allowed) {
            float diff = Math.abs(size - a);
            if (diff < bestDiff) { bestDiff = diff; best = a; }
        }
        return best;
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
