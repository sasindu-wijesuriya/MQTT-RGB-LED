package com.example.mqtt_rgb_led;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import yuku.ambilwarna.AmbilWarnaDialog;

// In SecondActivity.java
public class SelectedDeviceColorActivity extends AppCompatActivity implements MqttHandler.MqttMessageListener{
    public TextView textView;

    private static final String BROKER_URL = "tcp://test.mosquitto.org:1883";
    private static final String CLIENT_ID = "sasinduwije1";
    private static final String topic = "device/device1/user/sasinduwije1";
    private MqttHandler mqttHandler;

    private int currentRed = 0;
    private int currentGreen = 0;
    private int currentBlue = 0;
    private int currentColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_device_color);

        textView = findViewById(R.id.textView);

        Intent intent = getIntent();
        String receivedData = intent.getStringExtra("mqttTopic");

//        textView.setText(receivedData);

        mqttHandler = new MqttHandler();
        mqttHandler.setMessageListener(this);
        mqttHandler.connect(BROKER_URL,CLIENT_ID);

        subscribeToTopic(topic);

        Button btnPickColor = findViewById(R.id.btnPickColor);
        btnPickColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openColorPicker();;
            }
        });
    }

    private void openColorPicker() {
        AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, currentColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {
                // Do nothing on cancel
            }

            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                // Store the selected color
                currentColor = color;

                // Get RGB values
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);

                System.out.println("Red: " + red);
                System.out.println("Green: " + green);
                System.out.println("Blue: " + blue);

                publishColor(red, green, blue);

            }
        });
        colorPicker.show();
    }


    private void publishColor(int red, int green, int blue) {
        JSONObject json = new JSONObject();
        try {
            json.put("red", red);
            json.put("green", green);
            json.put("blue", blue);
            String message = json.toString();
            publishMessage(topic, message);
            Toast.makeText(this, "Published msg : " + message + " to topic : " + topic, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onMessageReceived(String message) {
//        setTextViewToNewReceivedMsg(message);

        try {
            JSONObject json = new JSONObject(message);
            int red = json.getInt("red");
            int green = json.getInt("green");
            int blue = json.getInt("blue");

            setViewBackgroundColor(red, green, blue);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    private void setViewBackgroundColor(int red, int green, int blue) {
        View colorView = findViewById(R.id.colorView); // Assuming you have a View in your layout with id colorView
        colorView.setBackgroundColor(Color.rgb(red, green, blue));
    }


}
