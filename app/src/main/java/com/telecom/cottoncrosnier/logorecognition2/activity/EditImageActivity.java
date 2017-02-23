package com.telecom.cottoncrosnier.logorecognition2.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.soundcloud.android.crop.Crop;
import com.telecomlille.cottoncrosnier.logorecognition2.R;

import java.io.File;

/**
 * Created by fcro on 23/02/2017.
 */

public class EditImageActivity extends Activity {

    private static final String TAG = EditImageActivity.class.getSimpleName();

    private ImageView mImageView;
    private Button mButtonCrop;
    private Button mButtonAnalyse;

    private Uri mImgPath;


    /**
     * Appelée au demarrage de l'activité, initialise l'affichage et appelle l'analyse de la photo.
     *
     * @param savedInstanceState Éléments sauvegardés lors du dernier arrêt de l'activité (non utilisé).
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);
        Log.d(TAG, "onCreate:");

        mImageView = (ImageView) findViewById(R.id.image_view);

        mButtonCrop = (Button) findViewById(R.id.button_crop);
        mButtonAnalyse = (Button)findViewById(R.id.button_analyse);

        final Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mImgPath = b.getParcelable(MainActivity.KEY_PHOTO_PATH);

        mImageView.setImageURI(mImgPath);
        setButtonListener(mImgPath);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Crop.REQUEST_CROP && resultCode == RESULT_OK) {
            Log.d(TAG, "onActivityResult: cropped");
            mImgPath = Crop.getOutput(data);
            mImageView.setImageURI(mImgPath);
        }
    }


    private void setButtonListener(final Uri imgPath){
        mButtonCrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri croppedImgPath = Uri.fromFile(new File(getCacheDir() + "/crop"));
                Crop.of(imgPath, croppedImgPath).start(EditImageActivity.this);
            }
        });

        mButtonAnalyse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();

                resultIntent.setData(imgPath);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }
}
