package com.rmdssoe.sos.ShakeServices;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;

import com.rmdssoe.sos.Contacts.ContactModel;
import com.rmdssoe.sos.Contacts.DbHelper;
import com.rmdssoe.sos.R;


import java.util.List;

public class ScreenReceiver extends BroadcastReceiver {
    public static boolean wasScreenOn = true;
    int count = 0;
    long time = 0, timeDiff = 0;

    public ScreenReceiver(){};

    @Override
    public void onReceive(final Context context, final Intent intent) {
        //Log.e("tag", count+"");

        SharedPreferences settings = context.getSharedPreferences("RescueSettings", context.MODE_PRIVATE);
        String defaultMessage = context.getResources().getString(R.string.default_sms_text);
        String message = settings.getString("SMSText", defaultMessage);

        // Log.i("screen", "screen input");

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            count++;
            wasScreenOn = false;
            timeDiff = System.currentTimeMillis() - time;
            //Log.e("tag",timeDiff + "");
            time = System.currentTimeMillis();
            if(timeDiff >= 1200)
                count = 0;

            if(count > 4){
                count = 0;
                notify_and_send(context, message);
                // Log.e("screen", "success");
            }

        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            count++;
            wasScreenOn = true;
            timeDiff = System.currentTimeMillis() - time;
            //Log.e("tag",timeDiff + "");
            time = System.currentTimeMillis();
            if(timeDiff >= 1200)
                count = 0;

            if(count > 4){
                count = 0;
                notify_and_send(context, message);
                // Log.i("screen", "success");
            }

        }
    }

    @SuppressLint("MissingPermission")
    public void notify_and_send(final Context context, String message){
        // vibrate the phone
        final Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        VibrationEffect vibEff;

        // Android Q and above have some predefined vibrating patterns
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // vibEff = VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK);
            vibEff = VibrationEffect.createOneShot(1500, VibrationEffect.DEFAULT_AMPLITUDE);
            vibrator.cancel();
            vibrator.vibrate(vibEff);
        } else {
            vibrator.vibrate(1500);
        }


        // get last known location and check it's not too old
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        for (String provider: locationManager.getAllProviders()) {
            Location location = locationManager.getLastKnownLocation(provider);
            //Log.i("location", provider + " " + location);
            if (location != null) {
                if (location.getElapsedRealtimeNanos() > (SystemClock.elapsedRealtimeNanos() - 1e11)) {
                    sendSms(context, message, location);
                    return;
                }
            }
        }
        sendSms(context, message, null);

        LocationListener locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(android.location.Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                if (location.hasAccuracy()) {
                    if (location.getAccuracy() < 10) {
                        locationManager.removeUpdates(this);
                    }
                } else {
                    locationManager.removeUpdates(this);
                }
                sendSms(context, message, location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
        for (String provider: locationManager.getAllProviders()) {
            locationManager.requestLocationUpdates(provider,
                    2000, 0, locationListener);
        }
    }

    public void sendSms(Context context, String message, Location location){
        // get the SMSManager
        SmsManager smsManager = SmsManager.getDefault();

        // Log.i("yeee", "sending message " + location);

        // get the list of all the contacts in Database
        DbHelper db = new DbHelper(context);
        List<ContactModel> list = db.getAllContacts();

        // send SMS to each contact
        for (ContactModel c : list) {
            String messageLoc = message;
            if (location != null) { messageLoc = messageLoc + " http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();}
            smsManager.sendTextMessage(c.getPhoneNo(), null, messageLoc, null, null);
        }
    }
}