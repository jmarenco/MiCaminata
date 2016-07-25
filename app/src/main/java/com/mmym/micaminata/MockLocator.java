package com.mmym.micaminata;

import android.location.*;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public class MockLocator implements Locator
{
    private GoogleMap _map;
    private Location _anterior;
    private boolean _inicial;

    public MockLocator(GoogleMap googleMap)
    {
        _map = googleMap;
        _map.setMyLocationEnabled(true);
        _anterior = null;
    }

    public Location get()
    {
        if (_anterior == null )
        {
            _anterior = _map.getMyLocation();
        }
        else
        {
            _anterior.setLatitude(_anterior.getLatitude() + 0.001);
            _anterior.setLongitude(_anterior.getLongitude() + 0.001);
        }

        return _anterior;
    }
}
