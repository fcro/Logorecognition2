package com.telecom.cottoncrosnier.logorecognition2;


import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import static org.bytedeco.javacpp.opencv_highgui.imread;

import org.bytedeco.javacpp.Loader;
import org.bytedeco.javacpp.Pointer;
import org.bytedeco.javacpp.opencv_core;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_features2d.BOWImgDescriptorExtractor;
import org.bytedeco.javacpp.opencv_features2d.FlannBasedMatcher;
import org.bytedeco.javacpp.opencv_features2d.KeyPoint;
import org.bytedeco.javacpp.opencv_ml.CvSVM;
import org.bytedeco.javacpp.opencv_nonfree.SIFT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by matthieu on 19/01/17.
 */

public class Classifier {


    private final static String TAG = Classifier.class.getSimpleName();
    private Context mMainContext;
    private final int mClassNomber = 3;
    private String[] mClassName = new String[mClassNomber];
    private BOWImgDescriptorExtractor mBowide;

    public Classifier(Context contextMain) { // TODO rajouter throw filenullexeption

        mMainContext = contextMain;
        mClassName[0] = "Coca";
        mClassName[1] = "Pepsi";
        mClassName[2] = "Sprite";

        String ref = "images/";
        String imageName = "Coca_15";

        final SIFT detector = new SIFT(0, 3, 0.04, 10, 1.6);
        final FlannBasedMatcher matcher = new FlannBasedMatcher();

        mBowide = new BOWImgDescriptorExtractor(detector.asDescriptorExtractor(), matcher);
        createVocabulary();

        File image = assetToCache(mMainContext, ref+imageName+".jpg", imageName+".jpg");
        Mat response_hist = computeHist(image.getPath(), detector, mBowide);

        final CvSVM[] classifiers = loadClassifier();

        long timePrediction = System.currentTimeMillis();
        String bestMatch = bestMatch(response_hist, classifiers);
        timePrediction = System.currentTimeMillis() - timePrediction;

        Log.d(TAG, image.getName() + "  predicted as " + bestMatch + " in " + timePrediction + " ms");

    }

    private void createVocabulary() {
        Log.d(TAG, "createVocabulary() called");
        final Mat vocabulary;
        Loader.load(opencv_core.class);

        File fileVoca = assetToCache(mMainContext, "vocabulary.yml", "vocabulary.yml");

        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(fileVoca.getAbsolutePath(), null, opencv_core.CV_STORAGE_READ);
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new Mat(cvMat);
        opencv_core.cvReleaseFileStorage(storage);

        mBowide.setVocabulary(vocabulary);
    }

    private Mat computeHist(String imgPath, SIFT detector, BOWImgDescriptorExtractor bowide) {

        Log.d(TAG, "computeHist() called with: imgPath = [" + imgPath + "], detector = [" + detector + "], bowide = [" + bowide + "]");
        Mat response_hist = new Mat();
        KeyPoint keypoints = new KeyPoint();
        Mat inputDescriptors = new Mat();

        Mat matImage = imread(imgPath, 1);
        detector.detectAndCompute(matImage, Mat.EMPTY, keypoints, inputDescriptors);
        bowide.compute(matImage, keypoints, response_hist);

        return response_hist;
    }

    private String bestMatch(Mat response_hist, CvSVM[] classifiers) {
        Log.d(TAG, "bestMatch() called with: response_hist = [" + response_hist + "], classifiers = [" + classifiers + "]");
        float minf = Float.MAX_VALUE;
        String bestMatch = null;
        for (int i = 0; i < mClassNomber; i++) {
            float res = classifiers[i].predict(response_hist, true);
            if (res < minf) {
                minf = res;
                bestMatch = mClassName[i];
            }
        }
        return bestMatch;
    }

    private CvSVM[] loadClassifier() {

        Log.d(TAG, "loadClassifier() called");
        final CvSVM[] classifiers;
        classifiers = new CvSVM[mClassNomber];
        for (int i = 0; i < mClassNomber; i++) {
            File classifierFile = assetToCache(mMainContext, "classifier/" + mClassName[i] + ".xml", mClassName[i] + ".xml");
            classifiers[i] = new CvSVM();

            classifiers[i].load(classifierFile.getAbsolutePath());
        }
        return classifiers;
    }

    /**
     * Copie un fichier des assets vers le cache de l'application.
     *
     * @param context   contexte de l'application pour récupérer le dossier de cache.
     * @param assetPath chemin vers le fichier dans les assets à copier.
     * @param fileName  nom du fichier de destination.
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


}
