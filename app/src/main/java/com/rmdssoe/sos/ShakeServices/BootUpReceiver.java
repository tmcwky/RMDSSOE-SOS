package com.rmdssoe.sos.ShakeServices;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.rmdssoe.sos.MainActivity;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, SensorService.class), PendingIntent.FLAG_UPDATE_CURRENT);
        //am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 2000, 10000, pi);

        Log.d("Check: ","Receiver Started");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, SensorService.class));
        } else {
            context.startService(new Intent(context, SensorService.class));
        }

        /*
        ** Maybe show app to user?
        Intent target = new Intent(context, MainActivity.class);
        target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(target);
         */
    }
}
