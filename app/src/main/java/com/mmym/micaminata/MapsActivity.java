package com.mmym.micaminata;

import android.graphics.Color;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import android.view.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.Date;
import java.text.DateFormat;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap _map;
    private Button _boton;
    private TextView _texto;
    private TextView _status;

    private Recorrido _recorrido = null;
    private Locator _locator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        _boton = (Button)findViewById(R.id.button);
        _texto = (TextView)findViewById(R.id.textView);
        _status = (TextView)findViewById(R.id.bottomView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        // Context context = getApplicationContext();
        // Toast.makeText(context, "onMapReady()", Toast.LENGTH_SHORT).show();

        _map = googleMap;
        _locator = new MockLocator(_map);

        if( mostrarUbicacion() == false)
            _timerHandler.postDelayed(_timerRunnable, _tryTime);
    }

    private boolean mostrarUbicacion()
    {
        Location location = _locator.get();

        if (location != null)
        {
            LatLng posicion = new LatLng(location.getLatitude(), location.getLongitude());

            _map.addMarker(new MarkerOptions().position(posicion).title("Inicio"));
            _map.moveCamera(CameraUpdateFactory.newLatLng(posicion));
        }

        return location != null;
    }

    public void onClick(View view)
    {
        if (_recorrido == null )
        {
            _recorrido = new Recorrido();
            _boton.setText("TERMINAR");
            _status.setText(String.format("Actualizacion: %.2f seg", _tickTime / 1000.0));
            _startTime = System.currentTimeMillis();
            _timerHandler.postDelayed(_timerRunnable, _tickTime);
        }
        else
        {
            guardarRecorrido();

            _recorrido = null;
            _boton.setText("COMENZAR");
            _timerHandler.removeCallbacks(_timerRunnable);
        }
    }

    // Hora de inicio del timer
    private long _startTime = 0;
    private long _tickTime = 20000;
    private long _tryTime = 2000;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler _timerHandler = new Handler();
    Runnable _timerRunnable = new Runnable()
    {
        private int _posiciones = 0;
        private int _totales = 0;

        @Override
        public void run()
        {
            if (_recorrido == null )
            {
                // Estamos al comienzo de la aplicacion, intentando mostrar la ubicacion actual
                intentarUbicacion();
            }
            else
            {
                // Estamos registrando el recorrido
                actualizarRecorrido();
                actualizarTextos();

                _timerHandler.postDelayed(this, _tickTime);
            }
        }

        private Polyline _polyline;

        private void intentarUbicacion()
        {
            if (mostrarUbicacion() == false)
                _timerHandler.postDelayed(_timerRunnable, _tryTime);
            else
                 _timerHandler.removeCallbacks(_timerRunnable);
        }

        private void actualizarRecorrido()
        {
            Location location = _locator.get();
            if (location != null)
            {
                Date ahora = new Date(); // _calendar.getTime()
                LatLng posicion = new LatLng(location.getLatitude(), location.getLongitude());

                _recorrido.agregar(ahora, location);

                PolylineOptions options = new PolylineOptions();
                options.color(Color.RED);
                options.width(2);

                for(Tick tick: _recorrido.getTicks())
                    options.add(tick.getPosicion());

                if (_polyline != null)
                    _polyline.remove();

                _polyline = _map.addPolyline(options);
                _map.moveCamera(CameraUpdateFactory.newLatLng(posicion));
                _posiciones += 1;
            }

            _totales += 1;
        }

        private void actualizarTextos()
        {
            long millis = System.currentTimeMillis() - _startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;

            minutes = minutes % 60;
            seconds = seconds % 60;

            double velocidad = _recorrido.distancia() / (millis / 1000.0);

            _texto.setText(String.format("%02d:%02d - %.2f km", hours, minutes, _recorrido.distancia()));
            _status.setText(String.format("%.2f km/h -  Locs: %d/%d", velocidad, _posiciones, _totales));
        }
    };

//    @Override
//    public void onPause()
//    {
//        super.onPause();
//
//        _timerHandler.removeCallbacks(_timerRunnable);
//        _boton.setText("RECOMENZAR");
//    }

    private void guardarRecorrido()
    {
        EscritorRecorrido escritor = new EscritorRecorrido(_recorrido, _status);
        escritor.escribir();
    }

    public void onClose(View view)
    {
        _timerHandler.removeCallbacks(_timerRunnable);
        this.finish();
    }
}