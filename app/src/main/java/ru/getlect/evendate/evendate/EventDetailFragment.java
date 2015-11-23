package ru.getlect.evendate.evendate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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

import org.chalup.microorm.MicroOrm;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateApiFactory;
import ru.getlect.evendate.evendate.sync.EvendateService;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
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

    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mFAB;

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

        mCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.main_content);

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

        mMonthTextView = (TextView)rootView.findViewById(R.id.event_month);
        mDayTextView = (TextView)rootView.findViewById(R.id.event_day);
        mTimeTextView = (TextView)rootView.findViewById(R.id.event_time);
        mParticipantCountTextView = (TextView)rootView.findViewById(R.id.event_participant_count);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.event_organization_icon);
        mOrganizationIconView.setOnClickListener(this);
        mEventImageView = (ImageView)rootView.findViewById(R.id.event_image);

        mFAB = (FloatingActionButton) rootView.findViewById((R.id.fab));

        mUri = mEventDetailActivity.mUri;
        eventId = Integer.parseInt(mUri.getLastPathSegment());
        if(!mEventDetailActivity.isLocal)
            mEventDetailActivity.getSupportLoaderManager().initLoader(EVENT_DESCRIPTION_ID, null,
            (LoaderManager.LoaderCallbacks)this);
        else{
            EventDetailAsyncLoader eventDetailAsyncLoader = new EventDetailAsyncLoader();
            eventDetailAsyncLoader.execute();
        }

        mFAB.setOnClickListener(this);
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
                MicroOrm mOrm = new MicroOrm();
                data.moveToFirst();
                mEventEntry = mOrm.fromCursor(data, EventModel.class);
                eventId = mEventEntry.getEntryId();
                setEventInfo();
                setFabIcon();
                data.close();
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

    private void setEventInfo(){
        mOrganizationTextView.setText(mEventEntry.getOrganizationName());
        mDescriptionTextView.setText(mEventEntry.getDescription());
        mTitleTextView.setText(mEventEntry.getTitle());
        mPlaceTextView.setText(mEventEntry.getLocation());
        //mTagsTextView.setText(data.getString(COLUMN_DESCRIPTION));
        mLinkTextView.setText(mEventEntry.getDetailInfoUrl());
        mParticipantCountTextView.setText(String.valueOf(mEventEntry.getLikedUsersCount()));
        String time;
        if(mEventEntry.isFullDay())
            time = getResources().getString(R.string.event_all_day);
        else{
            //cut off seconds
            //TODO temporary
            time = "";
            if(mEventEntry.getBeginTime() != null && mEventEntry.getEndTime() != null)
                time = mEventEntry.getBeginTime().substring(0, 5) + " - " + mEventEntry.getEndTime().substring(0, 5);
        }
        mTimeTextView.setText(time);
        //convert to milliseconds
        Date date = new Date(mEventEntry.getFirstDate() * 1000L);
        DateFormat[] formats = new DateFormat[] {
                new SimpleDateFormat("dd", Locale.getDefault()),
                new SimpleDateFormat("MMMM", Locale.getDefault()),
        };
        String day = formats[0].format(date);
        if(day.substring(0, 1).equals("0"))
            day = day.substring(1);
        mDayTextView.setText(day);
        mMonthTextView.setText(formats[1].format(date));

        try {
            mParcelFileDescriptor = mEventDetailActivity.getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("events").appendPath(String.valueOf(mEventEntry.getEntryId())).build(), "r");
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
                            .appendPath(String.valueOf(mEventEntry.getOrganizationId())).build(), "r");
            if(fileDescriptor == null)
                mOrganizationIconView.setImageDrawable(getResources().getDrawable(R.drawable.place));
            else{
                mOrganizationIconView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor()));
                fileDescriptor.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        if(v == mOrganizationIconView || v == mOrganizationTextView){
            Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
            intent.setData(EvendateContract.OrganizationEntry.CONTENT_URI.buildUpon().appendPath(String.valueOf(mEventEntry.getOrganizationId())).build());
            startActivity(intent);
        }
        if(v == mFAB) {
            FavoriteAsyncTask favoriteAsyncTask = new FavoriteAsyncTask();
            favoriteAsyncTask.execute();
        }
    }
    private class EventDetailAsyncLoader extends AsyncTask<Void, Void, DataModel> {
        @Override
        protected DataModel doInBackground(Void... params) {

            Account account = EvendateSyncAdapter.getSyncAccount(getContext());
            String token = null;
            try{
                token = AccountManager.get(getContext()).blockingGetAuthToken(account, getContext().getString(R.string.account_type), false);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(token == null)
                return null;
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
    }
    private void setFabIcon(){
        if (mEventEntry.isFavorite()) {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_favorite_on));
        } else {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_favorite_off));
        }
    }
    public boolean favorite(){
        Account account = EvendateSyncAdapter.getSyncAccount(getContext());
        String token = null;
        try{
            token = AccountManager.get(getContext()).blockingGetAuthToken(account, getContext().getString(R.string.account_type), false);
        }catch (Exception e){
            e.printStackTrace();
        }
        if(token == null)
            return false;

        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        if(mEventEntry.isFavorite()){
            return ServerDataFetcher.eventDeleteFavorite(evendateService, token, mEventEntry.getEntryId());
        }
        else{
            return ServerDataFetcher.eventPostFavorite(evendateService, token, mEventEntry.getEntryId());
        }
    }
    private class FavoriteAsyncTask extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {
            ConnectivityManager cm =
                    (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork.isConnectedOrConnecting();
            //Send the user a message to let them know change was made
            if (!isConnected){
                return false;
            }
            return favorite();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result){
                Snackbar.make(mCoordinatorLayout, R.string.subscription_fail_cause_network, Snackbar.LENGTH_LONG).show();
            }
            else{
                mEventEntry.setIsFavorite(!mEventEntry.isFavorite());
                setFabIcon();
                Snackbar.make(mCoordinatorLayout, R.string.subscription_confirm, Snackbar.LENGTH_LONG).show();
                ContentResolver contentResolver = getActivity().getContentResolver();
                contentResolver.update(mUri, mEventEntry.getContentValues(), null, null);
                EvendateSyncAdapter.syncImmediately(getContext());
            }
        }
    }
}
