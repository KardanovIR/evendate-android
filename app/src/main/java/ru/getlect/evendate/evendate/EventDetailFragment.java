package ru.getlect.evendate.evendate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import ru.getlect.evendate.evendate.authorization.AuthActivity;
import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateApiFactory;
import ru.getlect.evendate.evendate.sync.EvendateService;
import ru.getlect.evendate.evendate.sync.ImageLoaderTask;
import ru.getlect.evendate.evendate.sync.ServerDataFetcher;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class EventDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
View.OnClickListener{
    private EventDetailActivity mEventDetailActivity;
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
    private int organizationId;
    private int eventId;
    private EventModel mEventEntry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventDetailActivity = (EventDetailActivity)getActivity();
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mEventDetailActivity.setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));
        mEventDetailActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //make status bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            // Set the status bar to dark-semi-transparentish
            mEventDetailActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
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

        mOrganizationTextView.setOnClickListener(this);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.event_organization_icon);
        mOrganizationIconView.setOnClickListener(this);
        mEventImageView = (ImageView)rootView.findViewById(R.id.event_image);

        mUri = mEventDetailActivity.mUri;
        eventId = Integer.parseInt(mUri.getLastPathSegment());
        if(!mEventDetailActivity.isLocal)
            mEventDetailActivity.getSupportLoaderManager().initLoader(EVENT_DESCRIPTION_ID, null,
            (LoaderManager.LoaderCallbacks)this);
        else{
            EventDetailAsyncLoader eventDetailAsyncLoader = new EventDetailAsyncLoader();
            eventDetailAsyncLoader.execute();
        }

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case EVENT_DESCRIPTION_ID:
            return new CursorLoader(
                getActivity(),
                mUri,
                null,
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
                mEventDetailActivity.finish();
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
            mParcelFileDescriptor = mEventDetailActivity.getContentResolver()
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
            final ParcelFileDescriptor fileDescriptor = mEventDetailActivity.getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("organizations").appendPath("logos")
                            .appendPath(data.getString(COLUMN_ORGANIZATION_ID)).build(), "r");
            if(fileDescriptor == null)
                mOrganizationIconView.setImageDrawable(getResources().getDrawable(R.drawable.place));
            else{
                mOrganizationIconView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor()));
                fileDescriptor.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        organizationId = data.getInt(COLUMN_ORGANIZATION_ID);
        data.close();
    }

    @Override
    public void onClick(View v) {
        if(v == mOrganizationIconView || v == mOrganizationTextView){
            Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
            intent.setData(EvendateContract.OrganizationEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(organizationId)).build());
            startActivity(intent);
        }
    }
    private class EventDetailAsyncLoader extends AsyncTask<Void, Void, DataModel> {
        @Override
        protected DataModel doInBackground(Void... params) {
            AccountManager accountManager = AccountManager.get(getContext());
            Account[] accounts = accountManager.getAccountsByType(getContext().getString(R.string.account_type));
            if (accounts.length == 0) {
                Log.e("SYNC", "No Accounts");
                Intent dialogIntent = new Intent(getContext(), AuthActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(dialogIntent);
            }
            Account account = accounts[0];
            String token = null;
            try{
                token = accountManager.blockingGetAuthToken(account, getContext().getString(R.string.account_type), false);
            }catch (Exception e){
                e.printStackTrace();
            }
            EvendateService evendateService = EvendateApiFactory.getEvendateService();
            return ServerDataFetcher.getEventData(evendateService, token, eventId);
        }

        @Override
        protected void onPostExecute(DataModel dataModel) {
            setEvent((EventModel) dataModel);
        }
    }
    public void setEvent(EventModel event){
        mEventEntry = event;

        //mOrganizationTextView.setText();
        //mDescriptionTextView.setText(data.getString(COLUMN_DESCRIPTION));
        mTitleTextView.setText(event.getTitle());
        //mPlaceTextView.setText(data.getString(COLUMN_LOCATION_TEXT));
        //mTagsTextView.setText(data.getString(COLUMN_DESCRIPTION));
        //mLinkTextView.setText(data.getString(COLUMN_DETAIL_INFO_URL));


        mEventImageView.setImageBitmap(null);
        if(event.getImageHorizontalUrl() == null)
            mEventImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
        else{
            ImageLoaderTask imageLoader = new ImageLoaderTask(mEventImageView);
            imageLoader.execute(event.getImageHorizontalUrl());
        }

        try{
            final ParcelFileDescriptor fileDescriptor = mEventDetailActivity.getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("organizations").appendPath("logos")
                            .appendPath(String.valueOf(event.getOrganizationId())).build(), "r");
            if(fileDescriptor == null)
                mOrganizationIconView.setImageDrawable(getResources().getDrawable(R.drawable.place));
            else{
                mOrganizationIconView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor()));
                fileDescriptor.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        organizationId = event.getOrganizationId();
    }
}
