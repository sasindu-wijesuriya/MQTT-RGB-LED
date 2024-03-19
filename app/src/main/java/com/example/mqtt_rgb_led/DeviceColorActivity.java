package com.example.mqtt_rgb_led;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RelativeLayout;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

public class DeviceColorActivity extends AppCompatActivity {

    private RelativeLayout colorLayout;
    private MqttClient mqttClient;
    private String mqttTopic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_color);

        colorLayout = findViewById(R.id.colorLayout);

        // Get mqttTopic from intent
        mqttTopic = getIntent().getStringExtra("MQTT_TOPIC");
        if (mqttTopic == null) {
            Toast.makeText(this, "MQTT Topic not found", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Connect to MQTT server and subscribe to mqttTopic
//        connectAndSubscribe();
    }


//    private void connectAndSubscribe() {
//        try {
//            mqttClient = new MqttAsyncClient("tcp://test.mosquitto.org:1883", MqttClient.generateClientId(), null);
//            mqttClient.setCallback(new MqttCallback() {
//                @Override
//                public void connectionLost(Throwable cause) {
//                    Toast.makeText(DeviceColorActivity.this, "MQTT Connection lost", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void messageArrived(String topic, MqttMessage message) throws Exception {
//                    String jsonColor = new String(message.getPayload());
//                    displayColorFromJson(jsonColor);
//                }
//
//                @Override
//                public void deliveryComplete(IMqttDeliveryToken token) {
//                }
//            });
//
//            mqttClient.connect(getMqttConnectionOptions(), null, new IMqttActionListener() {
//                @Override
//                public void onSuccess(IMqttToken asyncActionToken) {
//                    subscribeToTopic();
//                }
//
//                @Override
//                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
//                    Toast.makeText(DeviceColorActivity.this, "Failed to connect to MQTT server", Toast.LENGTH_SHORT).show();
//                    finish();
//                }
//            });
//
//        } catch (MqttException e) {
//            e.printStackTrace();
//            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
//        }
//    }
//



    private void subscribeToTopic() {
        try {
            mqttClient.subscribe(mqttTopic, 0);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error subscribing to MQTT topic", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private MqttConnectOptions getMqttConnectionOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        return options;
    }

    private void displayColorFromJson(String jsonColor) {
        try {
            // Parse JSON color data
            // Assuming JSON format: {"red": 255, "green": 0, "blue": 0}
            // Adjust parsing according to your JSON structure
            int red = 0, green = 0, blue = 0;

            // Parse JSON values
            // Example parsing, update according to your JSON structure
            JSONObject jsonObject = new JSONObject(jsonColor);
            if (jsonObject.has("red")) {
                red = jsonObject.getInt("red");
            }
            if (jsonObject.has("green")) {
                green = jsonObject.getInt("green");
            }
            if (jsonObject.has("blue")) {
                blue = jsonObject.getInt("blue");
            }

            // Update UI with the color
            int color = Color.rgb(red, green, blue);
            colorLayout.setBackgroundColor(color);

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing JSON color data", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.unsubscribe(mqttTopic);
                mqttClient.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
