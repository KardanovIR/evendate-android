package ru.evendate.android.ui;

import android.content.Context;
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
import android.support.v7.widget.ShareActionProvider;
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

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.AbstractLoader;
import ru.evendate.android.loaders.EventLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.models.EventModel;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponse;

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
    ProgressBar mProgressBar;
    EventAdapter mAdapter;
    EventLoader mEventLoader;

    ShareActionProvider mShareActionProvider;

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
        mParticipantCountTextView.setOnClickListener(this);

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
            //mTimeTextView.setText(eventFormatter.formatTime(mEvent));
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
            if(mAdapter.getEvent().isFavorite())
                Snackbar.make(mCoordinatorLayout, R.string.favorite_confirm, Snackbar.LENGTH_LONG).show();
            else
                Snackbar.make(mCoordinatorLayout, R.string.remove_favorite_confirm, Snackbar.LENGTH_LONG).show();
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

    private void setFabIcon(){
        if (mAdapter.getEvent().isFavorite()) {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_done));
        } else {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_add_white));
        }
    }

    private class LikeEventLoader extends AbstractLoader<Void> {
        EventModel mEvent;
        boolean favorite;
        public LikeEventLoader(Context context, EventModel eventModel, boolean favorite) {
            super(context);
            this.favorite = favorite;
            mEvent = eventModel;
        }

        public void load(){
            Log.d(LOG_TAG, "performing like");
            EvendateService evendateService = EvendateApiFactory.getEvendateService();
            Call<EvendateServiceResponse> call;
            if(favorite){
                call = evendateService.eventDeleteFavorite(mEvent.getEntryId(), peekToken());
            }
            else {
                call = evendateService.eventPostFavorite(mEvent.getEntryId(), peekToken());
            }

            call.enqueue(new Callback<EvendateServiceResponse>() {
                @Override
                public void onResponse(Response<EvendateServiceResponse> response,
                                       Retrofit retrofit) {
                    if (response.isSuccess()) {
                        Log.d(LOG_TAG, "performed like");
                    } else {
                        Log.e(LOG_TAG, "Error with response with like");
                        mListener.onError();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("Error", t.getMessage());
                    mListener.onError();
                }
            });
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
                        mAdapter.getEvent().getDetailInfoUrl());
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
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mEventLoader.getData(eventId);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}
