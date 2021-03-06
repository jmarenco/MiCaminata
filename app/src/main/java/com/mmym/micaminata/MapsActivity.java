package com.mmym.micaminata;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.provider.Settings;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.view.*;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Date;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener
{
    private GoogleMap _map = null;
    private Button _boton;
    private TextView _texto;
    private TextView _status;

    private LocationManager _locationManager;
    private Recorrido _recorrido = null;
    private String _version = "0.91";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        _boton = (Button)findViewById(R.id.button);
        _texto = (TextView)findViewById(R.id.textView);
        _status = (TextView)findViewById(R.id.textStatus);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        status("Versión " + _version);
        texto("Mi caminata!");

        // get Gps location service LocationManager object
        try
        {
            _locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, this);
        }
        catch (SecurityException e)
        {
            toast("No se pudo inicializar el LocationManager! " + e.getMessage());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        _map = googleMap;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        _actual = location;
        _posiciones += 1;

        if( _map != null )
            _map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 16.0f));

        if( _recorrido == null )
            status(String.format("Posición: (%.3f, %.3f) - Versión: " + _version, location.getLatitude(), location.getLongitude()));

        if( _recorrido != null )
        {
            actualizarRecorrido(location);
            actualizarTextos();
        }
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        toast("El GPS se ha desactivado!");
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        toast("Se activó el GPS!");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    // Ubicación actual
    private Location _actual;

    // Control de la aplicación durante el apagado de la pantalla
    private PowerManager.WakeLock _wakelock;

    // Click del botón principal
    public void onClick(View view)
    {
        if (_recorrido == null )
        {
            // Iniciamos un nuevo recorrido
            _recorrido = new Recorrido(_actual);
            _polyoptions = null;
            _boton.setText("Stop");

            // Wake lock para mantener la aplicación corriendo cuando se apague la pantalla
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            _wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Mi Caminata");
            _wakelock.acquire();

            // Inicia el timer
            _startTime = System.currentTimeMillis();

            // Información inicial
            toast("Recorrido iniciado!");
            texto("A caminar!");
        }
        else
        {
            // Información final
            toast("Recorrido finalizado!");
            guardarRecorrido();

            // Libera el wake lock
            _recorrido = null;
            _boton.setText("Start!");
            _wakelock.release();
        }
    }

    // Hora de inicio y parámetros del timer
    private long _startTime = 0;

    // Ruta
    private PolylineOptions _polyoptions;
    private Polyline _polyline;
    private int _posiciones = 0;

    // Actualiza el recorrido sobre el mapa
    private void actualizarRecorrido(Location location)
    {
        if (location != null)
        {
            if (_polyoptions == null)
            {
                _polyoptions = new PolylineOptions();
                _polyoptions.color(Color.RED);
                _polyoptions.width(2);

                for (Tick tick : _recorrido.getTicks())
                    _polyoptions.add(tick.getPosicion());
            }

            LatLng posicion = new LatLng(location.getLatitude(), location.getLongitude());

            _recorrido.agregar(new Date(), location);
            _polyoptions.add(posicion);

            if (_polyline != null)
                _polyline.remove();

            _polyline = _map.addPolyline(_polyoptions);
            _map.moveCamera(CameraUpdateFactory.newLatLng(posicion));
        }
    }

    // Actualiza la información en pantalla sobre el recorrido
    private void actualizarTextos()
    {
        long millis = System.currentTimeMillis() - _startTime;
        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        int hours = minutes / 60;

        minutes = minutes % 60;
        seconds = seconds % 60;

        double velocidad = _recorrido.distancia() / (millis / 3600000.0);

        texto(String.format("%02d:%02d:%02d - %.2f km", hours, minutes, seconds, _recorrido.distancia()));
        status(String.format("%.2f km/h - Locs: %d - Pts: %d", velocidad, _posiciones, _recorrido.getTicks().size()));
    }

    public void toast(String mensaje)
    {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
    public void texto(String mensaje)
    {
        _texto.setText(mensaje);
    }
    public void status(String mensaje)
    {
        _status.setText(mensaje);
    }

    private void guardarRecorrido()
    {
        final Recorrido cache = _recorrido;
        final MapsActivity thisMaps = this;

        new AlertDialog.Builder(MapsActivity.this)
                .setTitle("Guardar ruta")
                .setMessage("Querés guardar la ruta en el teléfono?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        EscritorRecorrido escritor = new EscritorRecorrido(cache, thisMaps);
                        escritor.escribir();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        // No hacemos nada
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public void onClose(View view)
    {
        this.finish();
    }
}