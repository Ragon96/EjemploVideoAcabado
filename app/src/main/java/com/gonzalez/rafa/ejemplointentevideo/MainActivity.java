package com.gonzalez.rafa.ejemplointentevideo;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

    static final int VENGO_DE_LA_CAMARA = 1;
    static final int VENGO_DE_LA_CAMARA_CON_FICHERO = 2;
    static final int PEDI_PERMISOS_DE_ESCRITURA = 3;
    Button captura, captura2;
    VideoView videoVisor;
    String rutaVideoActual;
    Uri videoUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        captura = findViewById(R.id.buttonCaptura);
        captura2 = findViewById(R.id.buttonCaptura2);
        videoVisor = findViewById(R.id.videoV);

        captura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent haceFoto = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
                if (haceFoto.resolveActivity(getPackageManager())!=null){
                    startActivityForResult(haceFoto,VENGO_DE_LA_CAMARA);
                }else{
                    Toast.makeText(MainActivity.this, "Necesito un programa de Grabar.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        captura2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pedirPermisoParaEscribirYHacerVideo();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if ((requestCode == VENGO_DE_LA_CAMARA) && (resultCode == RESULT_OK)){
            Bundle extras = data.getExtras();
        }

    }
    /*public void capturarVideo( ) {
        Intent intent = new Intent(MediaStore.INTENT_ACTION_VIDEO_CAMERA);
        File ficheroVideo = null;
        try {
            ficheroVideo = crearFicheroVideo();
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
        String nombreFichero = "VideoEjemplo_"+fechaYHora;
        File carpetaParaVideos = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        File video = File.createTempFile(nombreFichero, ".mp4", carpetaParaVideos);
        rutaVideoActual = video.getAbsolutePath();
        return video;
    }*/
    public void capturarVideo() {
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File carpetaParaVideos = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DCIM), "Camera");
            File videoFile = new File(carpetaParaVideos, "Commons_" + timeStamp + ".mp4");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                 videoUri = FileProvider.getUriForFile(MainActivity.this,
                        BuildConfig.APPLICATION_ID + ".provider",videoFile);
            } else {
                videoUri = Uri.fromFile(videoFile);
            }

            intent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, VENGO_DE_LA_CAMARA_CON_FICHERO);
        } else {
            Toast.makeText(this, "No tengo programa o cámara", Toast.LENGTH_SHORT).show();
        }
    }

    void pedirPermisoParaEscribirYHacerVideo(){
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // Aquí puedo explicar para qué quiero el permiso
            } else {
                // No explicamos nada y pedimos el permiso
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},PEDI_PERMISOS_DE_ESCRITURA);

                // El resultado de la petición se recupera en onRequestPermissionsResult
            }
        }else{//Tengo los permisos
            capturarVideo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PEDI_PERMISOS_DE_ESCRITURA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
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
