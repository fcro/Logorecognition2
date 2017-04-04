package com.telecom.cottoncrosnier.logorecognition2.utils;

import android.util.Log;

import com.telecom.cottoncrosnier.logorecognition2.activity.MainActivity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Classe fournissant des services de création et vérification de fichiers.
 */
public class FileManager {

    private static final String TAG = FileManager.class.getSimpleName();

    private static final int CREATE_CLASSIFIER = 0;
    private static final int CREATE_VOCABULARY = 1;

    private int[] mFileCount;
    private int mBrandsNumber;

    private MainActivity mMainActivity;


    public FileManager(MainActivity mainActivity) {
        this.mMainActivity = mainActivity;
        mFileCount = new int[2];
    }


    public void setBrandsNumber(int brandsNumber) {
        mBrandsNumber = brandsNumber;
    }
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
    public File createVocabularyFile(File cacheDir, String content) throws IOException {
        Log.d(TAG, "createVocabularyFile() called with: cacheDir = [" + cacheDir + "], content = [" + "..." + "]");
        File vocabularyFile = new File(cacheDir.getPath() + "/vocabulary.yml");
        FileWriter fw = new FileWriter(vocabularyFile, false);

        try {
            fw.write(content);
            mFileCount[CREATE_VOCABULARY]++;
            checkIsComplete();

            return vocabularyFile;
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                Log.e(TAG, "createVocabularyFile: failed flushing/closing filewriter");
            }
        }
    }

    /**
     * Crée un fichier de classifier avec comme contenu {@code content}. Retourne
     * toujours le fichier créé.
     *
     * @param cacheDir répertoire cache de l'application pour stocker le nouveau fichier.
     * @param content contenu du fichier reçu.
     * @param name nom du fichier à créer.
     * @return l'objet {@link File} créé dans le cache.
     * @throws IOException si une erreur se produit pendant la lecture ou l'écriture du fichier.
     */
    public File createClassifierFile(File cacheDir, String content, String name) throws IOException{
        Log.d(TAG, "createClassifierFile() called with: cacheDir = [" + cacheDir + "], content = [" + "..." + "], name = [" +name +"]");
        File classifierFile = new File(cacheDir.getPath() + "/" + name);
        FileWriter fw = new FileWriter(classifierFile, false);

        try {
            fw.write(content);
            mFileCount[CREATE_CLASSIFIER]++;
            checkIsComplete();

            return classifierFile;
        } finally {
            try {
                fw.close();
            } catch (IOException e) {
                Log.e(TAG, "createClassifierFile: failed flushing/closing filewriter");
            }
        }
    }

    /**
     * Renvoie {@code true} et affiche le bouton d'ajout d'images dans {@link MainActivity} si tous
     * les fichiers de vocabulaire et clasifier ont été créés.
     */
    private void checkIsComplete() {
        if (mFileCount[0] ==  mBrandsNumber && mFileCount[1] == 1) {
            Log.d(TAG, "checkIsComplete: isComplete");
            mMainActivity.onDatabaseInitFinished();
        }
    }
}
