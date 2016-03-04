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
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import net.opacapp.multilinecollapsingtoolbar.CollapsingToolbarLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.AbstractLoader;
import ru.evendate.android.loaders.EventLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventModel;
import ru.evendate.android.models.UsersFormatter;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponse;
import ru.evendate.android.views.TagsView;
import ru.evendate.android.views.UserFavoritedCard;

/**
 * contain details of events
 */
public class EventDetailFragment extends Fragment implements View.OnClickListener,
        LoaderListener<EventDetail>{
    private static String LOG_TAG = EventDetailFragment.class.getSimpleName();

    private EventDetailActivity mEventDetailActivity;

    private Uri mUri;
    private int eventId;
    private ProgressBar mProgressBar;
    private EventAdapter mAdapter;
    private EventLoader mEventLoader;

    private CoordinatorLayout mCoordinatorLayout;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private FloatingActionButton mFAB;

    private ImageView mEventImageView;
    private ImageView mOrganizationIconView;

    private TextView mOrganizationTextView;
    private TextView mDescriptionTextView;
    //private TextView mTitleTextView;
    private View mPlaceButtonView;
    private TextView mPlacePlaceTextView;
    private View mLinkCard;

    private TagsView mTagsView;

    private UserFavoritedCard mUserFavoritedCard;

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
        ((AppBarLayout)rootView.findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (verticalOffset > 0){
                    //TODO move to behavior?
                    //CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFAB.getLayoutParams();
                    //lp.setAnchorId(View.NO_ID);
                    //mFAB.setLayoutParams(lp);
                    //lp.gravity = Gravity.BOTTOM | Gravity.END;
                    //mFAB.setLayoutParams(lp);
                }
                else{
                    //CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFAB.getLayoutParams();
                    //lp.setAnchorId(R.id.event_organization_container);
                    //mFAB.setLayoutParams(lp);
                    //lp.gravity = Gravity.NO_GRAVITY;
                    //mFAB.setLayoutParams(lp);
                }
            }
        });


        mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        mOrganizationTextView = (TextView)rootView.findViewById(R.id.event_organization_name);
        mDescriptionTextView = (TextView)rootView.findViewById(R.id.event_description);
        //mTitleTextView = (TextView)rootView.findViewById(R.id.event_name);
        mPlaceButtonView = rootView.findViewById(R.id.event_place_button);
        mPlacePlaceTextView = (TextView)rootView.findViewById(R.id.event_place_text);
        mPlaceButtonView.setOnClickListener(this);
        mTagsView = (TagsView)rootView.findViewById(R.id.tag_layout);
        mLinkCard = rootView.findViewById(R.id.event_link_card);
        mLinkCard.setOnClickListener(this);
        rootView.findViewById(R.id.event_organization_container).setOnClickListener(this);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.event_organization_icon);
        mEventImageView = (ImageView)rootView.findViewById(R.id.event_image);

        mFAB = (FloatingActionButton) rootView.findViewById((R.id.fab));

        mUri = mEventDetailActivity.mUri;
        eventId = Integer.parseInt(mUri.getLastPathSegment());

        mFAB.setOnClickListener(this);

        mUserFavoritedCard = (UserFavoritedCard)rootView.findViewById(R.id.user_card);
        mUserFavoritedCard.setOnAllButtonListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserListActivity.class);
                intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(mAdapter.getEvent().getEntryId())).build());
                intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.event.nativeInt);
                startActivity(intent);
            }
        });

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
            //mTitleTextView.setText(mEvent.getTitle());
            mPlacePlaceTextView.setText(mEvent.getLocation());
            mTagsView.setTags(mEvent.getTagList());
            Picasso.with(getContext())
                    .load(mEvent.getImageHorizontalUrl())
                    .error(R.drawable.default_background)
                    .into(mEventImageView);
            Picasso.with(getContext())
                    .load(mEvent.getOrganizationLogoUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(mOrganizationIconView);
            mUserFavoritedCard.setTitle(UsersFormatter.formatUsers(getContext(), mEvent.getUserList()));
            mCollapsingToolbarLayout.setTitle(mEvent.getTitle());
            setFabIcon();
            mUserFavoritedCard.setUsers(mEvent.getUserList());
        }
    }

    @Override
    public void onClick(View v) {
        if(mAdapter.getEvent() == null)
            return;
        if(v.getId() == R.id.event_organization_container){
            Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
            intent.setData(EvendateContract.OrganizationEntry.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(mAdapter.getEvent().getOrganizationId())).build());

            Tracker tracker = EvendateApplication.getTracker();
            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                    .setCategory(getActivity().getString(R.string.stat_category_organization))
                    .setAction(getActivity().getString(R.string.stat_action_view))
                    .setLabel((Long.toString(mAdapter.getEvent().getOrganizationId())));
            tracker.send(event.build());

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
        if(v.getId() == R.id.event_link_card && mAdapter.getEvent() != null){
            Intent openLink = new Intent(Intent.ACTION_VIEW);
            openLink.setData(Uri.parse(mAdapter.getEvent().getDetailInfoUrl()));
            Tracker tracker = EvendateApplication.getTracker();
            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.stat_category_event))
                    .setAction(getString(R.string.stat_action_click_on_link))
                    .setLabel(mUri.getLastPathSegment());
            tracker.send(event.build());
            startActivity(openLink);
        }
        if(v.getId() == R.id.event_place_button){
            Uri gmmIntentUri = Uri.parse("geo:" + mAdapter.getEvent().getLatitude() +
                    "," + mAdapter.getEvent().getLongitude() + "?q="+ mAdapter.mEvent.getLocation());
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");
            startActivity(mapIntent);
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
}
