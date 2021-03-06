package com.mmym.micaminata;

import android.content.Context;
import android.widget.TextView;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.util.Date;

public class EscritorRecorrido
{
    private Recorrido _recorrido;
    private MapsActivity _parent;

    public EscritorRecorrido(Recorrido recorrido, MapsActivity parent)
    {
        _recorrido = recorrido;
        _parent = parent;
    }

    public void escribir()
    {
        if (_recorrido == null)
        {
            _parent.status("No hay recorrido para guardar!");
            return;
        }

        if (isExternalStorageWritable() == false)
        {
            _parent.status("No se pudo guardar la ruta");
            _parent.toast("Sin permisos de escritura!");
            return;
        }

        try
        {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/Rutas");

            if (myDir.exists() == false)
                myDir.mkdirs();

            String archivo = DateFormat.getDateTimeInstance().format(new Date()) + ".dat";
            File file = new File (myDir, archivo);

            if (file.exists() == true)
                file.delete();

            FileOutputStream outputStream = new FileOutputStream(file);
            for(Tick tick: _recorrido.getTicks())
            {
                String linea = DateFormat.getDateTimeInstance().format(tick.getTimestamp()) + " | " + tick.getPosicion().latitude + " | " + tick.getPosicion().longitude + "\r\n";
                outputStream.write(linea.getBytes());
            }

            outputStream.flush();
            outputStream.close();

            _parent.status("Guardado: " + archivo);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            _parent.status("Problemas! " + e.getMessage());
        }
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;

        return false;
    }
}
