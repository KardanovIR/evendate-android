package ru.getlect.evendate.evendate;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private DetailActivity mDetailActivity;
    /** Loader id that get images */
   // public static final int EVENT_IMAGE_ID = 0;
    public static final int EVENT_DESCRIPTION_ID = 1;

    private ImageView mEventImageView;
    private ImageView mOrganizationIconView;
    private ParcelFileDescriptor mParcelFileDescriptor;

    private TextView mOrganizationTextView;
    private TextView mDescriptionTextView;
    private TextView mTitleTextView;
    private TextView mPlaceTextView;
    private TextView mTagsTextView;
    private TextView mLinkTextView;

    private TextView mMonthTextView;
    private TextView mDayTextView;
    private TextView mTimeTextView;
    private TextView mParticipantCountTextView;

    private Uri mUri;

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

        mOrganizationTextView = (TextView)rootView.findViewById(R.id.event_organization);
        mDescriptionTextView = (TextView)rootView.findViewById(R.id.event_description);
        mTitleTextView = (TextView)rootView.findViewById(R.id.event_name);
        mPlaceTextView = (TextView)rootView.findViewById(R.id.event_place);
        mTagsTextView = (TextView)rootView.findViewById(R.id.event_tags);
        mLinkTextView = (TextView)rootView.findViewById(R.id.event_link);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.event_organization_icon);
        mEventImageView = (ImageView)rootView.findViewById(R.id.event_image);

        mUri = mDetailActivity.mUri;

        mDetailActivity.getSupportLoaderManager().initLoader(EVENT_DESCRIPTION_ID, null,
        (LoaderManager.LoaderCallbacks)this);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case EVENT_DESCRIPTION_ID:
            return new CursorLoader(
                getActivity(),
                mUri,
                new String[] {
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry._ID,
                        EvendateContract.EventEntry.COLUMN_TITLE,
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry.COLUMN_DESCRIPTION,
                        EvendateContract.EventEntry.COLUMN_EVENT_ID,
                        EvendateContract.EventEntry.COLUMN_LOCATION_TEXT,
                        EvendateContract.EventEntry.COLUMN_START_DATE,
                        EvendateContract.EventEntry.COLUMN_BEGIN_TIME,
                        EvendateContract.EventEntry.COLUMN_END_TIME,
                        EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL,
                        EvendateContract.EventEntry.TABLE_NAME + "." + EvendateContract.EventEntry.COLUMN_ORGANIZATION_ID,
                        EvendateContract.OrganizationEntry.COLUMN_NAME,
                        EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
                        EvendateContract.OrganizationEntry.COLUMN_IMG_URL,
                },
                null,
                null,
                null
            );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case EVENT_DESCRIPTION_ID:
                setEventInfo(data);
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case EVENT_DESCRIPTION_ID:
                mDetailActivity.finish();
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    private void setEventInfo(Cursor data){
        final int COLUMN_EVENT_ID = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_EVENT_ID);
        final int COLUMN_TITLE = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_TITLE);
        final int COLUMN_DESCRIPTION = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_DESCRIPTION);
        final int COLUMN_LOCATION_TEXT = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_LOCATION_TEXT);
        final int COLUMN_START_DATE = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_START_DATE);
        final int COLUMN_BEGIN_TIME = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_BEGIN_TIME);
        final int COLUMN_END_TIME = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_END_TIME);
        final int COLUMN_DETAIL_INFO_URL = data.getColumnIndex(EvendateContract.EventEntry.COLUMN_DETAIL_INFO_URL);
        final int COLUMN_ORGANIZATION_ID = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID);
        final int COLUMN_ORGANIZATION_NAME = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_NAME);
        final int COLUMN_ORGANIZATION_SHORT_NAME = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME);
        data.moveToFirst();
        mOrganizationTextView.setText(data.getString(COLUMN_ORGANIZATION_NAME));
        mDescriptionTextView.setText(data.getString(COLUMN_DESCRIPTION));
        mTitleTextView.setText(data.getString(COLUMN_TITLE));
        mPlaceTextView.setText(data.getString(COLUMN_LOCATION_TEXT));
        //mTagsTextView.setText(data.getString(COLUMN_DESCRIPTION));
        mLinkTextView.setText(data.getString(COLUMN_DETAIL_INFO_URL));

        try {
            mParcelFileDescriptor = mDetailActivity.getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("events").appendPath(data.getString(COLUMN_EVENT_ID)).build(), "r");
            if(mParcelFileDescriptor == null)
                //заглушка на случай отсутствия картинки
                mEventImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
            else {
                ImageLoadingTask imageLoadingTask = new ImageLoadingTask(mEventImageView);
                imageLoadingTask.execute(mParcelFileDescriptor);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            final ParcelFileDescriptor fileDescriptor = mDetailActivity.getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("organizations").appendPath("logos")
                            .appendPath(data.getString(COLUMN_ORGANIZATION_ID)).build(), "r");
            mOrganizationIconView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor()));
            fileDescriptor.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        data.close();
    }

}
