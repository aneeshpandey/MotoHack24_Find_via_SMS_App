package com.example.myapp;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import android.os.Binder;

import java.util.Timer;
import java.util.TimerTask;

public class RespondService extends Service {
    private static boolean UNAUTHORISED_MODE = false;
    private BroadcastReceiver smsReceiver;

    private Timer timer;

    private AlarmSoundPlayer alarmSoundPlayer;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    // Class used for the client Binder
    public class LocalBinder extends Binder {
        public RespondService getService() {
            return RespondService.this;
        }
    }

    //private static final String CHANNEL_ID = "respond_service_channel";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();

        CharSequence name = "MyApp Channel";
        String description = "Channel for Foreground Service";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel("MY_CHANNEL_ID", name, importance);
        channel.setDescription(description);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        // Create a notification for the foreground service
        Notification notification = new NotificationCompat.Builder(this, "MY_CHANNEL_ID")
                .setContentTitle("RespondService")
                .setContentText("Service is running...")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .build();

        // Start the service as a foreground service
        startForeground(1, notification);

        timer = new Timer();
        alarmSoundPlayer = new AlarmSoundPlayer();

        smsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String sender = intent.getStringExtra("sender");
                String message = intent.getStringExtra("message");

                MainActivity.smsSenderTextView.setText("Sender: " + sender);
                MainActivity.smsBodyTextView.setText("Message: " + message);

                if(message.equals("Send location, 1001!")) {
                    getLocationAndSendSMS(sender,false);
                } else if(message.equals("Play alarm, 1001!")) {
                    alarmSoundPlayer.playAlarmSound(RespondService.this);
                    // Define the TimerTask that calls the method after 10 seconds
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            // Call your method after the delay
                            alarmSoundPlayer.stopAlarmSound();
                        }
                    }, 3000);
                } else if(message.equals("Unlock device, 1001!")) {
                    if(UNAUTHORISED_MODE) {
                        switchToMainActivity();
                        Toast.makeText(RespondService.this, "Device unlocked", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RespondService.this, "Device already unlocked", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter("sms_received");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(smsReceiver, filter, Context.RECEIVER_EXPORTED);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void sendSmsAfter5Sec() {
        // Define the TimerTask that calls the method after 5 seconds
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Call your method after the delay
                getLocationAndSendSMS("9006491190",false);
            }
        }, 5000); // 5000 milliseconds = 5 seconds
    }

    private void getLocationAndSendSMS(String sender, boolean isEmergencyVar) {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double latitude = location.getLatitude();
                            double longitude = location.getLongitude();
                            String locationMessage = "Location: https://www.google.com/maps?q=" + latitude + "," + longitude;
                            locationMessage = getBatteryLevelAndSendSMS() + "\n" + locationMessage;
                            String s = "";
                            if(isEmergencyVar) {
                                s = "UNAUTHORISED ACCESS ATTEMPT DETECTED! DEVICE LOCKED!\n\n";
                            }
                            locationMessage = s.concat(locationMessage);
                            System.out.println("aneesh +" + isEmergencyVar);
                            // Send the location via SMS
                            sendSMS(sender, locationMessage);
                        } else {
                            System.out.println("Location is null. Unable to retrieve location.");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("Location request failed: " + e.getMessage());
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
        String batteryMessage = "Battery level is: " + (int) batteryPercentage + "%";

        return batteryMessage;
    }

    public void unauthorisedModeStarted() {
        UNAUTHORISED_MODE = true;
        alarmSoundPlayer.playAlarmSound(RespondService.this);
        getLocationAndSendSMS("9006491190", true);
        // Define the TimerTask that calls the method after 10 seconds
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Call your method after the delay
                alarmSoundPlayer.stopAlarmSound();
            }
        }, 5000);
    }

    private void switchToMainActivity() {
        Intent closeActivityIntent = new Intent("com.example.CLOSE_UNAUTHORIZED_ACTIVITY");
        sendBroadcast(closeActivityIntent);
        UNAUTHORISED_MODE = false;

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(smsReceiver);
    }
}
