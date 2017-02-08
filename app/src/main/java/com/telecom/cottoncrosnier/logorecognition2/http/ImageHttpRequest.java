package com.telecom.cottoncrosnier.logorecognition2.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

/**
 * Created by matthieu on 19/01/17.
 */

public class ImageHttpRequest extends HttpRequest {

    public static final int IMAGE_REQUEST = 1111;
    public static final String KEY_IMAGE = "key_image";

    private static final String TAG = ImageHttpRequest.class.getSimpleName();
    public ImageHttpRequest(Context context, Handler handler, String baseUrl) {
        super(context, handler, baseUrl);
    }

    @Override
    public void sendRequest(String request) {
        mQueue.add(new ImageRequest(mBaseUrl + request, this, 0, 0, null, null, this));
        this.request = request;
    }

    @Override
    protected void sendMessage(Object response) {

        Message message = mHandler.obtainMessage();
        message.arg1 = IMAGE_REQUEST;

        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_IMAGE, (Bitmap) response);
        bundle.putString(KEY_REQUEST, request);
        message.setData(bundle);

        mHandler.sendMessage(message);
    }

//    @Override
//    public void onResponse(Object response) {
//        Log.d(TAG, "onResponse() called with: response = [" + response + "]");
//        sendMessage(response);
//    }

//    @Override
//    public void onErrorResponse(VolleyError error) {
//
//    }
}
