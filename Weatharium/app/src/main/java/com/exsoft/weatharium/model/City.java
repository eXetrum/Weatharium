package com.exsoft.weatharium.model;

import java.util.Comparator;

/**
 * Created by eXetrum on 03.12.2015.
 */
public class City {
    private int id;
    private String name, country, lon, lat;

    public City() {}

    public City(int id, String name, String country, String lon, String lat) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.lon = lon;
        this.lat = lat;
    }

    public int ID() {
        return  id;
    }

    public String Name() {
        return name;
    }

    public String Country() {
        return country;
    }

    public String Longitude() {
        return lon;
    }

    public String Latitude() {
        return lat;
    }

    @Override
    public String toString() {
        return name + ", " + country;
    }

}
