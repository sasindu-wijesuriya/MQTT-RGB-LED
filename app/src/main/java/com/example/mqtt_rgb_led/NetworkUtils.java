package com.example.mqtt_rgb_led;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class NetworkUtils {

    public void sendPostRequest(final String url, final String ssid, final String password) {
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

                    // Write the data to the output stream
                    outputStream.write(postData.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    // Get the response code
                    int responseCode = conn.getResponseCode();

                    // You can handle the response code as needed
                    if (responseCode == HttpURLConnection.HTTP_OK) {

                    } else {
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
