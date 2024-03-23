package com.example.mqtt_rgb_led;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends AppCompatActivity {

    private ListView listView;
    private List<String> deviceList;
    private ArrayAdapter<String> adapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        listView = findViewById(R.id.LVDevices);
        deviceList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(adapter);

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            // User not signed in, go back to MainActivity
            Toast.makeText(this, "User not signed in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userName = user.getEmail().split("@")[0]; // Get username before '@' sign
        String sanitizedUserName = sanitizeUserName(userName);
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(sanitizedUserName).child("devices");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                deviceList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String deviceName = dataSnapshot.getKey();
                    deviceList.add(deviceName);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(DeviceListActivity.this, "Failed to fetch devices: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        // Add click listener to the ListView items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDevice = deviceList.get(position);
                String mqttTopic = "device/" + selectedDevice + "/user/" + sanitizedUserName;
                Toast.makeText(DeviceListActivity.this, "MQTT Topic: " + mqttTopic, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(DeviceListActivity.this, SelectedDeviceColorActivity.class);
                intent.putExtra("mqttTopic", mqttTopic);
                startActivity(intent);
            }
        });
    }

    // Helper method to sanitize the userName
    private String sanitizeUserName(String userName) {
        return userName.replace(".", "-")
                .replace("#", "-")
                .replace("$", "-")
                .replace("[", "-")
                .replace("]", "-");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_device_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_add_device) {
            // Open AddDeviceActivity when the "Add Device" button is clicked
            Intent intent = new Intent(this, AddDeviceActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
