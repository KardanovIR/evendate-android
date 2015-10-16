package ru.getlect.evendate.evendate;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {
    private DetailActivity mDetailActivity;

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


        CollapsingToolbarLayout collapsingToolbarLayout;
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        //collapsingToolbarLayout.setTitle("test");
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));


        TextView textView = (TextView)rootView.findViewById(R.id.event_description);
        Toolbar toolbar = (Toolbar)rootView.findViewById(R.id.toolbar2);
        final String[] PROJECTION = new String[] {
                EvendateContract.EventEntry._ID,
                EvendateContract.EventEntry.COLUMN_TITLE,
                EvendateContract.EventEntry.COLUMN_DESCRIPTION,
                EvendateContract.EventEntry.COLUMN_END_DATE
        };

        final int COLUMN_ID = 0;
        final int COLUMN_TITLE = 1;
        final int COLUMN_DESCRIPTION = 2;
        final int COLUMN_END_DATE = 3;
        final Uri uri = mDetailActivity.mUri;
        Cursor c = getActivity().getContentResolver().query(uri, PROJECTION, null, null, null);
        c.moveToFirst();
        textView.setText(c.getString(COLUMN_DESCRIPTION));
        //collapsingToolbarLayout.setTitle(c.getString(COLUMN_TITLE));
        TextView textView1 = (TextView)rootView.findViewById(R.id.event_name);
        textView1.setText(c.getString(COLUMN_TITLE));
        toolbar.setTitle(c.getString(COLUMN_END_DATE));

        c.close();
        ImageView imageView = (ImageView)rootView.findViewById(R.id.event_image);
        ContentResolver contentResolver = getActivity().getContentResolver();
        try {
            final ParcelFileDescriptor fileDescriptor = contentResolver.openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon().appendPath("image_test").build(), "r");

            imageView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor()));
            fileDescriptor.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        return rootView;
    }

    public DetailActivityFragment() {
        super();
    }
}
