package com.example.mqtt_rgb_led;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.net.wifi.WifiConfiguration;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedOutputStream;
import java.io.IOException;


public class AddDeviceActivity extends AppCompatActivity {

    private static final int WIFI_PERMISSION_CODE = 101;
    private List<ScanResult> wifiList;
    private WifiManager wifiManager;
    private EditText editTextSSID;
    private EditText editTextWiFiPassword;
    private EditText editTextDevicePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        editTextSSID = findViewById(R.id.editTextSSID);
        editTextWiFiPassword = findViewById(R.id.editTextWiFiPassword);
        editTextDevicePassword = findViewById(R.id.editTextDevicePassword);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        // Check and request necessary permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, WIFI_PERMISSION_CODE);
        }

        Button buttonAddDevice = findViewById(R.id.buttonAddDevice);
        buttonAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showWifiNetworks();
                String ipAddress = "192.164.1.4";
                String url = "https://" + ipAddress + "/save";
                String ssid = editTextSSID.getText().toString();
                String password = editTextWiFiPassword.getText().toString();
                sendPostRequestToConnectedWifiNetwork(url, ssid, password);
            }
        });
    }

    private void showWifiNetworks() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        wifiList = wifiManager.getScanResults();

        List<String> ssidList = new ArrayList<>();
        for (ScanResult scanResult : wifiList) {
            ssidList.add(scanResult.SSID);
        }

        String[] ssidArray = ssidList.toArray(new String[0]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Wi-Fi Network").setItems(ssidArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedSSID = ssidArray[which];
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    connectToWifiNetworkSpecifier(selectedSSID);
                } else {
                    connectToLegacyWifi(selectedSSID);
                }
            }
        });
        builder.show();
    }

    private void connectToWifiNetworkSpecifier(String ssid) {
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi is disabled, enabling...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        WifiNetworkSpecifier.Builder builder = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder = new WifiNetworkSpecifier.Builder();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setSsid(ssid);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setWpa2Passphrase(editTextDevicePassword.getText().toString());
        }

        WifiNetworkSpecifier wifiNetworkSpecifier = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            wifiNetworkSpecifier = builder.build();
        }
        NetworkRequest networkRequest = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                networkRequest = new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).setNetworkSpecifier(wifiNetworkSpecifier).build();
            }
        }

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.requestNetwork(networkRequest, new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                super.onAvailable(network);
                sendPostRequest(ssid, editTextWiFiPassword.getText().toString());
                connectivityManager.unregisterNetworkCallback(this);
            }

            @Override
            public void onUnavailable() {
                super.onUnavailable();
                Toast.makeText(AddDeviceActivity.this, "Failed to connect to the Wi-Fi network", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connectToLegacyWifi(String ssid) {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "Wi-Fi is disabled, enabling...", Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", ssid);
        wifiConfig.preSharedKey = String.format("\"%s\"", editTextDevicePassword.getText().toString());

        int netId = wifiManager.addNetwork(wifiConfig);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netId, true);
        wifiManager.reconnect();

        sendPostRequest(ssid, editTextWiFiPassword.getText().toString());
    }

    private void sendPostRequest(String ssid, String wifiPassword) {
        String postUrl = "http://192.168.1.4/save";
        String postData = "ssid=" + ssid + "&password=" + wifiPassword;

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL(postUrl);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);

                    OutputStream os = conn.getOutputStream();
                    os.write(postData.getBytes());
                    os.flush();
                    os.close();

                    int responseCode = conn.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("POST Request", "Success");
                        runOnUiThread(() -> {
                            Toast.makeText(AddDeviceActivity.this, "POST request sent", Toast.LENGTH_SHORT).show();
                        });
                    } else {
                        Log.e("POST Request", "Failed with response code: " + responseCode);
                        runOnUiThread(() -> {
                            Toast.makeText(AddDeviceActivity.this, "Failed to send POST request", Toast.LENGTH_SHORT).show();
                        });
                    }
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        Toast.makeText(AddDeviceActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
                return null;
            }
        }.execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WIFI_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showWifiNetworks();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }

    private void showPermissionDeniedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permission Denied");
        builder.setMessage("This app needs location permission to scan for Wi-Fi networks. Grant the permission in settings.");
        builder.setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", getPackageName(), null));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }


    public void sendPostRequestToConnectedWifiNetwork(final String url, final String ssid, final String password) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create URL object
                    URL urlObj = new URL(url);

                    // Create connection object
                    HttpURLConnection conn = (HttpURLConnection) urlObj.openConnection();

                    // Set the request method to POST
                    conn.setRequestMethod("POST");

                    // Set Content-Type header
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    // Enable writing data to the connection output stream
                    conn.setDoOutput(true);

                    // Create POST data string
                    String postData = "ssid=" + ssid + "&password=" + password;


                    // Get the output stream of the connection
                    OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());

                    Log.d("POST MSg", postData);

                    // Write the data to the output stream
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Get the response code
                    int responseCode = conn.getResponseCode();

                    // You can handle the response code as needed
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Log.d("Post status", "Done");
                    } else {
                        Log.d("Post status", "Fale");
                        // Request failed
                    }

                    // Close the connection
                    conn.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}