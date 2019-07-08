package me.devhelp.unityplugin;

public class Spot {
    public String _id;
    public GeoJsonPoint geometry;
    public SpotProperties properties = new SpotProperties();

    class SpotProperties {
        public String name;
    }
}
