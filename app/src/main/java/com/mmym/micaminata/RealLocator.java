package com.mmym.micaminata;
import android.location.*;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class RealLocator implements Locator
{
    private GoogleMap _map;

    public RealLocator(GoogleMap googleMap)
    {
        _map = googleMap;
        _map.setMyLocationEnabled(true);
    }

    public Location get()
    {
        return _map.getMyLocation();
    }
}