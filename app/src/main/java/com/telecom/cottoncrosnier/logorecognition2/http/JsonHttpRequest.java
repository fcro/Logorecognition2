package com.telecom.cottoncrosnier.logorecognition2.http;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * Created by matthieu on 19/01/17.
 */

public class JsonHttpRequest extends HttpRequest {

    private static final String TAG = JsonHttpRequest.class.getSimpleName();
    public JsonHttpRequest(Context context, Handler handler, String baseUrl) {
        super(context, handler, baseUrl);
        Log.d(TAG, "JsonHttpRequest() called with: context = [" + context + "], handler = [" + handler + "], baseUrl = [" + baseUrl + "]");
    }

    @Override
    public void sendRequest(String request) {
        Log.d(TAG, "sendRequest() called with: request = [" + request + "]");
        mQueue.add(new JsonObjectRequest(Request.Method.GET, mBaseUrl + request,null,this,this) {
        });
    }

    @Override
    public void onResponse(Object response) {
        Log.d(TAG, "onResponse() called with: response = [" + response + "]");
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
