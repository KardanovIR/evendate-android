package ru.getlect.evendate.evendate;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.edmodo.cropper.CropImageView;
import com.rey.material.widget.Button;

/**
 * Created by fj on 14.09.2015.
 */
public class CroppActivity extends Activity {


    private int mAspectRatioX = 10;
    private int mAspectRatioY = 7;
    Bitmap croppedImage;

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

        final CropImageView cropImageView = (CropImageView) findViewById(R.id.cropImageView);

        cropImageView.setAspectRatio(mAspectRatioX, mAspectRatioY);

        final Button cropOkButton = (com.rey.material.widget.Button)findViewById(R.id.btn_crop_ok);

        final Button cropButton = (com.rey.material.widget.Button) findViewById(R.id.btn_crop);
        cropButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                croppedImage = cropImageView.getCroppedImage();
                ImageView croppedImageView = (ImageView) findViewById(R.id.croppedImageView);
                croppedImageView.setImageBitmap(croppedImage);
                cropOkButton.setVisibility(View.VISIBLE);
            }
        });




    }






}