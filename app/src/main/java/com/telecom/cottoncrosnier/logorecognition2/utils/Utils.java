package com.telecom.cottoncrosnier.logorecognition2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.telecom.cottoncrosnier.logorecognition2.reference.Brand;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Classe utilitaire qui met à dispositions des méthodes pour des besoins globaux ou génériques.
 */
public class Utils {

    private final static String TAG = Utils.class.getSimpleName();


    /**
     * <p>Vérifie que l'application possède une ou des permissions. Demande l'accès à l'utilisateur
     * aux permissions que l'application n'a pas déjà.</p>
     * <p>Renvoie true si les permission sont accordées, false sinon.</p>
     *
     * @param activity Activity qui appelle la méthode.
     * @param requestCode code interne pour récupérer le résultat de ActivityCompat.requestPermissions.
     * @param permissions liste des permissions à vérifier.
     * @return true si les permissions sont accordées, false sinon.
     */
    public static boolean requestPermission(
            Activity activity, int requestCode, String... permissions) {
        boolean granted = true;
        ArrayList<String> permissionsNeeded = new ArrayList<>();

        for (String s : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(activity, s);
            boolean hasPermission = (permissionCheck == PackageManager.PERMISSION_GRANTED);
            granted &= hasPermission;
            if (!hasPermission) {
                permissionsNeeded.add(s);
            }
        }

        if (granted) {
            return true;
        } else {
            ActivityCompat.requestPermissions(activity,
                    permissionsNeeded.toArray(new String[permissionsNeeded.size()]),
                    requestCode);
            return false;
        }
    }


    /**
     * <p>Vérifie que les permissions demandées par {@link ActivityCompat#requestPermissions} ont bien
     * été accordées à l'application.</p>
     * <p>Renvoie true si {@code requestCode} == {@code permissionCode} et {@code grantResults}
     * n'est pas vide et toutes les valeurs de {@code grantResults} sont
     * {@link PackageManager#PERMISSION_GRANTED}.</p>
     *
     * @param requestCode le code de retour récupéré.
     * @param permissionCode le code de retour attendu.
     * @param grantResults le status des permissions demandées.
     * @return true si {@code requestCode} == {@code permissionCode} et {@code grantResults}
     * n'est pas vide et toutes les valeurs de {@code grantResults} sont
     * {@link PackageManager#PERMISSION_GRANTED}.
     */
    public static boolean permissionGranted(
            int requestCode, int permissionCode, int[] grantResults) {
        if (grantResults.length < 1 || requestCode != permissionCode) {
            return false;
        }

        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }


    /**
     * Copie un fichier des assets vers le cache de l'application.
     *
     * @param context contexte de l'application pour récupérer le dossier de cache.
     * @param assetPath chemin vers le fichier dans les assets à copier.
     * @param fileName nom du fichier de destination.
     * @return fichier copié dans le cache de l'application.
     */
    public static File assetToCache(Context context, String assetPath, String fileName) {
        InputStream is;
        FileOutputStream fos;
        int size;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);
        AssetManager assetManager = context.getAssets();

        try {
            is = assetManager.open(assetPath);
            size = is.available();
            buffer = new byte[size];

            if (is.read(buffer) <= 0) {
                return null;
            }

            is.close();

            fos = new FileOutputStream(filePath);
            fos.write(buffer);
            fos.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Copie un fichier de la galerie vers le cache de l'application.
     *
     * @param context contexte de l'application pour récupérer le dossier de cache.
     * @param imgPath chemin sous forme d'Uri vers l'image dans la galerie.
     * @param fileName nom du fichier de destination.
     * @return fichier copié dans le cache de l'application.
     */
    public static File galleryToCache(Context context, Uri imgPath, String fileName) {
        InputStream is;
        FileOutputStream fos;
        int size;
        byte[] buffer;
        String filePath = context.getCacheDir() + "/" + fileName;
        File file = new File(filePath);

        try {
            is = context.getContentResolver().openInputStream(imgPath);
            if (is == null) {
                return null;
            }

            size = is.available();
            buffer = new byte[size];

            if (is.read(buffer) <= 0) {
                return null;
            }

            is.close();

            fos = new FileOutputStream(filePath);
            fos.write(buffer);
            fos.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Stocke un objet Bitmap dans le cache de l'application.
     *
     * @param context contexte de l'application pour récupérer le dossier de cache.
     * @param bitmap Bitmap à stocker dans le cache.
     * @param fileName nom du fichier de destination.
     * @return fichier copié dans le cache de l'application.
     */
    public static File bitmapToCache(Context context, Bitmap bitmap, String fileName) {
        File file = new File(context.getCacheDir(), fileName);

        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            byte[] bitmapData = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bitmapData);

            fos.flush();
            fos.close();

            return file;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * Renvoie la liste des noms des fichiers commençant par {@code prefix}. Cette méthode sert à
     * associer toutes les images d'une marque à un même objet {@link Brand}.
     *
     * @param files liste des fichiers parmi lesquels chercher.
     * @param prefix préfixe attendu dans la liste retournée.
     * @return liste des noms de fichiers commençant par {@code prefix}.
     */
    public static List<String> filesStartWith(String[] files, String prefix) {
        List<String> fileList = new ArrayList<String>();

        for (String file : files) {
            if (file.startsWith(prefix)) {
                fileList.add(file);
            }
        }

        return fileList;
    }


    /**
     * Affiche une notification Toast.
     *
     * @param context contexte appelant.
     * @param text texte à afficher.
     */
    public static void toast(Context context, String text) {
        Toast.makeText(context, text,
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Réduit la taille d'une image.
     *
     * @param bitmap image à réduire.
     * @param maxDimension dimension de l'image de sortie.
     * @return image réduite.
     */
    public static Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }


    public static Brand getBrandByName(List<Brand> brandList, String brandName) {
        Log.d(TAG, "getBrandByName() called with: brandList = [" + brandList + "], brandName = [" + brandName + "]");
        for (Brand brand : brandList) {
            if (brand.getBrandName().toLowerCase().equals(brandName.toLowerCase())) {
                return brand;
            }
        }

        return null;
    }

    public static Brand getBrandByClassifier(List<Brand> brandList, String brandName){
        Log.d(TAG, "getBrandByClassifier() called with: brandList = [" + brandList+ "], brandName = [" + brandName + "]");
        for (Brand brand : brandList) {
            if(brand.getClassifierFile().toLowerCase().equals(brandName.toLowerCase()))
                return brand;
        }

        return null;
    }
}