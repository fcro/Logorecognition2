package com.telecom.cottoncrosnier.logorecognition2.http;

import android.content.Context;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * Created by matthieu on 19/01/17.
 */

public class ImageHttpRequest extends HttpRequest {

    private static final String TAG = ImageHttpRequest.class.getSimpleName();
    public ImageHttpRequest(Context context, Handler handler, String baseUrl) {
        super(context, handler, baseUrl);
    }

    @Override
    public void sendRequest(String request) {

        int maxWidth = 10;
        int maxHeight = 10;
        mQueue.add(new ImageRequest(mBaseUrl + request,this,0,0,null,null,this));

    }

    @Override
    public void onResponse(Object response) {
        Log.d(TAG, "onResponse() called with: response = [" + response + "]");
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
