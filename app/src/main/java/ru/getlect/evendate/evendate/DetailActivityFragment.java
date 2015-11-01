package ru.getlect.evendate.evendate;

import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;

import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Bitmap>{
    private DetailActivity mDetailActivity;
    /** Loader id that get images */
    public static final int EVENT_IMAGE_ID = 0;

    private ImageView mImageView;
    private ParcelFileDescriptor mParcelFileDescriptor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDetailActivity = (DetailActivity)getActivity();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mDetailActivity.setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));
        mDetailActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //make status bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            // Set the status bar to dark-semi-transparentish
            mDetailActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        CollapsingToolbarLayout collapsingToolbarLayout;
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        //collapsingToolbarLayout.setTitle("test");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));


        TextView textView = (TextView)rootView.findViewById(R.id.event_description);
        //Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.toolbar2);
        final String[] PROJECTION = new String[] {
                EvendateContract.EventEntry._ID,
                EvendateContract.EventEntry.COLUMN_TITLE,
                EvendateContract.EventEntry.COLUMN_DESCRIPTION,
                EvendateContract.EventEntry.COLUMN_END_DATE,
                EvendateContract.EventEntry.COLUMN_EVENT_ID,
        };

        final int COLUMN_ID = 0;
        final int COLUMN_TITLE = 1;
        final int COLUMN_DESCRIPTION = 2;
        final int COLUMN_END_DATE = 3;
        final int COLUMN_EVENT_ID = 4;
        final Uri uri = mDetailActivity.mUri;
        Cursor c = getActivity().getContentResolver().query(uri, PROJECTION, null, null, null);
        c.moveToFirst();
        textView.setText(c.getString(COLUMN_DESCRIPTION));
        //collapsingToolbarLayout.setTitle(c.getString(COLUMN_TITLE));
        TextView textView1 = (TextView)rootView.findViewById(R.id.event_name);
        textView1.setText(c.getString(COLUMN_TITLE));
        //toolbar.setTitle(c.getString(COLUMN_END_DATE));

        mImageView = (ImageView)rootView.findViewById(R.id.event_image);
        ContentResolver contentResolver = getActivity().getContentResolver();
        try {
            mParcelFileDescriptor = contentResolver.openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon().appendPath("images").appendPath("events").appendPath(c.getString(COLUMN_EVENT_ID)).build(), "r");
            if(mParcelFileDescriptor == null)
                //заглушка на случай отсутствия картинки
                mImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
            else {
                mDetailActivity.getLoaderManager().initLoader(EVENT_IMAGE_ID, null,
                        (LoaderManager.LoaderCallbacks) this);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        c.close();
        return rootView;
    }

    @Override
    public Loader<Bitmap> onCreateLoader(int id, Bundle args) {
        switch (id){
            case EVENT_IMAGE_ID:
                return new ImageLoader(mDetailActivity, mParcelFileDescriptor.getFileDescriptor());
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Bitmap> loader, Bitmap data) {
        switch (loader.getId()){
            case EVENT_IMAGE_ID:
                mImageView.setImageBitmap(data);
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Bitmap> loader) {
        switch (loader.getId()) {
            case EVENT_IMAGE_ID:
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

}
