package com.telecom.cottoncrosnier.logorecognition2.http;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

/**
 * Created by matthieu on 19/01/17.
 */

public class StringHttpRequest extends HttpRequest {

    public static final int STRING_REQUEST = 3333;
    public static final String KEY_STRING = "key_string";



    public StringHttpRequest(Context context, Handler handler, String baseUrl) {
        super(context, handler, baseUrl);
    }

    @Override
    public void sendRequest(String request) {
        mQueue.add(new StringRequest(Request.Method.GET, mBaseUrl + request, this, this));
        this.request = request;
    }

    @Override
    public void sendMessage(Object response) {
        Message message = mHandler.obtainMessage();
        message.arg1 = STRING_REQUEST;

        Bundle bundle = new Bundle();
        bundle.putString(KEY_STRING, response.toString());
        bundle.putString(KEY_REQUEST,request);
        message.setData(bundle);

        mHandler.sendMessage(message);
    }

    @Override
    public void onResponse(Object response) {
        sendMessage(response);
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }
}
