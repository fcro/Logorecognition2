package com.telecom.cottoncrosnier.logorecognition2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
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

/**
 * Created by matthieu on 19/01/17.
 */

public class Classifier {


    private final static String TAG = Classifier.class.getSimpleName();
    private Context mMainContext;
    private final int mClassNomber = 3;
    private String[] mClassName = new String[mClassNomber];
    private BOWImgDescriptorExtractor mBowide;
    private SIFT mDetector;

    private CvSVM[] mClassifiers;

    public Classifier(Context mainContext) { // TODO rajouter throw filenullexeption

        mDetector = new SIFT(0, 3, 0.04, 10, 1.6);
        mMainContext = mainContext;
        mClassName[0] = "Coca";
        mClassName[1] = "Pepsi";
        mClassName[2] = "Sprite";

//        String ref = "images/";
//        String imageName = "Coca_12";


//        File image = Utils.assetToCache(mMainContext, ref+imageName+".jpg", imageName+".jpg");
//        Mat response_hist = computeHist(image.getPath(), mDetector, mBowide);
//

//
//        long timePrediction = System.currentTimeMillis();
//        String bestMatch = bestMatch(response_hist, classifiers);
//        timePrediction = System.currentTimeMillis() - timePrediction;

//        Log.d(TAG, image.getName() + "  predicted as " + bestMatch + " in " + timePrediction + " ms");
//        mClassifiers = loadClassifier();
    }


    public void setVoca(File vocabularyFile) {

        Log.d(TAG, "setVoca() called with: vocabularyFile = [" + vocabularyFile.getAbsolutePath() + "]");

        // instancier variable juste avant de les utiliser, pourquoi ? no sé
        final FlannBasedMatcher matcher = new FlannBasedMatcher();
        mBowide = new BOWImgDescriptorExtractor(mDetector.asDescriptorExtractor(), matcher);
        //

        final Mat vocabulary;
        Loader.load(opencv_core.class);

        opencv_core.CvFileStorage storage = opencv_core.cvOpenFileStorage(
//                Utils.assetToCache(mMainContext, "vocabulary.yml", "vocabulary.yml").getAbsolutePath(),
                vocabularyFile.getAbsolutePath(),
                null, opencv_core.CV_STORAGE_READ);
        Pointer p = opencv_core.cvReadByName(storage, null, "vocabulary", opencv_core.cvAttrList());
        opencv_core.CvMat cvMat = new opencv_core.CvMat(p);
        vocabulary = new Mat(cvMat);
        opencv_core.cvReleaseFileStorage(storage);

        mBowide.setVocabulary(vocabulary);
        Log.d(TAG, "setVoca() returned: ");
    }

    private Mat computeHist(String imgPath, SIFT detector) {
        Log.d(TAG, "computeHist() called with: imgPath = [" + imgPath + "], mDetector = [" + detector + "]");
        Mat response_hist = new Mat();
        KeyPoint keypoints = new KeyPoint();
        Mat inputDescriptors = new Mat();

        Mat matImage = imread(imgPath, 1);
        Log.d(TAG, "computeHist: after imread");
        detector.detectAndCompute(matImage, Mat.EMPTY, keypoints, inputDescriptors);
        Log.d(TAG, "computeHist: after detectandcompute");
        mBowide.compute(matImage, keypoints, response_hist);
        Log.d(TAG, "computeHist: afet compute");

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
            File classifierFile = Utils.assetToCache(mMainContext, "classifier/" + mClassName[i] + ".xml", mClassName[i] + ".xml");
            classifiers[i] = new CvSVM();

            classifiers[i].load(classifierFile.getAbsolutePath());
        }
        return classifiers;
    }

    public void loadClassifier(File[] classifierFiles) {
        Log.d(TAG, "loadClassifier() called");
        final CvSVM[] classifiers;
        classifiers = new CvSVM[mClassNomber];
        for (int i = 0; i < mClassNomber; i++) {
            File classifierFile = classifierFiles[i];
            classifiers[i] = new CvSVM();

            classifiers[i].load(classifierFile.getAbsolutePath());
        }
        mClassifiers =  classifiers;
    }


    public void computeImageHist(Uri path, File vocabularyFile) {
        Log.d(TAG, "computeImageHist() called with: path = [" + path + "]");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Utils.scaleBitmapDown(BitmapFactory.decodeFile(path.getPath(), options), 500);

        Log.d(TAG, "computeImageHist: after scale down");
        File bitmapFile = Utils.bitmapToCache(mMainContext, bitmap, path.getLastPathSegment());
        Log.d(TAG, "computeImageHist: after bitmaptocache");
        setVoca(vocabularyFile);
        Mat response_hist = computeHist(bitmapFile.getAbsolutePath(), mDetector);
        String bestMatch = bestMatch(response_hist, mClassifiers);
        Log.d(TAG, "computeImageHist: bestMatch = "+bestMatch);
    }
}
