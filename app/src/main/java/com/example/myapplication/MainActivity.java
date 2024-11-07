package com.example.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private TextView smsSenderTextView;
    private TextView smsBodyTextView;

    private Button startTimer;
    private BroadcastReceiver smsReceiver;

    private Timer timer;

    private AlarmSoundPlayer alarmSoundPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        smsSenderTextView = findViewById(R.id.smsSender);
        smsBodyTextView = findViewById(R.id.smsBody);
        startTimer = findViewById(R.id.button);
        timer = new Timer();
        alarmSoundPlayer = new AlarmSoundPlayer();

        int REQUEST_LOCATION = 99;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }

        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String sender = intent.getStringExtra("sender");
                String message = intent.getStringExtra("message");

                smsSenderTextView.setText("Sender: " + sender);
                smsBodyTextView.setText("Message: " + message);

                if(message.equals("send location, 1001!")) {
                    getLocationAndSendSMS(sender);
                } else if(message.equals("play alarm, 1001!")) {
                    alarmSoundPlayer.playAlarmSound(MainActivity.this);
                    // Define the TimerTask that calls the method after 10 seconds
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // Call your method after the delay
                            alarmSoundPlayer.stopAlarmSound();
                        }
                    }, 10000); // 5000 milliseconds = 5 seconds
                }
            }
        };
        IntentFilter filter = new IntentFilter("sms_received");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(smsReceiver, filter, Context.RECEIVER_EXPORTED);
        }

        startTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Define the TimerTask that calls the method after 5 seconds
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Call your method after the delay
                        getLocationAndSendSMS("9006491190");
                    }
                }, 5000); // 5000 milliseconds = 5 seconds
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }

    private void getLocationAndSendSMS(String sender) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String locationMessage = "My current location: https://www.google.com/maps?q=" + latitude + "," + longitude;
                            locationMessage = getBatteryLevelAndSendSMS() + "\n" + locationMessage;

                            // Send the location via SMS
                            sendSMS(sender, locationMessage);
                        } else {
                            Toast.makeText(MainActivity.this, "Unable to get location", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Location request failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Toast.makeText(this, "Location sent via SMS", Toast.LENGTH_SHORT).show();
    }

    private String getBatteryLevelAndSendSMS() {
        // Register for battery change updates
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, filter);

        // Get battery level from the batteryStatus Intent
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Calculate battery percentage
        float batteryPercentage = ((float) level / (float) scale) * 100;

        // Create the message to send via SMS
        String batteryMessage = "My current battery level is: " + (int) batteryPercentage + "%";

        return batteryMessage;
    }
}