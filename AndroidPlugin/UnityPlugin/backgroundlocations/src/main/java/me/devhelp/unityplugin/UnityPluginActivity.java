package me.devhelp.unityplugin;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.unity3d.player.UnityPlayerActivity;

import java.util.ArrayList;
import java.util.List;

public class UnityPluginActivity extends UnityPlayerActivity {
    private static final int REQUEST_LOCATION = 1;
    private static final int LOCATION_REQUEST_CODE = 1010;
    public static final String LOG_TAG = "LocationPlugin";
    private String serverUrl;
    private String userId;
    private String userToken;
    private Intent locationIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(LOG_TAG, "UnityPluginActivity:onCreate");
        locationIntent = new Intent(getApplicationContext(), LocationService.class);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "UnityPluginActivity:onDestroy");
        stopLocationService();
    }

    // Old entry point for Unity
    @Deprecated
    public void startLocationService() {
        startLocationService(null, null, null);
    }

    // Entry point for Unity
    public void startLocationService(String serverUrl, String userId, String userToken) {
        this.serverUrl = serverUrl;
        this.userId = userId;
        this.userToken = userToken;
        checkPermissions();
    }

    public void stopLocationService() {
        Log.i(LOG_TAG, "UnityPluginActivity:stopLocationService");
        stopService(locationIntent);
    }

    private boolean hasPermission() {
        return PackageManager.PERMISSION_GRANTED == checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION);
    }

    private void checkPermissions() {
        if (hasPermission()) {
            Log.i(LOG_TAG, "Location updates permission granted.");
            startBackgroundLocationService();
        } else {
            Log.i(LOG_TAG, "Asking user for location updates permission.");
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                boolean locationAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (locationAccepted) {
                    startBackgroundLocationService();
                }
                break;
            default:
        }
    }

    private void startBackgroundLocationService() {
        Log.i(LOG_TAG, "UnityPluginActivity:startLocationService");
        PendingIntent pendingIntent = createPendingResult(REQUEST_LOCATION, new Intent(), PendingIntent.FLAG_UPDATE_CURRENT);
        locationIntent.putExtra(LocationService.PENDING_INTENT, pendingIntent);
        locationIntent.putExtra(LocationService.SERVER_URL, serverUrl);
        locationIntent.putExtra(LocationService.USER_ID, userId);
        locationIntent.putExtra(LocationService.USER_TOKEN, userToken);
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            Log.i(LOG_TAG, "starting service in foreground");
            startForegroundService(locationIntent);
        } else {
            Log.i(LOG_TAG, "starting service in background");
            startService(locationIntent);
        }
    }

    public String getLocationsJson(long time) {
        Log.d(LOG_TAG, "UnityPluginActivity: getLocationsJson after " + time);
        Cursor cursor = getContentResolver().query(LocationContentProvider.CONTENT_URI, null,
                LocationContentProvider.LOCATION_TIME + " > " + time,
                null, null);
        List<LocationDto> locationUpdates = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                LocationDto dto = new LocationDto();
                dto.setTime(cursor.getLong(cursor.getColumnIndex(LocationContentProvider.LOCATION_TIME)));
                dto.setLongitude(cursor.getLong(cursor.getColumnIndex(LocationContentProvider.LOCATION_LONGITUDE)));
                dto.setLatitude(cursor.getLong(cursor.getColumnIndex(LocationContentProvider.LOCATION_LATITUDE)));
                locationUpdates.add(dto);
            }
            cursor.close();
        }
        String json = new Gson().toJson(locationUpdates);
        Log.d(LOG_TAG, "Json: " + json);
        return json;
    }

    public void deleteLocationsBefore(long time) {
        Log.i(LOG_TAG, "UnityPluginActivity: deleteLocationsBefore " + time);
        int count = getContentResolver().delete(LocationContentProvider.CONTENT_URI,
                LocationContentProvider.LOCATION_TIME + " <= " + time,
                null);
        Log.i(LOG_TAG, "Deleted: " + count + "rows");
    }


}
