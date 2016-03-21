package ru.evendate.android.ui;

import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.EventLoader;
import ru.evendate.android.loaders.LikeEventLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.UsersFormatter;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.views.DatesView;
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
    //private CollapsingToolbarLayout mCollapsingToolbarLayout;
    @Bind(R.id.scroll_view) ScrollView mScrollView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.event_image_mask) View mEventImageMask;
    @Bind(R.id.event_organization_mask) View mEventOrganizationMask;
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.event_toolbar_title) TextView mToolbarTitle;
    ObjectAnimator mTitleAppearAnimation;
    ObjectAnimator mTitleDisappearAnimation;
    @Bind(R.id.fab) FloatingActionButton mFAB;

    @Bind(R.id.event_image) ImageView mEventImageView;
    @Bind(R.id.event_organization_icon) ImageView mOrganizationIconView;
    @Bind(R.id.event_organization_name) TextView mOrganizationTextView;
    @Bind(R.id.event_description) TextView mDescriptionTextView;
    @Bind(R.id.event_title) TextView mTitleTextView;
    @Bind(R.id.event_place_button) View mPlaceButtonView;
    @Bind(R.id.event_place_text) TextView mPlacePlaceTextView;
    @Bind(R.id.event_link_card) View mLinkCard;

    @Bind(R.id.tag_layout) TagsView mTagsView;
    @Bind(R.id.event_price_card) android.support.v7.widget.CardView mPriceCard;
    @Bind(R.id.event_price) TextView mPriceTextView;
    @Bind(R.id.event_registration) TextView mRegistrationTextView;
    @Bind(R.id.event_dates) DatesView mDatesView;

    @Bind(R.id.user_card) UserFavoritedCard mUserFavoritedCard;

    @BindString(R.string.event_free) String eventFreeLabel;
    @BindString(R.string.event_price_from) String eventPriceFromLabel;
    @BindString(R.string.event_registration_not_required) String eventRegistrationNotRequiredLabel;
    @BindString(R.string.event_registration_till) String eventRegistrationTillLabel;

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
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        mCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.main_content);

        mToolbar.setTitle("");
        mEventDetailActivity.setSupportActionBar(mToolbar);
        mEventDetailActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);

        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);

        //make status bar transparent
        //((AppBarLayout)rootView.findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new StatusBarColorChanger(getActivity()));
        //((AppBarLayout)rootView.findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
        //    @Override
        //    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //        if (verticalOffset > 0){
        //            //TODO move to behavior?
        //            //CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFAB.getLayoutParams();
        //            //lp.setAnchorId(View.NO_ID);
        //            //mFAB.setLayoutParams(lp);
        //            //lp.gravity = Gravity.BOTTOM | Gravity.END;
        //            //mFAB.setLayoutParams(lp);
        //        }
        //        else{
        //            //CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) mFAB.getLayoutParams();
        //            //lp.setAnchorId(R.id.event_organization_container);
        //            //mFAB.setLayoutParams(lp);
        //            //lp.gravity = Gravity.NO_GRAVITY;
        //            //mFAB.setLayoutParams(lp);
        //        }
        //    }
        //});
        mScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        mToolbarTitle.setAlpha(0f);
        mScrollView.post(new Runnable(){
        @Override
        public void run() {
            ViewTreeObserver observer = mScrollView.getViewTreeObserver();
            observer.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener(){

                @Override
                public void onScrollChanged() {
                    if(mScrollView.getScrollY() >= mEventImageView.getHeight()){
                        mToolbar.setBackgroundColor(getResources().getColor(R.color.primary));
                        if(mTitleDisappearAnimation != null && mTitleDisappearAnimation.isRunning())
                            mTitleDisappearAnimation.cancel();
                        if(mTitleAppearAnimation == null || !mTitleAppearAnimation.isRunning()){
                            mTitleAppearAnimation = ObjectAnimator.ofFloat(mToolbarTitle, "alpha", mToolbarTitle.getAlpha(), 1f);
                            mTitleAppearAnimation.setDuration(200);
                            mTitleAppearAnimation.start();
                        }
                    }
                    else{
                        mToolbar.setBackgroundColor(Color.TRANSPARENT);
                        if(mTitleAppearAnimation != null && mTitleAppearAnimation.isRunning())
                            mTitleAppearAnimation.cancel();
                        if(mTitleDisappearAnimation == null || !mTitleDisappearAnimation.isRunning()) {
                            mTitleDisappearAnimation = ObjectAnimator.ofFloat(mToolbarTitle, "alpha", mToolbarTitle.getAlpha(), 0f);
                            mTitleDisappearAnimation.setDuration(200);
                            mTitleDisappearAnimation.start();
                        }
                    }
                    int color = getResources().getColor(R.color.primary);
                    color = Color.argb(
                            (int)(((float)mScrollView.getScrollY() / mEventImageView.getHeight()) * 255),
                            Color.red(color), Color.green(color), Color.blue(color));
                    mEventImageMask.setBackgroundColor(color);
                    mEventOrganizationMask.setBackgroundColor(color);
                }});
        }});

        //mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.collapsing_toolbar);

        mUri = mEventDetailActivity.mUri;
        eventId = Integer.parseInt(mUri.getLastPathSegment());

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
            mToolbarTitle.setText(mEvent.getTitle());
            setFabIcon();
            mUserFavoritedCard.setUsers(mEvent.getUserList());
            mDatesView.setDates(mEvent.getDateList());
            mPriceTextView.setText(mEvent.isFree() ? eventFreeLabel :
                    (eventPriceFromLabel + " " + mEvent.getMinPrice()));
            mRegistrationTextView.setText(!mEvent.isRegistrationRequired() ? eventRegistrationNotRequiredLabel :
                eventRegistrationTillLabel + " " + mEvent.getRegistrationTill());
        }
    }

    @OnClick({R.id.event_place_button, R.id.event_link_card, R.id.event_organization_container, R.id.fab})
    @Override
    public void onClick(View v) {
        if(mAdapter.getEvent() == null)
            return;
        if(v.getId() == R.id.event_organization_container){
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
        if(v.getId() == R.id.event_link_card && mAdapter.getEvent() != null){
            Intent openLink = new Intent(Intent.ACTION_VIEW);
            openLink.setData(Uri.parse(mAdapter.getEvent().getDetailInfoUrl()));
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
    public void onStart() {
        super.onStart();
        mEventLoader.getData(eventId);
    }

    @Override
    public void onStop() {
        super.onStop();
        mEventLoader.cancel();
    }
}
