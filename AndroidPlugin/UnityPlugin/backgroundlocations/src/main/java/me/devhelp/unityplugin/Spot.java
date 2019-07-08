package me.devhelp.unityplugin;

import android.location.Location;

public class Spot {
    public String _id;
    public GeoJsonPoint geometry;
    public SpotProperties properties = new SpotProperties();

    class SpotProperties {
        public String name;
    }
}
