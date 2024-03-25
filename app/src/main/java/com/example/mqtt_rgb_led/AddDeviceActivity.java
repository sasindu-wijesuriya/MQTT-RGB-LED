// AddDeviceActivity.java
package com.example.mqtt_rgb_led;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AddDeviceActivity extends AppCompatActivity {

    private static final String ESP_IP_ADDRESS = "192.168.1.4";
    private static final String ESP_SUCCESS_URL = "http://" + ESP_IP_ADDRESS + "/success";

    private Button btnCancelPairing;
    private WebView webView;
    private ProgressBar progressBar;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        btnCancelPairing = findViewById(R.id.btnCancelPairing);
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);

        showConnectDialog();

        btnCancelPairing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCancelDialog();
            }
        });
    }

    private void showConnectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Connect to the wifi network of the ESP")
                .setMessage("Please connect to the wifi network of the ESP before continuing.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showWebView();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showWebView() {
        btnCancelPairing.setVisibility(Button.VISIBLE);
        webView.setVisibility(WebView.VISIBLE);
        progressBar.setVisibility(ProgressBar.VISIBLE);
        webView.getSettings().setJavaScriptEnabled(true);

        webView.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.equals(ESP_SUCCESS_URL)) {
                    showSuccessDialog();
                }
            }
        });

        webView.loadUrl(ESP_IP_ADDRESS);
    }

    private void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ESP Pairing done")
                .setMessage("Please reconnect to the internet.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void showCancelDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cancel Pairing")
                .setMessage("Pairing cancelled. Returning to the device list menu.")
                .setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
