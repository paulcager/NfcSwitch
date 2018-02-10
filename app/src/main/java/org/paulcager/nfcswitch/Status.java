package org.paulcager.nfcswitch;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class Status extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 1;
    private static final String TAG = "NFCStatusActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.i(TAG, "getAction = " + getIntent().getAction());

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                for (int i = 0; i < rawMsgs.length; i++) {
                    NdefMessage msg = (NdefMessage) rawMsgs[i];
                    for (int j = 0; j < msg.getRecords().length; j++) {
                        NdefRecord rec = msg.getRecords()[j];
                        processRecord(rec);
                    }
                }
            }
        }
    }

    private void processRecord(NdefRecord rec) {
        Log.i(TAG, "MimeType: " + rec.toMimeType());
        if ("text/plain".equals(rec.toMimeType())) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            WifiManager wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            byte[] payload = rec.getPayload();
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0077;
            try {
                String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
                String text =
                        new String(payload, languageCodeLength + 1,
                                payload.length - languageCodeLength - 1, textEncoding);

                Log.i(TAG, "Text: '" + text + "'");
                switch (text) {
                    case "bof":
                        Log.i(TAG, "Switch off bluetooth");
                        bluetoothAdapter.disable();
                        break;
                    case "bon":
                        bluetoothAdapter.enable();
                        Log.i(TAG, "Switch on bluetooth");
                        break;
                    case "wof":
                        wifiManager.setWifiEnabled(false);
                        break;
                    case "won":
                        wifiManager.setWifiEnabled(true);
                }
//                Intent callIntent = new Intent(Intent.ACTION_CALL);
//                callIntent.setData(Uri.parse("tel:01782503160"));
//
//                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                    ActivityCompat.requestPermissions(this,
//                            new String[]{Manifest.permission.CALL_PHONE},
//                            MY_PERMISSIONS_REQUEST_CALL_PHONE);
//                    return;
//                }
//                startActivity(callIntent);
            } catch (UnsupportedEncodingException e) {
                // Badly-formed message; just ignore.
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CALL_PHONE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:01782503160"));
                    if (false) {
                        startActivity(callIntent);
                    }
                } else {
                    // permission denied,
                }
                return;
            }
        }
    }
}
