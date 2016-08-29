package ru.evendate.android.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.Statistics;
import ru.evendate.android.adapters.NotificationConverter;
import ru.evendate.android.adapters.NotificationListAdapter;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.EventNotificationsLoader;
import ru.evendate.android.loaders.LikeEventLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.models.UsersFormatter;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.views.DatesView;
import ru.evendate.android.views.TagsView;
import ru.evendate.android.views.UserFavoritedCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * contain details of events
 */
public class EventDetailFragment extends Fragment implements LoaderListener<ArrayList<EventDetail>> {
    private static String LOG_TAG = EventDetailFragment.class.getSimpleName();

    private EventDetailActivity mEventDetailActivity;

    private Uri mUri;
    private int eventId;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    private EventAdapter mAdapter;

    @Bind(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
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
    @Bind(R.id.event_organization_icon_container) View mOrganizationIconContainer;
    @Bind(R.id.event_organization_name) TextView mOrganizationTextView;
    @Bind(R.id.event_description) TextView mDescriptionTextView;
    @Bind(R.id.event_title) TextView mTitleTextView;
    @Bind(R.id.event_place_button) View mPlaceButtonView;
    @Bind(R.id.event_place_text) TextView mPlacePlaceTextView;
    @Bind(R.id.event_link_card) View mLinkCard;
    @Bind(R.id.event_organization_container) View mOrganizationContainer;
    @Bind(R.id.event_image_foreground) ImageView mEventForegroundImage;

    @Bind(R.id.tag_layout) TagsView mTagsView;
    @Bind(R.id.event_price_card) android.support.v7.widget.CardView mPriceCard;
    @Bind(R.id.event_price) TextView mPriceTextView;
    @Bind(R.id.event_registration) TextView mRegistrationTextView;
    @Bind(R.id.event_dates) DatesView mDatesView;
    @Bind(R.id.event_dates_light) CardView mDatesLightView;
    @Bind(R.id.event_dates_intervals) TextView mEventDateIntervalsTextView;
    @Bind(R.id.event_time) TextView mEventTimeTextView;

    @Bind(R.id.event_content_container) View mEventContentContainer;
    @Bind(R.id.event_image_container) View mEventImageContainer;

    @Bind(R.id.user_card) UserFavoritedCard mUserFavoritedCard;

    @BindString(R.string.event_free) String eventFreeLabel;
    @BindString(R.string.event_registration_not_required) String eventRegistrationNotRequiredLabel;
    @BindString(R.string.event_registration_till) String eventRegistrationTillLabel;

    DrawerWrapper mDrawer;
    Dialog alertDialog;
    int mFabHeight;
    boolean isFabDown = false;

    ObjectAnimator mFabUpAnimation;
    ObjectAnimator mFabDownAnimation;

    private int mColor;

    private AlertDialog notificationDialog;

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

        mUri = mEventDetailActivity.mUri;
        eventId = Integer.parseInt(mUri.getLastPathSegment());

        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        initToolbar();
        initTransitions();
        initToolbarAnimation();
        initUserFavoriteCard();
        initDrawer();
        mAdapter = new EventAdapter();
        ViewTreeObserver vto = mCoordinatorLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCoordinatorLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                int bottom = mOrganizationContainer.getBottom();
                int size = mFAB.getHeight();
                mFabHeight = bottom - size / 2;
                if(Build.VERSION.SDK_INT > 19)
                    TransitionManager.beginDelayedTransition(mCoordinatorLayout);
                //CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams)mFAB.getLayoutParams();
                //params.setMargins(params.leftMargin, 0, params.rightMargin, mFabHeight);
                //mFAB.setLayoutParams(params);
                mFAB.setY(mFabHeight);
            }
        });

        mColor = getResources().getColor(R.color.primary);

        mFAB.hide();
        mEventImageContainer.setVisibility(View.INVISIBLE);
        mOrganizationIconContainer.setVisibility(View.INVISIBLE);
        //mEventContentContainer.setVisibility(View.INVISIBLE);
        mTitleTextView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mScrollView.setVisibility(View.INVISIBLE);
        return rootView;
    }

    private void initToolbar(){
        mToolbar.setTitle("");
        mEventDetailActivity.setSupportActionBar(mToolbar);
        mEventDetailActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    private void initTransitions(){
        if(Build.VERSION.SDK_INT >= 21){
            getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
            getActivity().getWindow().setExitTransition(new Slide(Gravity.TOP));
        }
    }

    private void initToolbarAnimation(){
        mScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
        mToolbarTitle.setAlpha(0f);
        if (Build.VERSION.SDK_INT >= 21)
            mAppBarLayout.setElevation(0);
        mScrollView.post(() -> {
            ViewTreeObserver observer = mScrollView.getViewTreeObserver();
            observer.addOnScrollChangedListener(() -> {
                if (mScrollView.getScrollY() >= mEventImageView.getHeight()) {
                    animateAppearToolbar();
                } else {
                    animateDisappearToolbar();
                }
                if(mScrollView.getScrollY() > 0) {
                    animateFabDown();
                } else {
                    animateFabUp();
                }
                paintMask(mScrollView.getScrollY());
            });
        });
    }

    private void animateAppearToolbar(){
        mToolbar.setBackgroundColor(mColor);
        if (mTitleDisappearAnimation != null && mTitleDisappearAnimation.isRunning())
            mTitleDisappearAnimation.cancel();
        if (mTitleAppearAnimation == null || !mTitleAppearAnimation.isRunning()) {
            mTitleAppearAnimation = ObjectAnimator.ofFloat(mToolbarTitle, "alpha",
                    mToolbarTitle.getAlpha(), 1f);
            mTitleAppearAnimation.setDuration(200);
            mTitleAppearAnimation.start();
            if (Build.VERSION.SDK_INT >= 21) {
                float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                        getResources().getDisplayMetrics());
                mAppBarLayout.setElevation(px);
            }
        }
    }
    private void animateDisappearToolbar(){
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
        if (mTitleAppearAnimation != null && mTitleAppearAnimation.isRunning())
            mTitleAppearAnimation.cancel();
        if (mTitleDisappearAnimation == null || !mTitleDisappearAnimation.isRunning()) {
            mTitleDisappearAnimation = ObjectAnimator.ofFloat(mToolbarTitle, "alpha",
                    mToolbarTitle.getAlpha(), 0f);
            mTitleDisappearAnimation.setDuration(200);
            mTitleDisappearAnimation.start();
            if (Build.VERSION.SDK_INT >= 21)
                mAppBarLayout.setElevation(0);
        }
    }

    private void animateFabUp(){
        if(mFabDownAnimation != null && mFabDownAnimation.isRunning())
            mFabDownAnimation.cancel();
        mFabUpAnimation = ObjectAnimator.ofFloat(mFAB, "y", mFAB.getY(), mFabHeight);
        mFabUpAnimation.setDuration(200);
        mFabUpAnimation.start();
        isFabDown = false;
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)mFAB.getLayoutParams();
        layoutParams.setBehavior(null);
        mFAB.setLayoutParams(layoutParams);
    }
    private void animateFabDown(){
        if(isFabDown)
            return;
        if(mFabUpAnimation != null && mFabUpAnimation.isRunning())
            mFabUpAnimation.cancel();
        mFabUpAnimation = ObjectAnimator.ofFloat(mFAB, "translationY", mFAB.getTranslationY(), 0);
        mFabUpAnimation.setDuration(200);
        mFabUpAnimation.start();
        isFabDown = true;
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams)mFAB.getLayoutParams();
        layoutParams.setBehavior(new FloatingActionButton.Behavior(getActivity(), null));
        mFAB.setLayoutParams(layoutParams);
    }

    private void paintMask(float scrolled){
        int height = mEventImageView.getHeight();
        int maskColor = Color.argb(
                (int)((scrolled / height) * 255),
                Color.red(mColor), Color.green(mColor), Color.blue(mColor));
        mEventImageMask.setBackgroundColor(maskColor);
        mEventOrganizationMask.setBackgroundColor(maskColor);
    }

    private void initDrawer(){
        mDrawer = DrawerWrapper.newInstance(getActivity());
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(getActivity(), mDrawer.getDrawer()));
    }

    @Override
    public void onStart() {
        super.onStart();
        loadEvent();
        mDrawer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(alertDialog != null)
            alertDialog.dismiss();
    }

    private void initUserFavoriteCard(){
        mUserFavoritedCard.setOnAllButtonListener((View v) -> {
            if(mAdapter.getEvent() == null)
                return;
            Intent intent = new Intent(getContext(), UserListActivity.class);
            intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                        .appendPath(String.valueOf(mAdapter.getEvent().getEntryId())).build());
            intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.event.nativeInt);
            if(Build.VERSION.SDK_INT >= 21){
                getActivity().startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            }
            else
                getActivity().startActivity(intent);
        });
    }

    public void loadEvent(){
        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<EventDetail>> eventObservable =
                apiService.getEvent(EvendateAccountManager.peekToken(getActivity()),
                        eventId, EventDetail.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if(result.isOk())
                        onLoaded(result.getData());
                    else
                        onError();
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "completed"));
    }

    private class EventAdapter {
        private EventDetail mEvent;

        public void setEvent(EventDetail event) {
            mEvent = event;
        }

        public EventDetail getEvent() {
            return mEvent;
        }

        private void setEventInfo() {
            //prevent illegal state exception cause fragment not attached to
            if (!isAdded())
                return;
            mOrganizationTextView.setText(mEvent.getOrganizationName());
            mDescriptionTextView.setText(mEvent.getDescription());
            mTitleTextView.setText(mEvent.getTitle());
            mPlacePlaceTextView.setText(mEvent.getLocation());
            mTagsView.setTags(mEvent.getTagList());
            final Target target = new Target() {
                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }

                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    if(bitmap == null)
                        return;
                    mEventImageView.setImageBitmap(bitmap);
                    palette(bitmap);
                    revealView(mEventImageContainer);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {}
            };
            Picasso.with(getContext())
                    .load(mEvent.getImageHorizontalUrl())
                    .error(R.drawable.default_background)
                    .noFade()
                    .into(target);
            Target target2 = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    mOrganizationIconView.setImageBitmap(bitmap);
                    revealView(mOrganizationIconContainer);
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {}

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {}
            };
            Picasso.with(getContext())
                    .load(mEvent.getOrganizationLogoUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(target2);
            mUserFavoritedCard.setTitle(UsersFormatter.formatUsers(getContext(), mEvent.getUserList()));
            if (mEvent.getUserList().size() == 0) {
                mUserFavoritedCard.setVisibility(View.GONE);
            }
            mToolbarTitle.setText(mEvent.getTitle());
            setFabIcon();
            mUserFavoritedCard.setUsers(mEvent.getUserList());
            setDates();

            mPriceTextView.setText(mEvent.isFree() ? eventFreeLabel :
                    EventFormatter.formatPrice(getContext(), mEvent.getMinPrice()));
            mRegistrationTextView.setText(!mEvent.isRegistrationRequired() ? eventRegistrationNotRequiredLabel :
                    eventRegistrationTillLabel + " " + EventFormatter.formatRegistrationDate(mEvent.getRegistrationTill()));
        }

        private void setDates(){
            if (mEvent.isSameTime()) {
                mDatesLightView.setVisibility(View.VISIBLE);
                mEventTimeTextView.setText(EventFormatter.formatEventTime(getContext(), mEvent.getDateList().get(0)));
                mEventDateIntervalsTextView.setText(EventFormatter.formatDate(mEvent));
            } else {
                mDatesView.setVisibility(View.VISIBLE);
                mDatesView.setDates(mEvent.getDateList());
            }

        }
    }

    @SuppressWarnings("deprecation")
    private void setFabIcon() {
        if(!mFAB.isShown())
            mFAB.show();
        if (mAdapter.getEvent().isFavorite()) {
            mFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.accent)));
            mFAB.setImageDrawable(getResources().getDrawable(R.drawable.ic_grade_white_48dp));
        } else {
            mFAB.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            Drawable drawable = getResources().getDrawable(R.drawable.ic_star_contur);
            mFAB.setImageDrawable(drawable);
        }
    }

    private void revealView(View view){
        if(!isAdded())
            return;
        if(view.getVisibility() == View.VISIBLE)
            return;
        view.setVisibility(View.VISIBLE);
        if(Build.VERSION.SDK_INT < 21)
            return;
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator animation = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

        animation.start();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_organization_container)
    public void onOrganizationClick(View v) {
        if (mAdapter.getEvent() == null)
            return;
        Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
        intent.setData(EvendateContract.OrganizationEntry.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(mAdapter.getEvent().getOrganizationId())).build());
        if(Build.VERSION.SDK_INT >= 21){
            getActivity().startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        }
        else
            getActivity().startActivity(intent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_place_button)
    public void onPlaceClick(){
        Statistics.init(getActivity());
        Statistics.sendEventOpenMap(eventId);
        Uri gmmIntentUri = Uri.parse("geo:" + mAdapter.getEvent().getLatitude() +
                "," + mAdapter.getEvent().getLongitude() + "?q=" + mAdapter.mEvent.getLocation());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_link_card)
    public void onLinkClick(){
        Statistics.init(getActivity());
        Statistics.sendEventOpenSite(eventId);
        if(mAdapter.getEvent() == null)
            return;
        Intent openLink = new Intent(Intent.ACTION_VIEW);
        openLink.setData(Uri.parse(mAdapter.getEvent().getDetailInfoUrl()));
        startActivity(openLink);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab)
    public void onFabClick(){
        if(mAdapter.getEvent() == null)
            return;
        Tracker tracker = EvendateApplication.getTracker();
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(getActivity().getString(R.string.stat_category_event))
                .setLabel((Long.toString(mAdapter.getEvent().getEntryId())));

        LikeEventLoader likeEventLoader = new LikeEventLoader(getActivity(), mAdapter.getEvent(),
                mAdapter.getEvent().isFavorite());
        likeEventLoader.setLoaderListener(new LoaderListener<ArrayList<Void>>() {
            @Override
            public void onLoaded(ArrayList<Void> subList) {

            }

            @Override
            public void onError() {
                Toast.makeText(getActivity(), R.string.download_error, Toast.LENGTH_SHORT).show();
            }
        });
        likeEventLoader.startLoading();
        mAdapter.getEvent().favore();
        if (mAdapter.getEvent().isFavorite()) {
            event.setAction(getActivity().getString(R.string.stat_action_like));
            Toast.makeText(getActivity(), R.string.favorite_confirm, Toast.LENGTH_SHORT).show();
        } else {
            event.setAction(getActivity().getString(R.string.stat_action_dislike));
            Toast.makeText(getActivity(), R.string.remove_favorite_confirm, Toast.LENGTH_SHORT).show();
        }
        tracker.send(event.build());
        mAdapter.setEventInfo();
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
                        mAdapter.getEvent().getLink());
                shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(mEventImageView));
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, getActivity().getString(R.string.action_share)));
                return true;
            case android.R.id.home:
                onUpPressed();
                return true;
            case R.id.action_add_notification:
                loadNotifications();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //TODO DRY
    private void onUpPressed(){
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if(taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getActivity().getClass().getName())) {
            Log.i(LOG_TAG, "This is last activity in the stack");
            getActivity().startActivity(NavUtils.getParentActivityIntent(getActivity()));
        }
        else{
            getActivity().onBackPressed();
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
        if (drawable instanceof BitmapDrawable) {
            bmp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
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
    public void onLoaded(ArrayList<EventDetail> events) {
        if (!isAdded())
            return;
        EventDetail event = events.get(0);
        mAdapter.setEvent(event);
        mAdapter.setEventInfo();
        mProgressBar.setVisibility(View.GONE);
        if(Build.VERSION.SDK_INT > 19)
            TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        mScrollView.setVisibility(View.VISIBLE);
        mTitleTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError() {
        if (!isAdded())
            return;
        mProgressBar.setVisibility(View.GONE);
        AlertDialog alertDialog = ErrorAlertDialogBuilder.newInstance(getActivity(),
                (DialogInterface dialog, int which) -> {
                loadEvent();
                mProgressBar.setVisibility(View.VISIBLE);
                dialog.dismiss();
        });
        alertDialog.show();
    }

    public void palette(Bitmap bitmap) {
        if (bitmap == null)
            return;
        Palette palette = Palette.from(bitmap).generate();
        mColor = palette.getDarkMutedColor(getResources().getColor(R.color.primary));
        int red = (int)(Color.red(mColor) * 0.8);
        int blue = (int)(Color.blue(mColor) * 0.8);
        int green = (int)(Color.green(mColor) * 0.8);

        int vibrantDark = Color.argb(255, red, green, blue);
        int vibrantDarkEnd = Color.argb(50, red, green, blue);

        mOrganizationContainer.setBackgroundColor(mColor);
        GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
                new int[]{vibrantDark, vibrantDarkEnd});
        mEventForegroundImage.setImageDrawable(shadow);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(vibrantDark);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawer.cancel();
    }


    public void loadNotifications() {
        EventNotificationsLoader mEventNotificationsLoader = new EventNotificationsLoader(getActivity(), eventId);
        mEventNotificationsLoader.setLoaderListener(new LoaderListener<ArrayList<EventNotification>>() {
            @Override
            public void onLoaded(ArrayList<EventNotification> subList) {
                initDialog(subList);
            }

            @Override
            public void onError() {

            }
        });
        mEventNotificationsLoader.startLoading();
    }

    public void initDialog(ArrayList<EventNotification> notifications) {
        NotificationListAdapter adapter;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.NotificationDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_multichoice, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle(getString(R.string.action_add_notification));

        ListView lv = (ListView) convertView.findViewById(R.id.listView);
        adapter = new NotificationListAdapter(getActivity(),
                NotificationConverter.convertNotificationList(notifications), eventId);
        lv.setAdapter(adapter);
        alertDialog.setPositiveButton("Ok", (DialogInterface d, int which) -> {
            adapter.update();
        });
        alertDialog.setNegativeButton("Cancel", (DialogInterface d, int which) -> {
            notificationDialog.dismiss();
        });

        Button addNotificationButton = (Button)convertView.findViewById(R.id.add_notification);
        addNotificationButton.setOnClickListener((View view) -> {
            DialogFragment newFragment = DatePickerFragment.getInstance(getActivity(), eventId);
            newFragment.show(getChildFragmentManager(), "datePicker");
            adapter.update();
            notificationDialog.dismiss();
        });
        notificationDialog = alertDialog.show();
    }

    public static class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
        Calendar calendar = Calendar.getInstance();
        int eventId;
        Context context;

        public static DatePickerFragment getInstance(Context context, int eventId) {
            DatePickerFragment fragment = new DatePickerFragment();
            fragment.eventId = eventId;
            fragment.context = context;
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            DatePickerDialog pickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            pickerDialog.getDatePicker().setMinDate(c.getTimeInMillis());
            return  pickerDialog;
        }

        public void onDateSet(DatePicker view, final int year, final int month, final int day) {
            calendar.set(year, month, day);
            TimePickerDialog newFragment2 = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendar.set(year, month, day, hourOfDay, minute, 0);
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    ApiService apiService = ApiFactory.getEvendateService();
                    Observable<Response> notificationObservervable =
                            apiService.setNotificationByTime(EvendateAccountManager.peekToken(context), eventId, df.format(new Date(calendar.getTimeInMillis())));

                    notificationObservervable.subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(result -> {
                                Log.i(LOG_TAG, "loaded");
                                if(result.isOk())
                                    //todo update notification list cause new list in return answer
                                    Toast.makeText(context, R.string.custom_notification_added, Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(context, R.string.custom_notification_error, Toast.LENGTH_SHORT).show();
                            }, error -> {
                                Toast.makeText(context, R.string.custom_notification_error, Toast.LENGTH_SHORT).show();
                                Log.e(LOG_TAG, error.getMessage());
                            }, () -> Log.i(LOG_TAG, "completed"));
                }
            }, 0, 0, true);
            newFragment2.show();
        }
    }
}