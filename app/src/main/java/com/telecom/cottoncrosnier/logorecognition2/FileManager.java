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
     * Compare le fichier de vocabulaire existant avec {@code content}. Si ils ont un poids
     * différent, remplace le fichier de vocabulaire par {@code content}. Retourne toujours
     * le fichier de vocabulaire.
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
        long currentFileSize = vocabularyFile.length();
        FileWriter fw = new FileWriter(vocabularyFile, false);

        try {
            if (vocabularyFile.createNewFile()) {
                Log.d(TAG, "createVocabularyFile: vocabulary file does not exist");
                fw.write(content);
            } else {
                Log.d(TAG, "createVocabularyFile: filesize = " + currentFileSize);
                long newFileSize = content.length();
                Log.d(TAG, "createVocabularyFile: newfilesize = " + newFileSize);
                if (newFileSize != currentFileSize) {
                    fw.write(content);
                }
            }

            return vocabularyFile;
        } finally {
            try {
                fw.flush();
                Log.d(TAG, "createVocabularyFile: flushed");
                fw.close();
                Log.d(TAG, "createVocabularyFile: closed");
            } catch (IOException e) {
                Log.e(TAG, "createVocabularyFile: failed flushing/closing filewriter");
            }
        }
    }

    public static File createClassifierFile(File cacheDir, String content, String name) throws IOException{
        Log.d(TAG, "createClassifierFile() called with: cacheDir = [" + cacheDir + "], content = [" + "..." + "], name = [" +name +"]");
        File classifierFile = new File(cacheDir.getPath() + "/" + name);
        long currentFileSize = classifierFile.length();
        FileWriter fw = new FileWriter(classifierFile, false);

        try {
            if (classifierFile.createNewFile()) {
                Log.d(TAG, "createClassifierFile: vocabulary file does not exist");
                fw.write(content);
            } else {
                Log.d(TAG, "createClassifierFile: filesize = " + currentFileSize);
                long newFileSize = content.length();
                Log.d(TAG, "createClassifierFile: newfilesize = " + newFileSize);
                if (newFileSize != currentFileSize) {
                    fw.write(content);
                }
            }

            return classifierFile;
        } finally {
            try {
                fw.flush();
                Log.d(TAG, "createClassifierFile: flushed");
                fw.close();
                Log.d(TAG, "createClassifierFile: closed");
            } catch (IOException e) {
                Log.e(TAG, "createClassifierFile: failed flushing/closing filewriter");
            }
        }
    }
}
