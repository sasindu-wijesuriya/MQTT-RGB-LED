package com.example.mqtt_rgb_led;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// In SecondActivity.java
public class SelectedDeviceColorActivity extends AppCompatActivity {
    public TextView textView;

    private static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "sasinduwije1";
    private static final String topic = "device/device1/user/sasinduwije1";
    private MqttHandler mqttHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_device_color);

        textView = findViewById(R.id.textView);

        Intent intent = getIntent();
        String receivedData = intent.getStringExtra("mqttTopic");

        textView.setText(receivedData);

        mqttHandler = new MqttHandler();
        mqttHandler.connect(BROKER_URL,CLIENT_ID);

        subscribeToTopic(topic);

    }


    @Override
    protected void onDestroy() {
        mqttHandler.disconnect();
        super.onDestroy();

    }
    private void publishMessage(String topic, String message){
        Toast.makeText(this, "Publishing message: " + message, Toast.LENGTH_SHORT).show();
        mqttHandler.publish(topic,message);
    }
    private void subscribeToTopic(String topic){
        Toast.makeText(this, "Subscribing to topic "+ topic, Toast.LENGTH_SHORT).show();
        mqttHandler.subscribe(topic);
    }

    public void setTextViewToNewReceivedMsg(String msg) {
        runOnUiThread(() -> textView.setText(msg));
    }

}
