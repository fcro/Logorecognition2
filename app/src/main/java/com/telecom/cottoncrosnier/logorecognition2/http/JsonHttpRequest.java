package com.telecom.cottoncrosnier.logorecognition2.http;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;

/**
 * Created by matthieu on 19/01/17.
 */

/**
 * Requête Volley pour récupérer un fichier JSON.
 */
public class JsonHttpRequest extends HttpRequest {

    public static final int JSON_REQUEST = 2222;
    public static final String KEY_JSON = "key_json";

    private static final String TAG = JsonHttpRequest.class.getSimpleName();


    /**
     * Instancie une {@link JsonHttpRequest}.
     * @param context contexte appelant.
     * @param handler classe {@link Handler} décrivant l'action à faire selon le fichier
     *                reçu.
     * @param baseUrl adresse du serveur HTTP sur lequel les requêtes seront envoyées.
     */
    public JsonHttpRequest(Context context, Handler handler, String baseUrl) {
        super(context, handler, baseUrl);
        Log.d(TAG, "JsonHttpRequest() called with: context = [" + context + "], handler = [" + handler + "], baseUrl = [" + baseUrl + "]");
    }

    @Override
    public void sendRequest(String request) {
        Log.d(TAG, "sendRequest() called with: request = [" + request + "]");
        mQueue.add(new JsonObjectRequest(Request.Method.GET, mBaseUrl + request, null, this, this) {
        });
        this.request = request;
    }

    @Override
    protected void sendMessage(Object response) {
        Message message = mHandler.obtainMessage();
        message.arg1 = JSON_REQUEST;

        Bundle bundle = new Bundle();
        bundle.putString(KEY_JSON, response.toString());
        bundle.putString(KEY_REQUEST, request);

        message.setData(bundle);

        mHandler.sendMessage(message);
    }
}
