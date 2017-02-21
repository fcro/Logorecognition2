package com.telecom.cottoncrosnier.logorecognition2;

import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by User on 21/01/2017.
 */

public class FileManager {

    private static final String TAG = FileManager.class.getSimpleName();


    /**
     * Crée le fichier de vocabulaire vocabulary.yml avec comme contenu {@code content}. Retourne
     * toujours le fichier de vocabulaire.
     *
     * @param cacheDir répertoire cache de l'application pour stocker le fichier vocabulary.yml.
     * @param content contenu du vocabulary.yml reçu.
     * @return l'objet {@link File} de vocabulary.yml dans le cache.
     * @throws IOException si une erreur se produit pendant la lecture ou l'écriture du fichier
     * vocabulary.yml.
     */
    public static File createVocabularyFile(File cacheDir, String content) throws IOException {
        Log.d(TAG, "createVocabularyFile() called with: cacheDir = [" + cacheDir + "], content = [" + "..." + "]");
        File vocabularyFile = new File(cacheDir.getPath() + "/vocabulary.yml");
        FileWriter fw = new FileWriter(vocabularyFile, false);

        try {
            fw.write(content);

            return vocabularyFile;
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                Log.e(TAG, "createVocabularyFile: failed flushing/closing filewriter");
            }
        }
    }

    public static File createClassifierFile(File cacheDir, String content, String name) throws IOException{
        Log.d(TAG, "createClassifierFile() called with: cacheDir = [" + cacheDir + "], content = [" + "..." + "], name = [" +name +"]");
        File classifierFile = new File(cacheDir.getPath() + "/" + name);
        FileWriter fw = new FileWriter(classifierFile, false);

        try {
            fw.write(content);

            return classifierFile;
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                Log.e(TAG, "createClassifierFile: failed flushing/closing filewriter");
            }
        }
    }
}
