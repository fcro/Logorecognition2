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

/**
 * Activité principale.
 * Comporte un bouton d'ajout d'image, et la liste des images analysées.
 */
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
    private static final int EDIT_IMAGE_REQUEST = 3;
    private static final int VIEW_BROWSER_REQUEST = 4;

    private static final String BASE_URL = "http://www-rech.telecom-lille.fr/nonfreesift/";

    private static final String JSON_REQUEST = "index.json";

    private List<Brand> mBrands;
    private String mVocabularyName;
    private File vocabularyFile;

    private ProgressDialog mProgressDialog;

    private ArrayList<Photo> mArrayPhoto;
    private ArrayAdapter<Photo> mPhotoAdapter;

    private FileManager mFileManager;

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

        mArrayPhoto = new ArrayList<>();
        mPhotoAdapter = new PhotoArrayAdapter(
                this, R.layout.listview_row, mArrayPhoto);
        initListView();

        mFileManager = new FileManager(this);

        JsonHttpRequest jsonHttpRequest = new JsonHttpRequest(this, handler, BASE_URL);
        jsonHttpRequest.sendRequest(JSON_REQUEST);

        setBroadcastReceiver();
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
            Uri galleryImgPath = data.getData();
            Uri imgPath = Uri.fromFile(Utils.galleryToCache(this, galleryImgPath, "temp"));
            Log.d(TAG, "onActivityResult:: imgPath = " + imgPath.toString());

            Intent editImageIntent = new Intent(this, EditImageActivity.class);
            editImageIntent.putExtra(KEY_PHOTO_PATH, imgPath);
            startActivityForResult(editImageIntent, EDIT_IMAGE_REQUEST);

        } else if (requestCode == GALLERY_IMAGE_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "onActivityResult:: gallery & canceled");

        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            Bundle b = data.getExtras();
            Uri imgPath = b.getParcelable(KEY_PHOTO_PATH);

            Intent editImageIntent = new Intent(this, EditImageActivity.class);
            editImageIntent.putExtra(KEY_PHOTO_PATH, imgPath);
            startActivityForResult(editImageIntent, EDIT_IMAGE_REQUEST);
            Log.d(TAG, "onActivityResult:: imgPath = " + imgPath.toString());

        } else if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_CANCELED) {
            Log.d(TAG, "onActivityResult: take photo & canceled");

        } else if (requestCode == EDIT_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: edit image request & ok");
            startAnalyze(data.getData());
        }
    }

    /**
     * Appelé quand on reçoit un fichier texte depuis le serveur web.
     * @param data coutenu du fichier reçu.
     */
    private void onStringRequestResult(Bundle data){
        Log.d(TAG, "onStringRequestResult() called ");
        if (data.getString(HttpRequest.KEY_REQUEST).equals(mVocabularyName)) {
            // fichier de vocabulaire
            Log.d(TAG, "onStringRequestResult: received vocabulary");
            try {
                Log.d(TAG, "onStringRequestResult: createVocaFile");
                vocabularyFile = mFileManager.createVocabularyFile(getCacheDir(),
                        data.getString(StringHttpRequest.KEY_STRING));

                mProgressDialog.setMessage("Downloaded " + mVocabularyName + "...");
            } catch (Exception e) {
                e.printStackTrace();
                StringHttpRequest ymlRequest = new StringHttpRequest(this, handler, BASE_URL);
                ymlRequest.sendRequest(mVocabularyName);
            }
        } else {
            // fichier de classifier
            final String requestedBrand = data.getString(StringHttpRequest.KEY_REQUEST);
            final String classifierContent = data.getString(StringHttpRequest.KEY_STRING);

            try {
                Utils.getBrandByClassifier(mBrands, requestedBrand).setLocalClassifier(mFileManager.createClassifierFile(
                        getCacheDir(), classifierContent, requestedBrand));
                mProgressDialog.setMessage("Downloaded " + requestedBrand + "...");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Appelé quand on reçoit un fichier JSON depuis le serveur web.
     * @param data coutenu du fichier reçu.
     */
    private void onJSONRequestResult(Bundle data) {
        try {
            mProgressDialog = ProgressDialog.show(
                    this, getString(R.string.progress_wait), data.getString(JsonHttpRequest.KEY_REQUEST) + "...");
            JSONObject jsonData = new JSONObject(data.getString(JsonHttpRequest.KEY_JSON));
            JsonParser jsonParser = new JsonParser(jsonData);
            mBrands = jsonParser.readBrandArray();
            mVocabularyName = jsonParser.readVocabulary();
            Log.d(TAG, "onJSONRequestResult: brands = " + mBrands);
            Log.d(TAG, "onJSONRequestResult: vocabulary = " + mVocabularyName);

            mFileManager.setBrandsNumber(mBrands.size());
            getClassifiers();
            getVocabulary();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Appelé quand on reçoit un fichier image depuis le serveur web.
     * @param data coutenu du fichier reçu.
     */
    private void onImageRequestResult(Bundle data){
        Log.d(TAG, "onImageRequestResult: request = " + data.getString(HttpRequest.KEY_REQUEST));
        Bitmap img = data.getParcelable(ImageHttpRequest.KEY_IMAGE);
        Log.d(TAG, "onImageRequestResult: img " + img.toString());
    }

    /**
     * Pour chaque marque connue, envoie une requête HTTP GET au serveur web pour récupérer le
     * classifier de chaque marque.
     */
    private void getClassifiers() {
        for (Brand brand : mBrands) {
            new StringHttpRequest(this, handler, BASE_URL)
                    .sendRequest("classifiers/" + brand.getClassifierFile());
        }
    }

    /**
     * Envoie une requette HTTP GET au serveur web pour récupérer le vocabulaire OpenCV.
     */
    private void getVocabulary() {
        new StringHttpRequest(this, handler, BASE_URL)
                .sendRequest(mVocabularyName);
    }

    /**
     * Démarre l'activité de prise de photo par la caméra.
     */
    public void startCamera() {
        Intent startTakePhoto = new Intent(MainActivity.this, TakePhotoActivity.class);
        startActivityForResult(startTakePhoto, TAKE_PHOTO_REQUEST);
    }

    /**
     * Démarre l'activité de prise de photo dans la gallerie.
     */
    public void startGalleryChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a photo"),
                GALLERY_IMAGE_REQUEST);
    }

    /**
     * Démarre un navigateur web pointant sur la page web de la marque sélectionnée.
     */
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

    /**
     * Démarre le service d'analyse d'image.
     * @param uri chemin vers l'image à analyser.
     */
    private void startAnalyze(final Uri uri) {
        Intent analyzeIntent = new Intent(this, AnalyseService.class);

        analyzeIntent.putExtra(KEY_BRANDLIST, (Serializable) mBrands);
        analyzeIntent.putExtra(KEY_VOCA_FILE, vocabularyFile);
        analyzeIntent.putExtra(KEY_URI, uri);
        startService(analyzeIntent);

        mProgressDialog = ProgressDialog.show(
                this, getString(R.string.progress_analyzing), getString(R.string.progress_wait));
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


    /**
     * Appelé quand tous les fichiers nécessaires aux analyses ont été créés.
     * Retire le {@link ProgressDialog} et affiche le bouton d'ajout d'images.
     */
    public void onDatabaseInitFinished() {
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
        Log.d(TAG, "onDatabaseInitFinished: visible");

        mProgressDialog.dismiss();
    }


    /**
     * Initialise le {@link BroadcastReceiver} local à l'application.
     * Utilisé par le service d'analyse d'image pour envoyer le résultat de l'analyse.
     */
    private void setBroadcastReceiver() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Brand brand = (Brand) intent.getSerializableExtra(KEY_RESPONSE_BRAND);
                Uri imgPath = intent.getParcelableExtra(KEY_PHOTO_PATH);
                Log.d(TAG, "onReceive: brand = " + intent.getSerializableExtra(KEY_RESPONSE_BRAND)
                        + " ; imgPath = " + imgPath);

                try {
                    Bitmap bitmap = ThumbnailUtils.extractThumbnail(
                            MediaStore.Images.Media.getBitmap(getContentResolver(), imgPath),
                            300,
                            300);
                    addImage(new Photo(bitmap, brand));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mProgressDialog.dismiss();

//                LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mBroadcastReceiver);
            }
        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(AnalyseService.BROADCAST_ACTION_ANALYZE));
    }
}
