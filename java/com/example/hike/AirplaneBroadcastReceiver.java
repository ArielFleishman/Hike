package com.example.hike;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

public class AirplaneBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Get the current state of airplane mode
        boolean isAirplaneModeOn = Settings.System.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;

        if (isAirplaneModeOn) {
            // Phone is in airplane mode
            Toast.makeText(context, "Airplane mode is on, please turn it off", Toast.LENGTH_SHORT).show();
        } else {
            // Phone is not in airplane mode
            Toast.makeText(context, "Airplane mode is off", Toast.LENGTH_SHORT).show();
        }
    }
}