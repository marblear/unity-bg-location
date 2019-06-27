package me.devhelp.unityplugin;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import static me.devhelp.unityplugin.UnityPluginActivity.LOG_TAG;

public class LocationService extends Service {
    public static final String PENDING_INTENT = "pendingIntent";
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final String CHANNEL_ID = "com.marblear.prototype.Notifications";
    private static final int ONGOING_NOTIFICATION_ID = 1;

    private LocationManager locationManager;

    private LocationListener gpsListener = new LocationListener();
    private LocationListener networkListener = new LocationListener();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "LocationService:onCreate");
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            setToForeground();
        }
        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }
    @TargetApi(26)
    private void setToForeground() {
        Log.d(LOG_TAG, "LocationService:setToForeground");
        Intent notificationIntent = new Intent(this, UnityPluginActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Notification channel", importance);
        channel.setDescription("Notification channel description");
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
        Notification notification =
                new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("Title")
                        .setContentText("Message")
                        //.setSmallIcon(R.drawable.icon)
                        .setContentIntent(pendingIntent)
                        .setTicker("Ticker text")
                        .build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "LocationService:onDestroy");
        if (locationManager != null) {
            try {
                locationManager.removeUpdates(gpsListener);
                locationManager.removeUpdates(networkListener);
            } catch (SecurityException ex) {
                Log.w(LOG_TAG, "fail to remove location listeners, ignore", ex);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "LocationService:onStartCommand flags = " + flags + " startId = " + startId);
        if (startId == 1) {
            Log.d(LOG_TAG, "initializing GPS location provider");
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        gpsListener);
            } catch (SecurityException ex) {
                Log.w(LOG_TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.w(LOG_TAG, "provider does not exist, " + ex);
            } catch (Exception ex) {
                Log.w(LOG_TAG, "GPS provider could not be initialized " + ex);
            }
            Log.d(LOG_TAG, "initializing network location provider");
            try {
                locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                        networkListener);
            } catch (SecurityException ex) {
                Log.w(LOG_TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.w(LOG_TAG, "provider does not exist, " + ex);
            }
        }
        saveStartLocation();
        return START_STICKY;
    }

    private void saveStartLocation() {
        Location lastKnownLocation;
        try {
            lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            saveLocation(lastKnownLocation);
        } catch (SecurityException ex) {
            Log.e(LOG_TAG, "fail to request initial location ", ex);
        } catch (IllegalArgumentException ex) {
            Log.e(LOG_TAG, "provider does not exist " + ex);
        }
    }

    private void saveLocation(Location location) {
        if (location == null) return;

        ContentValues values = new ContentValues();
        values.put(LocationContentProvider.LOCATION_TIME, System.currentTimeMillis());
        values.put(LocationContentProvider.LOCATION_LATITUDE, location.getLatitude());
        values.put(LocationContentProvider.LOCATION_LONGITUDE, location.getLongitude());
        Uri uri = getContentResolver().insert(LocationContentProvider.CONTENT_URI, values);
        Log.d(LOG_TAG, "inserted new location, location: " + location);
    }

    private class LocationListener implements android.location.LocationListener {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(LOG_TAG, "LocationListener:onLocationChanged: " + location);
            saveLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.w(LOG_TAG, "LocationListener: providerDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.i(LOG_TAG, "LocationListener: providerEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int i, Bundle bundle) {
            Log.d(LOG_TAG, "LocationListener: statusChanged" + provider);
        }
    }

}
