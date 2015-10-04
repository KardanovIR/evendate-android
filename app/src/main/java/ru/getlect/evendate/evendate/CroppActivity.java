package ru.getlect.evendate.evendate;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;

import com.edmodo.cropper.CropImageView;
import com.rey.material.widget.Button;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by fj on 14.09.2015.
 */
public class CroppActivity extends Activity {


    private int mAspectRatioX = 10;
    private int mAspectRatioY = 7;
    CropImageView cropImageView;
    Bitmap bmImg;
    public static Bitmap croppedImage;
//    public static String sEncodedImage;
    

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putInt("ASPECT_RATIO_X", mAspectRatioX);
        bundle.putInt("ASPECT_RATIO_Y", mAspectRatioY);
    }


    @Override
    protected void onRestoreInstanceState(Bundle bundle) {
        super.onRestoreInstanceState(bundle);
        mAspectRatioX = bundle.getInt("ASPECT_RATIO_X");
        mAspectRatioY = bundle.getInt("ASPECT_RATIO_Y");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropImageView = (CropImageView)findViewById(R.id.cropImageView);
        cropImageView.setAspectRatio(mAspectRatioX,mAspectRatioY);
        cropImageView.setFixedAspectRatio(true);
        final CropImageView cropImageView = (CropImageView) findViewById(R.id.cropImageView);


        String path = getIntent().getExtras().getString("imagePath");


        Uri uriFromPath = Uri.fromFile(new File(path));

        try {
            bmImg = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
            cropImageView.setImageBitmap(bmImg);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }


        final Button cropOkButton = (com.rey.material.widget.Button)findViewById(R.id.btn_crop_ok);

        final Button cropButton = (com.rey.material.widget.Button) findViewById(R.id.btn_crop);
        cropButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);
                croppedImageView.setImageBitmap(croppedImage);
                cropOkButton.setEnabled(true);
            }
        });

        cropOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FetchCropTask fetchCropTask = new FetchCropTask();
                fetchCropTask.execute();
                finish();

            }
        });





    }

    public class FetchCropTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            croppedImage.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            DialogsFragment.sEncodedImage = Base64.encodeToString(byteArray,Base64.DEFAULT);
            return null;
        }

    }






}