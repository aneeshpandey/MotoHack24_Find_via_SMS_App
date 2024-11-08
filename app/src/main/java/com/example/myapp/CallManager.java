package com.example.myapp;


import static java.security.AccessController.getContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;

public class CallManager {
    private static final String TAG = CallManager.class.getSimpleName();
    private final AudioManager audioManager;
    private final TelecomManager telecomManager;
    private final TelephonyManager telephonyManager;

    public CallManager(Context context) {
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.telecomManager = (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public void autoAnswerCall() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Listen to the call state
                if (telephonyManager != null) {
                    telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                }
            } else {
                Log.e(TAG, "Auto-answer not supported below Android O.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in auto-answering call: " + e.getMessage(), e);
        }
    }

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @SuppressLint("MissingPermission")
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);

            // Handle the ringing state
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                setSpeakerMode(true);
                // Answer the call
                if (telecomManager != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        telecomManager.acceptRingingCall();
                    }
                }
                // Set speakerphone mode after call is accepted
                setSpeakerMode(true);
                Log.d(TAG, "Call answered and speaker mode enabled.");
            }
            // Handle the idle state (call ended)
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                // Disable speakerphone mode when the call ends
                setSpeakerMode(false);
                Log.d(TAG, "Call ended, speaker mode disabled.");
            }
        }
    };

    private void setSpeakerMode(boolean enabled) {
        if (audioManager != null) {
            // Ensure we're in the call mode first
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            // Set the speakerphone state
            audioManager.setSpeakerphoneOn(enabled);
            Log.d(TAG, "Speaker mode set to: " + enabled);
        }
    }
}

