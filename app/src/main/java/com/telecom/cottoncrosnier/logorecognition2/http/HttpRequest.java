package com.telecom.cottoncrosnier.logorecognition2.http;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

/**
 * Created by fcro on 19/01/2017.
 */

public class HttpRequest implements Response.Listener, Response.ErrorListener {

    private static final String TAG = HttpRequest.class.getSimpleName();

    private String mBaseUrl;
    private Handler mHandler;
    private RequestQueue mQueue;


    public HttpRequest(Context context, Handler handler, String baseUrl) {
        mQueue = Volley.newRequestQueue(context);
        mHandler = handler;
        mBaseUrl = baseUrl;
    }


    public void sendRequest(String chaussure) {
        mQueue.add(new StringRequest(Request.Method.GET, mBaseUrl + chaussure, this, this));
    }

    @Override
    public void onResponse(Object response) {
        Message message = mHandler.obtainMessage();
        message.arg1 = 1;

        Bundle bundle = new Bundle();
        bundle.putString("test", (String) response);
        message.setData(bundle);

        mHandler.sendMessage(message);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
