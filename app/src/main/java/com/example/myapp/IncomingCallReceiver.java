package com.example.myapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;


public class IncomingCallReceiver extends BroadcastReceiver {
    private static final String TAG = IncomingCallReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            Log.d(TAG, "Incoming call detected. Attempting to auto-answer.");

            CallManager callManager = new CallManager(context);
            callManager.autoAnswerCall();
        }
    }
}


