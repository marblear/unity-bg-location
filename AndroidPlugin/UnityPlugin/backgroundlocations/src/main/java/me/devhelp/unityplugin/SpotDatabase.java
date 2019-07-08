package me.devhelp.unityplugin;

import android.location.Location;
import android.util.Log;

import java.util.HashMap;

public class SpotDatabase {
    private HashMap<String, Spot> db = new HashMap<>();
    public static final String LOG_TAG = "SpotDB";

    public void clear() {
        db = new HashMap<>();
    }

    public String upsert(Spot spot) {
        db.put(spot._id, spot);
        return spot._id;
    }

    public Spot getSpotNear(Location location, double distance) {
        double minDistance = Double.MAX_VALUE;
        Spot minDistanceSpot = null;
        for (HashMap.Entry<String, Spot> entry: db.entrySet()) {
            Spot spot = entry.getValue();
            double spotDistance = spot.geometry.haversineDistance(location);
//            Log.d(LOG_TAG, spot.properties.name + " (" + spotDistance + ")");
            if (spotDistance <= distance && spotDistance < minDistance) {
                minDistance = spotDistance;
                minDistanceSpot = spot;
            }
        }
        return minDistanceSpot;
    }
}
