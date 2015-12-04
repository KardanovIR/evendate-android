package ru.getlect.evendate.evendate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
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
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.chalup.microorm.MicroOrm;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateApiFactory;
import ru.getlect.evendate.evendate.sync.EvendateService;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.LocalDataFetcher;
import ru.getlect.evendate.evendate.sync.ServerDataFetcher;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventFormatter;
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

    private FrameLayout mLink;

    private Uri mUri;
    private int eventId;
    private EventModel mEventEntry;

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            //setupImage();
        }
    };

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

        mLink = (FrameLayout)rootView.findViewById(R.id.event_link_content);
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
                setDateRange();
                setTags();
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

    private void setDateRange(){
        LocalDataFetcher localDataFetcher = new LocalDataFetcher(getActivity().getContentResolver(), getContext());
        mEventEntry.setDataRangeList(localDataFetcher.getEventDatesDataFromDB(mEventEntry.getEntryId(), true));
    }
    private void setTags(){
        LocalDataFetcher localDataFetcher = new LocalDataFetcher(getActivity().getContentResolver(), getContext());
        mEventEntry.setTagList(localDataFetcher.getEventTagDataFromDB(mEventEntry.getEntryId()));
    }
    private void setEventInfo(){
        EventFormatter eventFormatter = new EventFormatter(getActivity());
        mOrganizationTextView.setText(mEventEntry.getOrganizationName());
        mDescriptionTextView.setText(mEventEntry.getDescription());
        mTitleTextView.setText(mEventEntry.getTitle());
        if(mEventEntry.getTitle().length() > 60)
            mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mPlaceTextView.setText(mEventEntry.getLocation());
        if(mEventEntry.getLocation().length() > 30)
            mPlaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        mTagsTextView.setText(eventFormatter.formatTags(mEventEntry));
        mLinkTextView.setText(mEventEntry.getDetailInfoUrl());
        mParticipantCountTextView.setText(String.valueOf(mEventEntry.getLikedUsersCount()));
        mTimeTextView.setText(eventFormatter.formatTime(mEventEntry));
        mDayTextView.setText(eventFormatter.formatDay(mEventEntry));
        mMonthTextView.setText(eventFormatter.formatMonth(mEventEntry));

        Picasso.with(getContext())
                .load(mEventEntry.getImageHorizontalUrl())
                .error(R.drawable.default_background)
                .into(mEventImageView);
        Picasso.with(getContext())
                .load(mEventEntry.getOrganizationLogoUrl())
                .error(R.mipmap.ic_launcher)
                .into(mOrganizationIconView);
        mLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openLink = new Intent(Intent.ACTION_VIEW);
                openLink.setData(Uri.parse(mEventEntry.getDetailInfoUrl()));
                startActivity(openLink);
            }
        });
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
            mEventEntry = (EventModel)dataModel;
            setEventInfo();
            setFabIcon();
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
            if(activeNetwork == null)
                return false;
            boolean isConnected = activeNetwork.isConnected();
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
                Snackbar.make(mCoordinatorLayout, R.string.no_internet_connection, Snackbar.LENGTH_LONG).show();
            }
            else{
                mEventEntry.setIsFavorite(!mEventEntry.isFavorite());
                if(mEventEntry.isFavorite())
                    mEventEntry.setLikedUsersCount(mEventEntry.getLikedUsersCount() + 1);
                else{
                    mEventEntry.setLikedUsersCount(mEventEntry.getLikedUsersCount() - 1);
                }
                setFabIcon();
                setEventInfo();
                if(mEventEntry.isFavorite())
                    Snackbar.make(mCoordinatorLayout, R.string.favorite_confirm, Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(mCoordinatorLayout, R.string.remove_favorite_confirm, Snackbar.LENGTH_LONG).show();
                ContentResolver contentResolver = getActivity().getContentResolver();
                contentResolver.update(mUri, mEventEntry.getContentValues(), null, null);
                EvendateSyncAdapter.syncImmediately(getContext());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(syncFinishedReceiver, new IntentFilter(EvendateSyncAdapter.SYNC_FINISHED));
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(syncFinishedReceiver);
    }

}
