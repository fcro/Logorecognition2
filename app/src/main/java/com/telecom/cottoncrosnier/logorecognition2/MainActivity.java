package com.telecom.cottoncrosnier.logorecognition2;

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

import com.telecom.cottoncrosnier.logorecognition2.http.ImageHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.JsonHttpRequest;
import com.telecom.cottoncrosnier.logorecognition2.http.StringHttpRequest;
import com.telecomlille.cottoncrosnier.logorecognition2.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final String BASE_URL = "http://www-rech.telecom-lille.fr/nonfreesift/";

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
        JsonHttpRequest jsonHttpRequest = new JsonHttpRequest(this, handler, BASE_URL);
        jsonHttpRequest.sendRequest("index.json");
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
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: msg.arg1 = " + msg.arg1);
            Log.d(TAG, "handleMessage: msg = " + msg.getData().getString("test"));
            return true;
        }
    });
}
