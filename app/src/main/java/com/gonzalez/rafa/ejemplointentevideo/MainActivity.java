package com.gonzalez.rafa.ejemplointentevideo;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    /*Mirar :
    -https://developer.android.com/guide/components/intents-common?hl=es-419
    -https://developer.android.com/training/permissions/requesting?hl=es-419
    */
    
    static final int VENGO_DE_LA_CAMARA_CON_FICHERO = 2;
    static final int PEDIR_PERMISOS_DE_ESCRITURA = 2;
    String rutaFichero;
    Button captura;
    VideoView videoVisor;
    String rutaVideoActual;
    MediaController control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        captura = findViewById(R.id.bCaptura);
        videoVisor = findViewById(R.id.videoView);
        //Control videoview
        control = new MediaController(this);
        //Ajustar al tamaño del videoView
        control.setAnchorView(videoVisor);

        captura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirPermisoParaEscribirYHacerVideo();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if ((requestCode == VENGO_DE_LA_CAMARA_CON_FICHERO) && (resultCode == RESULT_OK)){
            Uri uri =Uri.parse(rutaFichero);
            videoVisor.setMediaController(control);
            videoVisor.setVideoURI(uri);
            videoVisor.requestFocus();
            videoVisor.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    control.show();
                }
            });
        }
    }

    public void capturarVideo( ) {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        //Elegimos la duracion del video en segundos
        intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT ,5);
        //Al acabar el tiempo elegido paramos el video
        intent.putExtra(MediaStore.EXTRA_FINISH_ON_COMPLETION,false);

        File ficheroVideo = null;
        try {
            ficheroVideo = crearFicheroVideo();
            //nos dara la ruta absoluta del fichero
            rutaFichero=ficheroVideo.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ficheroVideo));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, VENGO_DE_LA_CAMARA_CON_FICHERO);
        }else{
            Toast.makeText(this, "No tengo programa o cámara", Toast.LENGTH_SHORT).show();
        }
    }

    File crearFicheroVideo() throws IOException {
        String fechaYHora = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nombreVideo = "VideoN_"+fechaYHora;
        File carpetaParaVideos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File imagen = File.createTempFile(nombreVideo, ".mp4", carpetaParaVideos);
        rutaVideoActual = imagen.getAbsolutePath();
        return imagen;
    }

    void pedirPermisoParaEscribirYHacerVideo(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Expliacion del uso de los permisos
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Aquí puedo explicar para qué quiero el permiso
            } else {
                // No explicamos nada y pedimos el permiso
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PEDIR_PERMISOS_DE_ESCRITURA);
                // El resultado de la petición se recupera en onRequestPermissionsResult
            }
        }else{//Tengo los permisos
            capturarVideo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PEDIR_PERMISOS_DE_ESCRITURA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Tengo los permisos: hago la foto:
                    this.capturarVideo();
                } else {
                    //No tengo permisos: Le digo que no se puede hacer nada
                    Toast.makeText(this, "Sin permisos de escritura no puedo guardar el video en alta resolución.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            //Pondría aquí más "case" si tuviera que pedir más permisos.
        }
    }
}