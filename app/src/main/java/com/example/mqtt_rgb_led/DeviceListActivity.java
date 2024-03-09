package com.example.mqtt_rgb_led;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
        userName = userName.replace(".", "-");
        userName = userName.replace("#", "-");
        userName = userName.replace("$", "-");
        userName = userName.replace("[", "-");
        userName = userName.replace("]", "-");

        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userName).child("devices");

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
    }
}
