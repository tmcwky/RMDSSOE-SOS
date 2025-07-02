package com.rmdssoe.sos.ShakeServices;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.rmdssoe.sos.Contacts.ContactModel;
import com.rmdssoe.sos.Contacts.DbHelper;
import com.rmdssoe.sos.MainActivity;
import com.rmdssoe.sos.R;

import java.util.List;

public class SensorService extends Service {

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    public SensorService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        // start the foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        /*
        Shaking is not a great idea...

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {
            @Override
            public void onShake(int count) {

                SharedPreferences settings = getSharedPreferences("RescueSettings", MODE_PRIVATE);
                String defaultMessage = getResources().getString(R.string.sms);
                String message = settings.getString("SMSText", defaultMessage);
                Log.i("shake", "shaking " + count);

                // check if the user has shacked
                // the phone for 3 time in a row
                if (count == 3) {

                    // vibrate the phone
                    vibrate();

                    // get last known location and check it's not too old
                    LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    for (String provider : locationManager.getAllProviders()) {
                        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(provider);
                        if (location != null) {
                            if (location.getElapsedRealtimeNanos() > (SystemClock.elapsedRealtimeNanos() - 1e11)) {
                                sendSms(message, location);
                                return;
                            }

                        }
                    }

                    sendSms(message, null);
                }
            }
        });


        // register the listener
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        */

        // register screen listener
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        final BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

/*
    THESE ARE NO LONGER NEEDED
    public void sendSms(String message, Location location){
        // get the SMSManager
        SmsManager smsManager = SmsManager.getDefault();

        // get the list of all the contacts in Database
        DbHelper db = new DbHelper(SensorService.this);
        List<ContactModel> list = db.getAllContacts();

        // send SMS to each contact
        for (ContactModel c : list) {
            String messageLoc = message;
            if (location != null) { messageLoc = messageLoc + " http://maps.google.com/?q=" + location.getLatitude() + "," + location.getLongitude();}

            smsManager.sendTextMessage(c.getPhoneNo(), null, messageLoc, null, null);
        }
    }

    // method to vibrate the phone
    public void vibrate() {

        final Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
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

    }
*/
    // For Build versions higher than Android Oreo, we launch
    // a foreground service in a different way. This is due to the newly
    // implemented strict notification rules, which require us to identify
    // our own notification channel in order to view them correctly.
    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        String appName = getResources().getString(R.string.app_name);

        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_MIN);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent intent = PendingIntent.getActivity(this, 0,
                notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle(appName + " is running")
                .setContentText("Check this notification from time to time")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setContentIntent(intent)
                .build();

        startForeground(2, notification);
    }

    @Override
    public void onDestroy() {
        // create an Intent to call the Broadcast receiver
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, ReactivateService.class);
        this.sendBroadcast(broadcastIntent);
        super.onDestroy();
    }

}

