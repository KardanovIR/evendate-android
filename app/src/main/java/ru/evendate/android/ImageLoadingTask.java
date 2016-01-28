package ru.evendate.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

/**
 * Created by Dmitry on 01.11.2015.
 */
@Deprecated
public class ImageLoadingTask extends AsyncTask<ParcelFileDescriptor, Void, Bitmap> {
    ImageView mImageView;
    public ImageLoadingTask(ImageView imageView) {
        mImageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(ParcelFileDescriptor... params) {
        return BitmapFactory.decodeFileDescriptor(params[0].getFileDescriptor());

    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }
}
