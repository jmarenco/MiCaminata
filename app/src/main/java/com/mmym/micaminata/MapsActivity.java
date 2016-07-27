package com.mmym.micaminata;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Location;
import android.os.PowerManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.os.Handler;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{
    private GoogleMap _map;
    private Button _boton;
    private TextView _texto;
    private TextView _status;

    private Recorrido _recorrido = null;
    private Locator _locator;
    private String _version = "0.89";

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        _map = googleMap;
        _locator = new RealLocator(_map);

        if( ubicarInicio() == false)
            _timerHandler.postDelayed(_timerRunnable, _tryTime);
    }

    // Ubicación actual
    private Location _inicio;
    private int _intentos = 0;

    // Intenta obtener la ubicación actual
    private boolean ubicarInicio()
    {
        if (_inicio == null )
        {
            _inicio = _locator.get();

            if (_inicio != null)
            {
                LatLng posicion = new LatLng(_inicio.getLatitude(), _inicio.getLongitude());
                _map.moveCamera(CameraUpdateFactory.newLatLngZoom(posicion, 16.0f));

                status(String.format("Inicio: (%.3f, %.3f)", _inicio.getLatitude(), _inicio.getLongitude()));
            }
            else
            {
                _intentos += 1;
                status("Versión " + _version + " - Obteniendo ubicación ... [" + _intentos + "]");
            }
        }

        return _inicio != null;
    }

    // Control de la aplicación durante el apagado de la pantalla
    private PowerManager.WakeLock _wakelock;

    // Click del botón principal
    public void onClick(View view)
    {
        if (_recorrido == null )
        {
            // Iniciamos un nuevo recorrido
            _recorrido = new Recorrido(_inicio);
            _polyoptions = null;
            _boton.setText("Stop");

            // Wake lock para mantener la aplicación corriendo cuando se apague la pantalla
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            _wakelock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Mi Caminata");
            _wakelock.acquire();

            // Inicia el timer
            _startTime = System.currentTimeMillis();
            _timerHandler.removeCallbacks(_timerRunnable);
            _timerHandler.postDelayed(_timerRunnable, _tickTime);

            // Información inicial
            toast("Recorrido iniciado!");
            texto("A caminar!");
            status(String.format("Actualizacion: %d seg", _tickTime / 1000));
        }
        else
        {
            // Información final
            toast("Recorrido finalizado!");
            guardarRecorrido();

            // Detiene el timer y libera el wake lock
            _recorrido = null;
            _boton.setText("Start!");
            _timerHandler.removeCallbacks(_timerRunnable);
            _wakelock.release();
        }
    }

    // Hora de inicio y parámetros del timer
    private long _startTime = 0;
    private long _tickTime = 20000;
    private long _tryTime = 5000;

    // Ruta
    private PolylineOptions _polyoptions;
    private Polyline _polyline;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler _timerHandler = new Handler();
    Runnable _timerRunnable = new Runnable()
    {
        private int _posiciones = 0;
        private int _totales = 0;
        private int _ejecuciones = 0;

        @Override
        public void run()
        {
            _ejecuciones += 1;

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

        // Intenta obtener la ubicación actual, y llama de nuevo al timer si no puede
        private void intentarUbicacion()
        {
            if (ubicarInicio() == false)
                _timerHandler.postDelayed(_timerRunnable, _tryTime);
            else
                 _timerHandler.removeCallbacks(_timerRunnable);
        }

        // Actualiza el recorrido sobre el mapa
        private void actualizarRecorrido()
        {
            Location location = _locator.get();
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
                _posiciones += 1;
            }

            _totales += 1;
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

            texto(String.format("%02d:%02d - %.2f km", hours, minutes, _recorrido.distancia()));
            status(String.format("%.2f km/h - Locs: %d/%d - %d ticks, %d tims", velocidad, _posiciones, _totales, _recorrido.getTicks().size(), _ejecuciones));
        }
    };

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
        _timerHandler.removeCallbacks(_timerRunnable);
        this.finish();
    }
}