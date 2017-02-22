package com.telecom.cottoncrosnier.logorecognition2.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Activité permettant l'affichage d'un browser
 */
public class LaunchBrowserActivity extends Activity {
    private static final String TAG = LaunchBrowserActivity.class.getSimpleName();

    private final static int VIEW_BROWSER_REQUEST = 1;


    /**
     * Appelée au demarrage de l'activité, appelle le demarrage du browser.
     *
     * @param savedInstanceState Éléments sauvegardés lors du dernier arrêt de l'activité (non utilisé).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();
        Bundle b = intent.getExtras();
        String url = b.getString(MainActivity.KEY_URL);

        launchBrowser(url);
    }

    /**
     * Appelée quand l'activité reprend, après avoir quitté le browser.
     *
     * @param requestCode code de l'activité qui a été appelée.
     * @param resultCode code de retour de l'activité appelée (ok / nok).
     * @param data valeur de retour de l'activité appelée (non utilisé).
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIEW_BROWSER_REQUEST) {
            setResult(RESULT_OK);
            finish();
        }
    }

    /**
     * Lance l'activité affichant le browser.
     *
     * @param url site web à afficher.
     */
    private void launchBrowser(String url) {
        Log.d(TAG, "launchBrowser() called with: url = [" + url + "]");
        toast(url);
        startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse(url)), VIEW_BROWSER_REQUEST);
    }

    /**
     * Affiche une notification Toast.
     *
     * @param text texte à afficher.
     */
    private void toast(String text) {
        Toast.makeText(this, text,
                Toast.LENGTH_LONG).show();
    }
}
