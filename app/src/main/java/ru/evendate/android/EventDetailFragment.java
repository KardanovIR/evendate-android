package ru.evendate.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.chalup.microorm.MicroOrm;

import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateSyncAdapter;
import ru.evendate.android.sync.LocalDataFetcher;
import ru.evendate.android.sync.ServerDataFetcher;
import ru.evendate.android.sync.models.DataModel;
import ru.evendate.android.sync.models.EventFormatter;
import ru.evendate.android.sync.models.EventModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class EventDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
View.OnClickListener{
    private static String LOG_TAG = EventDetailFragment.class.getSimpleName();

    private EventDetailActivity mEventDetailActivity;
    private View eventCover;
    private View stripCover;
    private View eventDescriptionContent;
    private View eventStrip;
    /** Loader id that get images */
   // public static final int EVENT_IMAGE_ID = 0;
    public static final int EVENT_DESCRIPTION_ID = 1;

    private CoordinatorLayout mCoordinatorLayout;
    private FloatingActionButton mFAB;

    private ImageView mEventImageView;
    private ImageView mOrganizationIconView;

    private TextView mOrganizationTextView;
    private TextView mDescriptionTextView;
    private TextView mTitleTextView;
    private TextView mDateTextView;
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
    ProgressBar mProgressBar;

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
        eventCover = rootView.findViewById(R.id.cover);
        stripCover = rootView.findViewById(R.id.strip_cover);
        eventCover.setVisibility(View.VISIBLE);
        stripCover.setVisibility(View.VISIBLE);
        eventDescriptionContent = rootView.findViewById(R.id.event_description_content);
        eventStrip = rootView.findViewById(R.id.strip);


        mCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.main_content);

        mEventDetailActivity.setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));
        mEventDetailActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);

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
        mTitleTextView.setAlpha(0.0f);
        mDateTextView = (TextView)rootView.findViewById(R.id.event_date);
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
        if(mEventDetailActivity.isLocal)
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
                data.close();
                onLoaded();
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
        if(!isAdded())
            return;
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
        mDateTextView.setText(eventFormatter.formatDate(mEventEntry));

        Picasso.with(getContext())
                .load(mEventEntry.getImageHorizontalUrl())
                .error(R.drawable.default_background)
                .into(new Target() {
                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                        if(bitmap == null)
                            return;
                        mEventImageView.setImageBitmap(bitmap);
                        pallete(bitmap);
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                    }
                });
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
            onLoaded();
        }
    }

    private void setFabIcon(){
        if (mEventEntry.isFavorite()) {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_done));
        } else {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_add_white));
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
                setEventInfo();
                if(mEventEntry.isFavorite())
                    Snackbar.make(mCoordinatorLayout, R.string.favorite_confirm, Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(mCoordinatorLayout, R.string.remove_favorite_confirm, Snackbar.LENGTH_LONG).show();
                ContentResolver contentResolver = getActivity().getContentResolver();
                contentResolver.update(mUri, mEventEntry.getContentValues(), null, null);
                //EvendateSyncAdapter.syncImmediately(getContext());
                try {
                    contentResolver.applyBatch(EvendateContract.CONTENT_AUTHORITY, mEventEntry.getInsertDates());
                }catch (Exception e){
                    Log.e(LOG_TAG, e.getMessage(), e);
                    e.printStackTrace();
                }
                onLoaded();
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

    public void onLoaded(){
        if(!isAdded())
            return;
        setEventInfo();
        setFabIcon();
        if (eventCover.getVisibility() != View.VISIBLE && stripCover.getVisibility() != View.VISIBLE)
            return;
        final int duration = 200;
        mTitleTextView.setAlpha(0.0f);
        mTitleTextView.animate()
                .alpha(1.0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                    }
                });
        eventDescriptionContent.setAlpha(0.0f);
        eventDescriptionContent.animate()
                .alpha(1.0f)
                .setDuration(duration)
                .setListener(null);
        eventStrip.setAlpha(0.0f);
        eventStrip.animate()
                .alpha(1.0f)
                .setDuration(duration)
                .setListener(null);
        eventCover.animate()
                .alpha(0.0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        eventCover.setVisibility(View.GONE);
                    }
                });
        stripCover.animate()
                .alpha(0.0f)
                .setDuration(duration)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        stripCover.setVisibility(View.GONE);
                    }
                });
        mProgressBar.setVisibility(View.GONE);
    }
    public void pallete(Bitmap bitmap){
        Palette palette = Palette.generate(bitmap);
        Palette.Swatch mutedSwatch = palette.getMutedSwatch();
        Palette.Swatch mutedLightSwatch = palette.getLightMutedSwatch();
        Palette.Swatch mutedDarkSwatch = palette.getDarkMutedSwatch();
        Palette.Swatch swatch = palette.getVibrantSwatch();
        // Gets the RGB packed int -> same as palette.getVibrantColor(defaultColor);
        int rgbColor = swatch.getRgb();
        // Gets the HSL values
        // Hue between 0 and 360
        // Saturation between 0 and 1
        // Lightness between 0 and 1
        float[] hslValues = swatch.getHsl();
        // Gets the number of pixels represented by this swatch
        int pixelCount = swatch.getPopulation();
        // Gets an appropriate title text color
        int titleTextColor = swatch.getTitleTextColor();
        // Gets an appropriate body text color
        int bodyTextColor = swatch.getBodyTextColor();

        eventStrip.setBackgroundColor(mutedDarkSwatch.getRgb());
        mTitleTextView.setTextColor(mutedDarkSwatch.getTitleTextColor());

    }
}
