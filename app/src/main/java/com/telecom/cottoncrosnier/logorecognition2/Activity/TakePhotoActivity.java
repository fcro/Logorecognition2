package com.telecom.cottoncrosnier.logorecognition2.Activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.telecom.cottoncrosnier.logorecognition2.Utils;

import java.io.File;

/**
 * Activité permettant le démarrage de l'appareil photo.
 */
public class TakePhotoActivity extends Activity {

    private static final String TAG = TakePhotoActivity.class.getSimpleName();

    public static final int CAMERA_PERMISSIONS_REQUEST = 2;
    public static final int CAMERA_IMAGE_REQUEST = 3;

    public static int mPhotoNumber = 0;

    public static String FILE_NAME = "temp.jpg";


    /**
     * Appelée au demarrage de l'activité, appelle le démarrage de l'appareil photo.
     *
     * @param savedInstanceState Éléments sauvegardés lors du dernier arrêt de l'activité (non utilisé).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCamera();
    }

    /**
     * Appelée quand l'activité reprend.
     *
     * @param requestCode code de l'activité qui a été appelée.
     * @param resultCode code de retour de l'activité appelée (ok / nok).
     * @param data valeur de retour de l'activité appelée.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult:: result actvity");

            Intent resultIntent = new Intent();
            resultIntent.putExtra(MainActivity.KEY_PHOTO_PATH, Uri.fromFile(getCameraFile()));
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    /**
     * Appelée si les permissions sont demandées à l'utilisateur (première utilisation).
     *
     * @param requestCode code de la requête.
     * @param permissions permissions demandées.
     * @param grantResults code de retour (ok / nok).
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (Utils.permissionGranted(
                requestCode,
                CAMERA_PERMISSIONS_REQUEST,
                grantResults)) {
            startCamera();
        }
    }

    /**
     * Démarre l'activité pour prendre une photo.
     */
    public void startCamera() {
        if (Utils.requestPermission(
                this,
                CAMERA_PERMISSIONS_REQUEST,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            mPhotoNumber++;
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getCameraFile()));
            startActivityForResult(intent, CAMERA_IMAGE_REQUEST);
        }
    }

    /**
     * Récupère la photo prise.
     *
     * @return fichier contenant la photo.
     */
    public File getCameraFile() {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String fileName = mPhotoNumber + FILE_NAME;
        return new File(dir, fileName);
    }

}
