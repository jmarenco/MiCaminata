package com.mmym.micaminata;

import android.content.Context;
import android.location.*;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Recorrido
{
    private ArrayList<Tick> _ticks;
    private Location _ultima;
    private double _distancia;

    private MapsActivity debug;

    public Recorrido(Location inicio, MapsActivity parent)
    {
        debug = parent;

        _ticks = new ArrayList<Tick>();
        _ultima = null;
        _distancia = 0;

        if (inicio != null)
            agregar(new Date(), inicio);
    }

    public void agregar(Date timestamp, Location location)
    {
        if (location == null)
            return;

        if (_ultima != null)
            _distancia += distancia(_ultima, location);

        _ticks.add(new Tick(timestamp, location));
        _ultima = location;
    }

    private double distancia(Location primera, Location segunda)
    {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(segunda.getLatitude()-primera.getLatitude());
        double dLng = Math.toRadians(segunda.getLongitude()-primera.getLongitude());
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(primera.getLatitude())) * Math.cos(Math.toRadians(segunda.getLatitude())) * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double ret = earthRadius * c;

        debug.status(String.format("prim = (%f, %f), seg = (%f, %f) - dLat = %f - dLng = %f - a = %f - c = %f - ret = %f",
                primera.getLatitude(), primera.getLongitude(), segunda.getLatitude(), segunda.getLongitude(), dLat, dLng, a, c, ret));

        return ret;
    }

    public double distancia()
    {
        return _distancia;
    }

    public ArrayList<Tick> getTicks()
    {
        return _ticks;
    }
}
