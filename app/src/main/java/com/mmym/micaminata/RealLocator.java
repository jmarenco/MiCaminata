package com.mmym.micaminata;
import android.content.Context;
import android.location.*;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

@Deprecated
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