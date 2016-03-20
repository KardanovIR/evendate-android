package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.EventLoader;
import ru.evendate.android.loaders.LikeEventLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.sync.EvendateApiFactory;

/**
 * contain details of events
 */
public class EventDetailFragment extends Fragment implements View.OnClickListener,
        LoaderListener<EventDetail>{
    private static String LOG_TAG = EventDetailFragment.class.getSimpleName();

    private EventDetailActivity mEventDetailActivity;

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
    private ProgressBar mProgressBar;
    private EventAdapter mAdapter;
    private EventLoader mEventLoader;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventDetailActivity = (EventDetailActivity)getActivity();
    }
    @SuppressWarnings({"ConstantConditions", "deprecation"})
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        setHasOptionsMenu(true);

        mCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.main_content);

        mEventDetailActivity.setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));
        mEventDetailActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);

        //make status bar transparent
        ((AppBarLayout)rootView.findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new StatusBarColorChanger(getActivity()));

        CollapsingToolbarLayout collapsingToolbarLayout;
        collapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(android.R.color.transparent));

        mOrganizationTextView = (TextView)rootView.findViewById(R.id.event_organization);
        mDescriptionTextView = (TextView)rootView.findViewById(R.id.event_description);
        mTitleTextView = (TextView)rootView.findViewById(R.id.event_name);
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

        mFAB.setOnClickListener(this);

        mLink = (FrameLayout)rootView.findViewById(R.id.event_link_content);
        rootView.findViewById(R.id.event_participant_button).setOnClickListener(this);

        mAdapter = new EventAdapter();
        mEventLoader = new EventLoader(getActivity());
        mEventLoader.setLoaderListener(this);
        mEventLoader.getData(eventId);
        return rootView;
    }

    private class EventAdapter{
        private EventDetail mEvent;

        public void setEvent(EventDetail event) {
            mEvent = event;
        }

        public EventDetail getEvent() {
            return mEvent;
        }

        private void setEventInfo(){
            //prevent illegal state exception cause fragment not attached to
            if(!isAdded())
                return;
            //TODO
            mOrganizationTextView.setText(mEvent.getOrganizationName());
            mDescriptionTextView.setText(mEvent.getDescription());
            mTitleTextView.setText(mEvent.getTitle());
            if(mEvent.getTitle().length() > 60)
                mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            mPlaceTextView.setText(mEvent.getLocation());
            if(mEvent.getLocation().length() > 30)
                mPlaceTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
            mTagsTextView.setText(EventFormatter.formatTags(mEvent));
            mLinkTextView.setText(mEvent.getDetailInfoUrl());
            mParticipantCountTextView.setText(String.valueOf(mEvent.getLikedUsersCount()));
            //mTimeTextView.setText(EventFormatter.formatTime(mEvent.getFirstDate()));
            mDayTextView.setText(EventFormatter.formatDay(mEvent.getFirstDate()));
            mMonthTextView.setText(EventFormatter.formatMonth(mEvent.getFirstDate()));
            mDateTextView.setText(EventFormatter.formatDate(mEvent));

            Picasso.with(getContext())
                    .load(mEvent.getImageHorizontalUrl())
                    .error(R.drawable.default_background)
                    .into(mEventImageView);
            Picasso.with(getContext())
                    .load(mEvent.getOrganizationLogoUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(mOrganizationIconView);
            mLink.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent openLink = new Intent(Intent.ACTION_VIEW);
                    openLink.setData(Uri.parse(mEvent.getDetailInfoUrl()));
                    Tracker tracker = EvendateApplication.getTracker();
                    HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                            .setCategory(getString(R.string.stat_category_event))
                            .setAction(getString(R.string.stat_action_click_on_link))
                            .setLabel(mUri.getLastPathSegment());
                    tracker.send(event.build());
                    startActivity(openLink);
                }
            });
            setFabIcon();
        }
    }

    @Override
    public void onClick(View v) {
        if(mAdapter.getEvent() == null)
            return;
        if(v == mOrganizationIconView || v == mOrganizationTextView){
            Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
            intent.setData(EvendateContract.OrganizationEntry.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(mAdapter.getEvent().getOrganizationId())).build());
            startActivity(intent);
        }
        if(v == mFAB) {
            Tracker tracker = EvendateApplication.getTracker();
            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                    .setCategory(getActivity().getString(R.string.stat_category_event))
                    .setLabel((Long.toString(mAdapter.getEvent().getEntryId())));

            LikeEventLoader likeEventLoader = new LikeEventLoader(getActivity(), mAdapter.getEvent(),
                    mAdapter.getEvent().isFavorite());
            likeEventLoader.setLoaderListener(new LoaderListener<Void>() {
                @Override
                public void onLoaded(Void subList) {

                }

                @Override
                public void onError() {
                    Toast.makeText(getActivity(), R.string.download_error, Toast.LENGTH_SHORT).show();
                }
            });
            likeEventLoader.load();
            mAdapter.getEvent().favore();
            if(mAdapter.getEvent().isFavorite()){
                event.setAction(getActivity().getString(R.string.stat_action_like));
                Snackbar.make(mCoordinatorLayout, R.string.favorite_confirm, Snackbar.LENGTH_LONG).show();
            }
            else{
                event.setAction(getActivity().getString(R.string.stat_action_dislike));
                Snackbar.make(mCoordinatorLayout, R.string.remove_favorite_confirm, Snackbar.LENGTH_LONG).show();
            }
            tracker.send(event.build());
            mAdapter.setEventInfo();
        }
        if(v.getId() == R.id.event_participant_button){
            Intent intent = new Intent(getContext(), UserListActivity.class);
            intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(mAdapter.getEvent().getEntryId())).build());
            intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.event.nativeInt);
            startActivity(intent);
        }
    }

    @SuppressWarnings("deprecation")
    private void setFabIcon(){
        if (mAdapter.getEvent().isFavorite()) {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_done));
        } else {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_add_white));
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.event_detail_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, mAdapter.getEvent().getTitle() + "\n\n" +
                        mAdapter.getEvent().getDescription() + "\n" +
                        ConstructUrl());
                shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(mEventImageView));
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, getActivity().getString(R.string.action_share)));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // https://github.com/codepath/android_guides/wiki/Sharing-Content-with-Intents
    /**
     * Returns the URI path to the Bitmap displayed in specified ImageView
     */
    public Uri getLocalBitmapUri(ImageView imageView) {
        // Extract Bitmap from ImageView drawable
        Drawable drawable = imageView.getDrawable();
        Bitmap bmp;
        if (drawable instanceof BitmapDrawable){
            bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        } else {
            return null;
        }
        // Store image to default external storage directory
        Uri bmpUri = null;
        try {
            // Use methods on Context to access package-specific directories on external storage.
            // This way, you don't need to request external read/write permission.
            // See https://youtu.be/5xVh-7ywKpE?t=25m25s
            File file = new File(getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.close();
            bmpUri = Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bmpUri;
    }

    public String ConstructUrl(){
        final String base = EvendateApiFactory.HOST_NAME + "/event.php?id=";
        return base + mAdapter.getEvent().getEntryId();
    }

    @Override
    public void onLoaded(EventDetail event) {
        if(!isAdded())
            return;
        mAdapter.setEvent(event);
        mAdapter.setEventInfo();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        if(!isAdded())
            return;
        mProgressBar.setVisibility(View.GONE);
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEventLoader.getData(eventId);
                mProgressBar.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
        mEventLoader.cancel();
    }
}
