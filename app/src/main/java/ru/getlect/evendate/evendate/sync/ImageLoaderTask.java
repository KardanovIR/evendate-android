package ru.getlect.evendate.evendate.sync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ru.getlect.evendate.evendate.ImageLoader;

/**
 * Created by Dmitry on 15.11.2015.
 */
public class ImageLoaderTask extends AsyncTask<String, Void, Bitmap> {
    private String LOG_TAG = ImageLoaderTask.class.getSimpleName();
    private ImageView mImageView;
    public ImageLoaderTask(ImageView imageView) {
        mImageView = imageView;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        try{
            URL url = new URL(params[0]);
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
            if(bitmap == null) {
                Log.e(LOG_TAG, "now image at url " + url);
                return null;
            }
            return bitmap;
        }catch (IOException e){
            Log.e(LOG_TAG, "bitmap load error");
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }
}
