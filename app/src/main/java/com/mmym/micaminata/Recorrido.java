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

        if (_ultima != null)
            _distancia += _ultima.distanceTo(location);

        _ticks.add(new Tick(timestamp, location));
        _ultima = location;
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
