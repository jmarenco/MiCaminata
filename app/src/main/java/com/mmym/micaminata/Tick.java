package com.mmym.micaminata;

import com.google.android.gms.maps.model.LatLng;

import android.location.*;

import java.util.Date;

public class Tick
{
    private Date _timestamp;
    private Location _location;
    private LatLng _posicion;

    public Tick(Date timestamp, Location location)
    {
        _timestamp = timestamp;
        _location = location;
        _posicion = new LatLng(location.getLatitude(), location.getLongitude());
    }

    public Date getTimestamp()
    {
        return _timestamp;
    }
    public LatLng getPosicion()
    {
        return _posicion;
    }
    public Location getLocation()
    {
        return _location;
    }
}