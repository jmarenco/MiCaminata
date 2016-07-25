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
    private TextView _status;

    public EscritorRecorrido(Recorrido recorrido, TextView status)
    {
        _recorrido = recorrido;
        _status = status;
    }

    public void escribir()
    {
        if (_recorrido == null)
            return;

        if (isExternalStorageWritable() == false)
            return;

        try
        {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/Rutas");
            myDir.mkdirs();

            String archivo = DateFormat.getDateTimeInstance().format(new Date()) + ".dat";
            File file = new File (myDir, archivo);

            if (file.exists())
                file.delete();

            FileOutputStream outputStream = new FileOutputStream(file);
            for(Tick tick: _recorrido.getTicks())
            {
                String linea = DateFormat.getDateTimeInstance().format(tick.getTimestamp()) + " | " + tick.getPosicion().latitude + " | " + tick.getPosicion().longitude;
                outputStream.write(linea.getBytes());
            }

            outputStream.flush();
            outputStream.close();

            _status.setText("Guardado: " + archivo);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            _status.setText("Problemas! " + e.getMessage());
        }
    }

    // Checks if external storage is available for read and write
    public boolean isExternalStorageWritable()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            return true;

        _status.setText("External storage not writable!");
        return false;
    }
}
