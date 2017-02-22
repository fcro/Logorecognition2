package com.telecom.cottoncrosnier.logorecognition2.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.soundcloud.android.crop.*;
import com.telecom.cottoncrosnier.logorecognition2.service.AnalyseService;
import com.telecom.cottoncrosnier.logorecognition2.reference.Brand;
import com.telecom.cottoncrosnier.logorecognition2.utils.FileManager;
import com.telecom.cottoncrosnier.logorecognition2.utils.JsonParser;
import com.telecom.cottoncrosnier.logorecognition2.reference.Photo;
import com.telecom.cottoncrosnier.logorecognition2.activity.adapter.PhotoArrayAdapter;
import com.telecom.cottoncrosnier.logorecognition2.utils.Utils;
import com.telecom.cottoncrosnier.logorecognition2.http.HttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.ImageHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.JsonHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.StringHttpRequest;
import com.telecomlille.cottoncrosnier.logorecognition2.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String KEY_PHOTO_PATH = "key_path";
    public static final String KEY_URL = "key_url";

    public static final String KEY_BRANDLIST = "key_brandlist";
    public static final String KEY_VOCA_FILE = "key_vocaFile";
    public static final String KEY_URI = "key_uri";
    public static final String KEY_RESPONSE_BRAND = "key_reponse_brand";

    private static final int TAKE_PHOTO_REQUEST = 1;
    private static final int GALLERY_IMAGE_REQUEST = 2;
    private static final int VIEW_BROWSER_REQUEST = 4;

    private static final String BASE_URL = "http://www-rech.telecom-lille.fr/nonfreesift/";

    private static final String JSON_REQUEST = "index.json";
    private static final String YML_REQUEST = "vocabulary.yml";

    private List<Brand> mBrands;
    private File vocabularyFile;

    private BroadcastReceiver mBroadcastReceiver;

    private ProgressDialog mProgressDialog;

    private ArrayList<Photo> mArrayPhoto;
    private ArrayAdapter<Photo> mPhotoAdapter;

    private static final int INVALID_POSITION = -1;
    private int mId;

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
                    case 0:
                        Brand brand = (Brand) msg.getData().getSerializable("brand");
                        Log.d(TAG, "handleMessage: "+brand.getBrandName());
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
            }
        });
        fab.setVisibility(View.VISIBLE);

        mArrayPhoto = new ArrayList<>();
        mPhotoAdapter = new PhotoArrayAdapter(
                this, R.layout.listview_row, mArrayPhoto);
        initListView();

        JsonHttpRequest jsonHttpRequest = new JsonHttpRequest(this, handler, BASE_URL);
        jsonHttpRequest.sendRequest(JSON_REQUEST);
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

            Crop.of(imgPath, Uri.fromFile(new File(getCacheDir() + "/crop"))).start(this);

        } else if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "onActivityResult:: gallery & canceled");

        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bundle b = data.getExtras();
            Uri imgPath = b.getParcelable(KEY_PHOTO_PATH);

            if (imgPath != null) {
                Crop.of(imgPath, Uri.fromFile(new File(getCacheDir() + "/crop"))).start(this);
                Log.d(TAG, "onActivityResult:: imgPath = " + imgPath.toString());
            }

        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "onActivityResult: take photo & canceled");

        } else if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: cropped");
            startAnalyze(Crop.getOutput(data));
        }
    }

    private void onStringRequestResult(Bundle data){
        Log.d(TAG, "onStringRequestResult() called ");
        if (data.getString(HttpRequest.KEY_REQUEST).equals(YML_REQUEST)) {
            Log.d(TAG, "onStringRequestResult: received vocabulary");
            try {
                vocabularyFile = FileManager.createVocabularyFile(getCacheDir(),
                        data.getString(StringHttpRequest.KEY_STRING));
                Log.d(TAG, "onStringRequestResult: createVocaFile");
//                classifier.setVoca(vocabularyFile);
                Utils.toast(this, getString(R.string.vocabulary_set));
            } catch (Exception e) {
                e.printStackTrace();
                Utils.toast(this, getString(R.string.error_vocabulary));
                StringHttpRequest ymlRequest = new StringHttpRequest(this, handler, BASE_URL);
                ymlRequest.sendRequest(YML_REQUEST);
            }
        } else {
            final String requestedBrand = data.getString(StringHttpRequest.KEY_REQUEST);
            final String classifierContent = data.getString(StringHttpRequest.KEY_STRING);
//            Log.d(TAG, "onStringRequestResult: brand = " + requestedBrand + " ; content = " + classifierContent);

            try {
                Utils.getBrandByClassifier(mBrands, requestedBrand).setLocalClassifier(FileManager.createClassifierFile(
                        getCacheDir(), classifierContent, requestedBrand));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void onJSONRequestResult(Bundle data){
        try {
            JSONObject jsonData = new JSONObject(data.getString(JsonHttpRequest.KEY_JSON));
            JsonParser jsonParser = new JsonParser(jsonData);
            mBrands = jsonParser.readBrandArray();
            Log.d(TAG, "onJSONRequestResult: brands = " + mBrands);
            Log.d(TAG, "onJSONRequestResult: vocabulary = " + jsonParser.readVocabulary());

            getClassifiers();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void onImageRequestResult(Bundle data){
        Log.d(TAG, "onImageRequestResult: request = " + data.getString(HttpRequest.KEY_REQUEST));
        Bitmap img = data.getParcelable(ImageHttpRequest.KEY_IMAGE);
        Log.d(TAG, "onImageRequestResult: img " + img.toString());
    }

    private void getClassifiers() {
        StringHttpRequest ymlRequest = new StringHttpRequest(this, handler, BASE_URL);
        ymlRequest.sendRequest(YML_REQUEST);

        for (Brand brand : mBrands) {
            new StringHttpRequest(this, handler, BASE_URL)
                    .sendRequest("classifiers/" + brand.getClassifierFile());
        }
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
        Log.d(TAG, "onOptionsItemSelected: view website");
        if (mId != INVALID_POSITION) {
            Photo photo = mPhotoAdapter.getItem(mId);
            if(photo != null){
                Intent startWebBrowser = new Intent(MainActivity.this, LaunchBrowserActivity.class);
                startWebBrowser.putExtra(KEY_URL, photo.getBrand().getUrl().toString());
                startActivityForResult(startWebBrowser, VIEW_BROWSER_REQUEST);
            }

        } else {
            Utils.toast(this,"please select an item");
        }
    }

    private void startAnalyze(final Uri uri) {

        mProgressDialog = ProgressDialog.show(
                this, getString(R.string.progress_analyzing), getString(R.string.progress_wait));

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Brand brand = (Brand) intent.getSerializableExtra(KEY_RESPONSE_BRAND);
                Log.d(TAG, "onReceive: "+intent.getSerializableExtra(KEY_RESPONSE_BRAND));

                try{
                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(
                            MediaStore.Images.Media.getBitmap(getContentResolver(), uri),
                            300,
                            300);
                    addImage(new Photo(bitmap, brand));
                }catch (IOException e){
                    e.printStackTrace();
                }
                mProgressDialog.dismiss();
                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(mBroadcastReceiver,
                new IntentFilter(AnalyseService.BROADCAST_ACTION_ANALYZE));
        Intent analyzeIntent = new Intent(this, AnalyseService.class);

        analyzeIntent.putExtra(KEY_BRANDLIST, (Serializable) mBrands);
        analyzeIntent.putExtra(KEY_VOCA_FILE, vocabularyFile);
        analyzeIntent.putExtra(KEY_URI, uri);
        startService(analyzeIntent);
    }

    /**
     * Ajoute une photo à {@link #mPhotoAdapter} pour l'afficher.
     *
     * @param photo photo à aafficher
     */
    private void addImage(Photo photo) {
        Log.d(TAG, "addImage() called with: photo = [" + photo.toString() + "]");
        mPhotoAdapter.add(photo);
    }

    /**
     * Supprime une photo de {@link #mPhotoAdapter}.
     */
    private void deletePhoto() {
        if (mId == INVALID_POSITION) {
            Utils.toast(this,"please select an item");
            return;
        }

        mPhotoAdapter.remove(mArrayPhoto.get(mId));
        mId = INVALID_POSITION;
    }


    /**
     * Initialise la listView affichant les photos.
     */
    private void initListView() {
        mId = INVALID_POSITION;
        ListView mPhotoListView = (ListView) findViewById(R.id.img_list_view);
        mPhotoListView.setAdapter(mPhotoAdapter);
        mPhotoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: position = [" + position + "] view = [" +
                        view.getClass().getCanonicalName() + "]");
                mId = (position == mId ? -1 : position);

                ((PhotoArrayAdapter) mPhotoAdapter).selectRow(mId);
                mPhotoAdapter.notifyDataSetChanged();
            }
        });
    }
}
