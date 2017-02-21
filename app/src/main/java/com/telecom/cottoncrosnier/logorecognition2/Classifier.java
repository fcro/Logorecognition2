package com.telecom.cottoncrosnier.logorecognition2;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.telecom.cottoncrosnier.logorecognition2.Activity.MainActivity;

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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthieu on 19/01/17.
 */

public class Classifier extends IntentService{


    private final static String TAG = Classifier.class.getSimpleName();
    private BOWImgDescriptorExtractor mBowide;
    private SIFT mDetector;

    private List<BrandMap> mBrandMapList;

    public static final String BROADCAST_ACTION_ANALYZE = "BROADCAST_ACTION_ANALYZE";

    public Classifier() { // TODO rajouter throw filenullexeption
        super("Classifier");
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

    private Brand bestMatch(Mat response_hist) {
        Log.d(TAG, "bestMatch() called with: response_hist = [" + response_hist + "], classifiers = []");
        float minf = Float.MAX_VALUE;
        Brand bestMatch = null;

        for(BrandMap brandMap : mBrandMapList){
            float res = brandMap.classifier.predict(response_hist, true);
            if(res < minf){
                minf = res;
                bestMatch = brandMap.brand;
            }
        }

        return bestMatch;
    }

    public void loadClassifier(List<Brand> brandList){
        Log.d(TAG, "loadClassifier() called");

        for (Brand brand: brandList) {
            File classifierFile = brand.getClassifier();
            CvSVM temp = new CvSVM();
            temp.load(classifierFile.getAbsolutePath());
            mBrandMapList.add(new BrandMap(temp, brand));
        }
    }


    public Brand computeImageHist(Uri path, File vocabularyFile) {
        Log.d(TAG, "computeImageHist() called with: path = [" + path + "]");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = Utils.scaleBitmapDown(BitmapFactory.decodeFile(path.getPath(), options), 500);

        Log.d(TAG, "computeImageHist: after scale down");
        File bitmapFile = Utils.bitmapToCache(this, bitmap, path.getLastPathSegment());
        Log.d(TAG, "computeImageHist: after bitmaptocache");
        setVoca(vocabularyFile);
        Mat response_hist = computeHist(bitmapFile.getAbsolutePath(), mDetector);
        Brand  bestBrand = bestMatch(response_hist);
        Log.d(TAG, "computeImageHist: bestMatch = "+bestBrand.getBrandName());

        return bestBrand;
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "onHandleIntent() called with: intent = [" + intent + "]");
        if(intent != null){

            Bundle b = intent.getExtras();

            List<Brand> brandlist = (List<Brand>) b.get(MainActivity.KEY_BRANDLIST);
            File vocaFile = (File) b.getSerializable(MainActivity.KEY_VOCA_FILE);
            Uri imagePath = b.getParcelable(MainActivity.KEY_URI);

            mDetector = new SIFT(0, 3, 0.04, 10, 1.6);
            mBrandMapList = new ArrayList<>();

            loadClassifier(brandlist);
            Brand bestBrand = computeImageHist(imagePath,vocaFile);

            onResult(bestBrand);

        }
    }

    private void onResult(Brand bestBrand){

        Intent localIntent = new Intent(BROADCAST_ACTION_ANALYZE).putExtra(MainActivity.KEY_RESPONSE_BRAND, bestBrand);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }


    private class BrandMap{

        private CvSVM classifier;
        private Brand brand;

        private BrandMap(CvSVM classifier, Brand brand){
            this.classifier = classifier;
            this.brand = brand;
        }
    }
}
