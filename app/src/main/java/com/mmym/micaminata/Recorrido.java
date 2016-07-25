package com.mmym.micaminata;

import com.google.android.gms.maps.model.LatLng;

import android.location.*;

import java.util.ArrayList;
import java.util.Date;

public class Recorrido
{
    private ArrayList<Tick> _ticks;
    private Location _ultima;
    private double _distancia;

    public Recorrido()
    {
        _ticks = new ArrayList<Tick>();
        _ultima = null;
        _distancia = 0;
    }

    public void agregar(Date timestamp, Location location)
    {
        if (location == null)
            return;

        if (_ultima != null)
            _distancia += _ultima.distanceTo(location);

        _ticks.add(new Tick(timestamp, location));
        _ultima = location;
    }

    public double distancia()
    {
        return _distancia;
    }
}
