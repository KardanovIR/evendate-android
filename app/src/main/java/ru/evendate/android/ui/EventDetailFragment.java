package ru.evendate.android.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
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
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

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
import ru.evendate.android.R;
import ru.evendate.android.adapters.NotificationConverter;
import ru.evendate.android.adapters.NotificationListAdapter;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.models.UsersFormatter;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.views.DatesView;
import ru.evendate.android.views.TagsView;
import ru.evendate.android.views.UserFavoritedCard;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * contain details of events
 */
public class EventDetailFragment extends Fragment implements TagsView.OnTagClickListener {
    private static String LOG_TAG = EventDetailFragment.class.getSimpleName();

    private EventDetailActivity mEventDetailActivity;

    private Uri mUri;
    private int eventId;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    private EventAdapter mAdapter;

    @Bind(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.scroll_view) ScrollView mScrollView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.event_image_mask) View mEventImageMask;
    @Bind(R.id.event_title_mask) View mEventTitleMask;
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
    @Bind(R.id.event_title_container) View mTitleContainer;
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

    final Target eventTarget = new Target() {
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (!isAdded())
                return;
            if (bitmap == null)
                return;
            mEventImageView.setImageBitmap(bitmap);
            palette(bitmap);
            revealView(mEventImageContainer);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}
    };
    final Target orgTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.d(LOG_TAG, "onBitmapLoaded");
            mOrganizationIconView.setImageBitmap(bitmap);
            revealView(mOrganizationIconContainer);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Log.d(LOG_TAG, "onBitmapFailed");
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            Log.d(LOG_TAG, "onPrepareLoad");
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mEventDetailActivity = (EventDetailActivity)getActivity();
        mUri = mEventDetailActivity.mUri;
        eventId = Integer.parseInt(mUri.getLastPathSegment());
        new Statistics(getActivity()).sendEventView(eventId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        initToolbar();
        initTransitions();
        initFabY();
        initToolbarAndFabAnimation();
        initUserFavoriteCard();
        initDrawer();
        mTagsView.setOnTagClickListener(this);
        mAdapter = new EventAdapter();

        mColor = ContextCompat.getColor(getActivity(), R.color.primary);

        mFAB.hide();
        mEventImageContainer.setVisibility(View.INVISIBLE);
        mOrganizationIconContainer.setVisibility(View.INVISIBLE);
        mTitleContainer.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mEventContentContainer.setVisibility(View.GONE);
        return rootView;
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        mToolbar.setTitle("");
        mEventDetailActivity.setSupportActionBar(mToolbar);
        mEventDetailActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    }

    private void initTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
            getActivity().getWindow().setExitTransition(new Slide(Gravity.TOP));
        }
    }

    private void initFabY() {
        ViewTreeObserver vto = mCoordinatorLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCoordinatorLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int bottom = mTitleContainer.getBottom();
                int size = mFAB.getHeight();
                mFabHeight = bottom - size / 2;
                if (Build.VERSION.SDK_INT > 19)
                    TransitionManager.beginDelayedTransition(mCoordinatorLayout);
                mFAB.setY(mFabHeight);
            }
        });
    }

    private void initToolbarAndFabAnimation() {
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
                if (mScrollView.getScrollY() > 0) {
                    animateFabDown();
                } else {
                    animateFabUp();
                }
                paintMask(mScrollView.getScrollY());
            });
        });
    }

    private void animateAppearToolbar() {
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

    private void animateDisappearToolbar() {
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

    private void animateFabUp() {
        if (mFabDownAnimation != null && mFabDownAnimation.isRunning())
            mFabDownAnimation.cancel();
        mFabUpAnimation = ObjectAnimator.ofFloat(mFAB, "y", mFAB.getY(), mFabHeight);
        mFabUpAnimation.setDuration(200);
        mFabUpAnimation.start();
        isFabDown = false;
    }

    private void animateFabDown() {
        if (isFabDown)
            return;
        if (mFabUpAnimation != null && mFabUpAnimation.isRunning())
            mFabUpAnimation.cancel();
        mFabUpAnimation = ObjectAnimator.ofFloat(mFAB, "translationY", mFAB.getTranslationY(), 0);
        mFabUpAnimation.setDuration(200);
        mFabUpAnimation.start();
        isFabDown = true;
    }

    private void paintMask(float scrolled) {
        int height = mEventImageView.getHeight();
        int maskColor = Color.argb(
                (int)((scrolled / height) * 255),
                Color.red(mColor), Color.green(mColor), Color.blue(mColor));
        mEventImageMask.setBackgroundColor(maskColor);
        mEventTitleMask.setBackgroundColor(maskColor);
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(getActivity());
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(getActivity(), mDrawer.getDrawer()));
    }

    private void initUserFavoriteCard() {
        mUserFavoritedCard.setOnAllButtonListener((View v) -> {
            if (mAdapter.getEvent() == null)
                return;
            Intent intent = new Intent(getContext(), UserListActivity.class);
            intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(mAdapter.getEvent().getEntryId())).build());
            intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.EVENT.type());
            if (Build.VERSION.SDK_INT >= 21) {
                getActivity().startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            } else
                getActivity().startActivity(intent);
        });
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
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.event_share_text_i_like) +
                        mAdapter.getEvent().getTitle() + getString(R.string.event_share_text_in_org) +
                        mAdapter.getEvent().getOrganizationName() + "\n" +
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadEvent();
        mDrawer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    public void loadEvent() {
        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<ResponseArray<EventDetail>> eventObservable =
                apiService.getEvent(EvendateAccountManager.peekToken(getActivity()),
                        eventId, EventDetail.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk())
                        onLoaded(result.getData());
                    else
                        onError();
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, error.getMessage());
                });
    }

    public void onLoaded(ArrayList<EventDetail> events) {
        if (!isAdded())
            return;
        EventDetail event = events.get(0);
        mAdapter.setEvent(event);
        mAdapter.setEventInfo();
        mProgressBar.setVisibility(View.GONE);
        // cause W/OpenGLRenderer﹕ Layer exceeds max. dimensions supported by the GPU (1080x5856, max=4096x4096)
        //if (Build.VERSION.SDK_INT > 19)
        //    TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        mEventContentContainer.setVisibility(View.VISIBLE);
        mTitleContainer.setVisibility(View.VISIBLE);
    }

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
            setAdaptiveTitle();
            mPlacePlaceTextView.setText(mEvent.getLocation());
            mTagsView.setTags(mEvent.getTagList());
            Picasso.with(getContext())
                    .load(mEvent.getImageHorizontalUrl())
                    .error(R.drawable.default_background)
                    .noFade()
                    .into(eventTarget);
            Picasso.with(getContext())
                    .load(mEvent.getOrganizationLogoUrl())
                    .error(R.mipmap.ic_launcher)
                    .noFade()
                    .into(orgTarget);
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

        private void setAdaptiveTitle() {
            String title = mEvent.getTitle();
            if (title.length() > 24)
                mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            if (title.length() > 64)
                mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            mTitleTextView.setText(mEvent.getTitle());
        }

        private void setDates() {
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

    private void setFabIcon() {
        if (!mFAB.isShown())
            mFAB.show();
        if (mAdapter.getEvent().isFavorite()) {
            mFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.accent)));
            mFAB.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_star));
        } else {
            mFAB.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.ic_star_border);
            mFAB.setImageDrawable(drawable);
        }
    }

    private void revealView(View view) {
        if (!isAdded())
            return;
        if (view.getVisibility() == View.VISIBLE)
            return;
        view.setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT < 21)
            return;
        int cx = (view.getLeft() + view.getRight()) / 2;
        int cy = (view.getTop() + view.getBottom()) / 2;

        int finalRadius = Math.max(view.getWidth(), view.getHeight());
        Animator animation = ViewAnimationUtils.createCircularReveal(view, cx, cy, 0, finalRadius);

        animation.start();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_org_card)
    public void onOrganizationClick(View v) {
        if (mAdapter.getEvent() == null)
            return;
        Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
        intent.setData(EvendateContract.OrganizationEntry.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(mAdapter.getEvent().getOrganizationId())).build());
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
        } else
            getActivity().startActivity(intent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_place_button)
    public void onPlaceClick() {
        new Statistics(getActivity()).sendEventOpenMap(eventId);
        Uri gmmIntentUri = Uri.parse("geo:" + mAdapter.getEvent().getLatitude() +
                "," + mAdapter.getEvent().getLongitude() + "?q=" + mAdapter.mEvent.getLocation());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_link_card)
    public void onLinkClick() {
        new Statistics(getActivity()).sendEventClickLinkAction(eventId);
        if (mAdapter.getEvent() == null)
            return;
        Intent openLink = new Intent(Intent.ACTION_VIEW);
        openLink.setData(Uri.parse(mAdapter.getEvent().getDetailInfoUrl()));
        startActivity(openLink);
    }

    @Override
    public void onTagClicked(String tag) {
        Intent searchIntent = new Intent(getActivity(), SearchResultsActivity.class);
        searchIntent.putExtra(SearchResultsActivity.SEARCH_BY_TAG, true);
        searchIntent.setAction(Intent.ACTION_SEARCH);
        searchIntent.putExtra(SearchManager.QUERY, tag);
        startActivity(searchIntent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab)
    public void onFabClick() {
        if (mAdapter.getEvent() == null)
            return;
        EventDetail event = mAdapter.getEvent();

        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<Response> LikeEventObservable;

        if (event.isFavorite()) {
            LikeEventObservable = apiService.eventDeleteFavorite(event.getEntryId(),
                    EvendateAccountManager.peekToken(getActivity()));
        } else {
            LikeEventObservable = apiService.eventPostFavorite(event.getEntryId(),
                    EvendateAccountManager.peekToken(getActivity()));
        }
        LikeEventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isOk())
                        Log.i(LOG_TAG, "performed like");
                    else
                        Log.e(LOG_TAG, "Error with response with like");
                }, error -> {
                    Log.e(LOG_TAG, error.getMessage());
                    Toast.makeText(getActivity(), R.string.download_error, Toast.LENGTH_SHORT).show();
                });

        event.favore();

        Statistics statistics = new Statistics(getActivity());
        if (event.isFavorite()) {
            statistics.sendEventFavoreAction(eventId);
            Toast.makeText(getActivity(), R.string.event_favorite_confirm, Toast.LENGTH_SHORT).show();
        } else {
            statistics.sendEventUnfavoreAction(eventId);
            Toast.makeText(getActivity(), R.string.event_favorite_remove_confirm, Toast.LENGTH_SHORT).show();
        }
        mAdapter.setEventInfo();
    }

    //TODO DRY
    private void onUpPressed() {
        ActivityManager activityManager = (ActivityManager)getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getActivity().getClass().getName())) {
            Log.i(LOG_TAG, "This is last activity in the stack");
            getActivity().startActivity(NavUtils.getParentActivityIntent(getActivity()));
        } else {
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

    public void palette(Bitmap bitmap) {
        if (!isAdded())
            return;
        if (bitmap == null)
            return;
        Palette palette = Palette.from(bitmap).generate();
        mColor = palette.getDarkMutedColor(ContextCompat.getColor(getActivity(), R.color.primary));
        int red = (int)(Color.red(mColor) * 0.8);
        int blue = (int)(Color.blue(mColor) * 0.8);
        int green = (int)(Color.green(mColor) * 0.8);

        int vibrantDark = Color.argb(255, red, green, blue);
        int colorShadow = Color.argb(150, red, green, blue);
        int colorShadowEnd = Color.argb(0, red, green, blue);

        mTitleContainer.setBackgroundColor(mColor);
        GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{colorShadow, colorShadowEnd, colorShadowEnd, colorShadowEnd});
        mEventForegroundImage.setImageDrawable(shadow);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getActivity().getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(vibrantDark);
        }
    }

    public void loadNotifications() {
        ApiService apiService = ApiFactory.getService(getActivity());

        Observable<ResponseArray<EventNotification>> eventObservable =
                apiService.getNotifications(EvendateAccountManager.peekToken(getActivity()),
                        eventId, EventNotification.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk())
                        initNotificationDialog(result.getData());
                }, error -> {
                    Log.e(LOG_TAG, "Error with response with notification");
                    Log.e(LOG_TAG, error.getMessage());
                });
    }

    public void initNotificationDialog(ArrayList<EventNotification> notifications) {
        NotificationListAdapter adapter;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View convertView = inflater.inflate(R.layout.dialog_add_notifiaction_button, null);
        alertDialog.setTitle(getString(R.string.action_add_notification));
        adapter = new NotificationListAdapter(getActivity(),
                NotificationConverter.convertNotificationList(notifications), mAdapter.getEvent());
        alertDialog.setAdapter(adapter, null);
        alertDialog.setView(convertView);
        alertDialog.setPositiveButton(R.string.dialog_ok, (DialogInterface d, int which) -> adapter.update());
        alertDialog.setNegativeButton(R.string.dialog_cancel, (DialogInterface d, int which) -> notificationDialog.dismiss());

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
            return pickerDialog;
        }

        public void onDateSet(DatePicker view, final int year, final int month, final int day) {
            calendar.set(year, month, day);
            TimePickerDialog newFragment2 = new TimePickerDialog(context, (TimePicker v, int hourOfDay, int minute) -> {
                calendar.set(year, month, day, hourOfDay, minute, 0);
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                String date = df.format(new Date(calendar.getTimeInMillis()));
                Log.d(LOG_TAG, "date: " + date);

                ApiService apiService = ApiFactory.getService(context);
                Observable<Response> notificationObservable =
                        apiService.setNotificationByTime(EvendateAccountManager.peekToken(context), eventId, date);

                notificationObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            Log.i(LOG_TAG, "loaded");
                            if (result.isOk())
                                //todo update notification list cause new list in return answer
                                Toast.makeText(context, R.string.custom_notification_added, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(context, R.string.custom_notification_error, Toast.LENGTH_SHORT).show();
                        }, error -> {
                            Toast.makeText(context, R.string.custom_notification_error, Toast.LENGTH_SHORT).show();
                            Log.e(LOG_TAG, error.getMessage());
                        });
            }, 0, 0, true);
            newFragment2.show();
        }
    }
}