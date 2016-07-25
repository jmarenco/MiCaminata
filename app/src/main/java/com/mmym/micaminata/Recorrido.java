package com.mmym.micaminata;

import android.content.Context;
import android.location.*;

import com.google.android.gms.maps.model.LatLng;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Recorrido
{
    private ArrayList<Tick> _ticks;
    private LatLng _ultima;
    private double _distancia;

    public Recorrido(Location inicio)
    {
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

        Tick tick = new Tick(timestamp, location);
        _ticks.add(tick);

        if (_ultima != null)
            _distancia += distancia(_ultima, tick.getPosicion());

        _ultima = tick.getPosicion();
    }

    private double distancia(LatLng primera, LatLng segunda)
    {
        double earthRadius = 6371000; // meters
        double dLat = Math.toRadians(segunda.latitude - primera.latitude);
        double dLng = Math.toRadians(segunda.longitude - primera.longitude);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) + Math.cos(Math.toRadians(primera.latitude)) * Math.cos(Math.toRadians(segunda.latitude)) * Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return earthRadius * c / 1000.0; // km
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
