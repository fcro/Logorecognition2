package com.telecom.cottoncrosnier.logorecognition2;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.telecom.cottoncrosnier.logorecognition2.http.HttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.ImageHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.JsonHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.StringHttpRequest;
import com.telecomlille.cottoncrosnier.logorecognition2.R;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String BASE_URL = "http://www-rech.telecom-lille.fr/nonfreesift/";

    private static final String JSON_REQUEST = "index.json";
    private static final String YML_REQUEST = "vocabulary.yml";
    private static final String CLASSIFIER_COCA_REQUEST = "classifiers/Coca.xml";
    private static final String CLASSIFIER_SPRITE_REQUEST = "classifiers/Sprite.xml";
    private static final String CLASSIFIER_PESPSI_REQUEST = "classifiers/Pepsi.xml";

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.arg1){
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
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        //Classifier classifier = new Classifier(this);
        StringHttpRequest httpRequest = new StringHttpRequest(this, handler, BASE_URL);
        httpRequest.sendRequest("test_images.txt");
        httpRequest.sendRequest(CLASSIFIER_COCA_REQUEST);
        httpRequest.sendRequest(CLASSIFIER_PESPSI_REQUEST);
        JsonHttpRequest jsonHttpRequest = new JsonHttpRequest(this, handler, BASE_URL);
        jsonHttpRequest.sendRequest(JSON_REQUEST);
        ImageHttpRequest imageHttpRequest = new ImageHttpRequest(this,handler,BASE_URL);
        imageHttpRequest.sendRequest("train-images/Coca_12.jpg");
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

        //noinspection SimplifiableIfStatement
        //
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void onStringRequestResult(Bundle data){

        switch (data.getString(HttpRequest.KEY_REQUEST)){
            case YML_REQUEST:
                //requete YML
                break;

            case CLASSIFIER_COCA_REQUEST:
                //XML coca
                Log.d(TAG, "onStringRequestResult: result = "+data.getString(StringHttpRequest.KEY_STRING));
                break;
            case CLASSIFIER_PESPSI_REQUEST:
                //XML pepsi
                Log.d(TAG, "onStringRequestResult: result = "+data.getString(StringHttpRequest.KEY_STRING));
                break;

            case CLASSIFIER_SPRITE_REQUEST:
                //XML sprite
                Log.d(TAG, "onStringRequestResult: result = "+data.getString(StringHttpRequest.KEY_STRING));
                break;
            
        }
    }

    private void onJSONRequestResult(Bundle data){
        try {
            JSONObject JSONData = new JSONObject(data.getString(JsonHttpRequest.KEY_JSON));
            Log.d(TAG, "handleMessage: data "+JSONData.toString());
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void onImageRequestResult(Bundle data){
        Log.d(TAG, "handleMessage: request = " + data.getString(HttpRequest.KEY_REQUEST));
        Bitmap img = data.getParcelable(ImageHttpRequest.KEY_IMAGE);
        Log.d(TAG, "handleMessage: img "+img.toString());
    }
}
