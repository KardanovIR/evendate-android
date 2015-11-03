package ru.getlect.evendate.evendate;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.FileDescriptor;

/**
 * Created by denis on 31.10.15.
 */
public class ImageLoader extends AsyncTaskLoader<Bitmap> {
    private FileDescriptor mImageFileDescriptor;
    private ImageView mImageView;
    private ProgressBar mProgressBar;

    public ImageLoader(Context context, FileDescriptor imageFile) {
        super(context);
        mImageFileDescriptor = imageFile;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        forceLoad();
    }

    @Override
    public Bitmap loadInBackground() {
        return BitmapFactory.decodeFileDescriptor(mImageFileDescriptor);
    }
}
