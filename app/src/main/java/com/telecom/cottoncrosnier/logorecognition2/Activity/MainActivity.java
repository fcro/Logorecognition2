package com.telecom.cottoncrosnier.logorecognition2.Activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.telecom.cottoncrosnier.logorecognition2.Classifier;
import com.telecom.cottoncrosnier.logorecognition2.FileManager;
import com.telecom.cottoncrosnier.logorecognition2.JsonParser;
import com.telecom.cottoncrosnier.logorecognition2.Utils;
import com.telecom.cottoncrosnier.logorecognition2.http.HttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.ImageHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.JsonHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.StringHttpRequest;
import com.telecomlille.cottoncrosnier.logorecognition2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String KEY_PHOTO_PATH = "key_path";
    public static final String KEY_URL = "key_url";

    private static final int TAKE_PHOTO_REQUEST = 1;
    private static final int GALLERY_IMAGE_REQUEST = 2;

    private static final String BASE_URL = "http://www-rech.telecom-lille.fr/nonfreesift/";

    private static final String JSON_REQUEST = "index.json";
    private static final String YML_REQUEST = "vocabulary.yml";
    private static final String CLASSIFIER_COCA_REQUEST = "classifiers/Coca.xml";
    private static final String CLASSIFIER_SPRITE_REQUEST = "classifiers/Sprite.xml";
    private static final String CLASSIFIER_PESPSI_REQUEST = "classifiers/Pepsi.xml";

    private Classifier classifier;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            Bundle data = msg.getData();
            if(data != null) {
                switch (msg.arg1) {
                    case ImageHttpRequest.IMAGE_REQUEST:
                        onImageRequestResult(msg.getData());
                        break;

                    case JsonHttpRequest.JSON_REQUEST:
                        onJSONRequestResult(msg.getData());
                        break;

                    case StringHttpRequest.STRING_REQUEST:
                        onStringRequestResult(msg.getData());
                        break;
                }
            }
            return true;
            }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder
                        .setMessage(R.string.dialog_select_prompt)
                        .setPositiveButton(R.string.dialog_select_gallery, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startGalleryChooser();
                            }
                        })
                        .setNegativeButton(R.string.dialog_select_camera, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startCamera();
                            }
                        });
                builder.create().show();
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });
        fab.setVisibility(View.VISIBLE);


        classifier = new Classifier(this.getApplicationContext());

        sendGetClassifiersRequest();

        JsonHttpRequest jsonHttpRequest = new JsonHttpRequest(this, handler, BASE_URL);
        jsonHttpRequest.sendRequest(JSON_REQUEST);

        StringHttpRequest ymlRequest = new StringHttpRequest(this, handler, BASE_URL);
        ymlRequest.sendRequest(YML_REQUEST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        Log.d(TAG, "onOptionsItemSelected: id = " + id);
        if (id == R.id.view_website) {
            startBrowser();
            return true;
        } else if (id == R.id.delete_photo) {
            deletePhoto();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult() called with: requestCode = [" + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Uri imgPath = data.getData();
            Log.d(TAG, "onActivityResult:: imgPath = " + imgPath.toString());

            File imgFile = Utils.galleryToCache(this, imgPath, imgPath.getFragment());
            Uri imgUri = Uri.fromFile(imgFile);
            startAnalyze(imgUri);

        } else if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "onActivityResult:: gallery & canceled");
//            Utils.toast(this,getString(R.string.toast_gallery_canceled));

        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bundle b = data.getExtras();
            Uri imgPath = b.getParcelable(KEY_PHOTO_PATH);

            if(imgPath != null){
                Log.d(TAG, "onActivityResult:: imgPath = " + imgPath.toString());
                startAnalyze(imgPath);
            }

        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "onActivityResult: take photo & canceled");
//            Utils.toast(this,getString(R.string.toast_photo_canceled));

        }


    }
    private void onStringRequestResult(Bundle data){

        switch (data.getString(HttpRequest.KEY_REQUEST)) {
            case YML_REQUEST:
                Log.d(TAG, "onStringRequestResult: received vocabulary");
                try {
                    File vocabularyFile = FileManager.createVocabularyFile(getCacheDir(),
                            (String) data.get(StringHttpRequest.KEY_STRING));

                        classifier.setVocabulary(vocabularyFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    Utils.toast(this, getString(R.string.error_vocabulary));
                }
                break;

            case CLASSIFIER_COCA_REQUEST:
                //XML coca
                Log.d(TAG, "onStringRequestResult: received coca");
                break;
            case CLASSIFIER_PESPSI_REQUEST:
                //XML pepsi
                Log.d(TAG, "onStringRequestResult: received pepsi");
                break;

            case CLASSIFIER_SPRITE_REQUEST:
                //XML sprite
                Log.d(TAG, "onStringRequestResult: received sprite");
                break;
            
        }
    }

    private void onJSONRequestResult(Bundle data){
        try {
            JSONObject jsonData = new JSONObject(data.getString(JsonHttpRequest.KEY_JSON));
            JsonParser jsonParser = new JsonParser(jsonData);
            Log.d(TAG, "handleMessage: brands = " + jsonParser.readBrandArray());
            Log.d(TAG, "onJSONRequestResult: vocabulary = " + jsonParser.readVocabulary());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onImageRequestResult(Bundle data){
        Log.d(TAG, "handleMessage: request = " + data.getString(HttpRequest.KEY_REQUEST));
        Bitmap img = data.getParcelable(ImageHttpRequest.KEY_IMAGE);
        Log.d(TAG, "handleMessage: img " + img.toString());
    }


    private void sendGetClassifiersRequest( ) {
        StringHttpRequest stringHttpRequest = new StringHttpRequest(this, handler, BASE_URL);
        stringHttpRequest.sendRequest(CLASSIFIER_COCA_REQUEST);
        stringHttpRequest.sendRequest(CLASSIFIER_PESPSI_REQUEST);
        stringHttpRequest.sendRequest(CLASSIFIER_SPRITE_REQUEST);
    }

    public void startCamera() {
        Intent startTakePhoto = new Intent(MainActivity.this, TakePhotoActivity.class);
        startActivityForResult(startTakePhoto, TAKE_PHOTO_REQUEST);
    }

    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    private void startBrowser() {
//        Log.d(TAG, "onOptionsItemSelected: view website");
//        if (mId != INVALID_POSITION) {
//            Photo photo = mPhotoAdapter.getItem(mId);
//            if(photo != null){
//                Intent startWebBrowser = new Intent(MainActivity.this, LaunchBrowserActivity.class);
//                startWebBrowser.putExtra(KEY_URL, photo.getBrand().getUrl().toString());
//                startActivityForResult(startWebBrowser, VIEW_BROWSER_REQUEST);
//            }
//
//        } else {
//            Utils.toast(this,"please select an item");
//        }
    }

    private void deletePhoto() {
//        if (mId == INVALID_POSITION) {
//            Utils.toast(this,"please select an item");
//            return;
//        }
//
//        mPhotoAdapter.remove(mArrayPhoto.get(mId));
//        mId = INVALID_POSITION;
    }

    private void startAnalyze(Uri uri) {
//        Intent startAnalyze = new Intent(MainActivity.this, AnalizePhotoActivity.class);
//        startAnalyze.putExtra(KEY_PHOTO_PATH, uri);
//        startActivityForResult(startAnalyze, ANALYZE_PHOTO_REQUEST);
        classifier.computeImageHist(uri);
    }
}
