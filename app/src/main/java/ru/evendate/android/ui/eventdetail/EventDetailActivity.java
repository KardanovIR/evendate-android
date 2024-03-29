package ru.evendate.android.ui.eventdetail;

import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
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
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.TransitionManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventDate;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.orgdetail.OrganizationDetailActivity;
import ru.evendate.android.ui.search.SearchResultsActivity;
import ru.evendate.android.ui.users.UserListActivity;
import ru.evendate.android.ui.users.UserListFragment;
import ru.evendate.android.ui.utils.DateFormatter;
import ru.evendate.android.ui.utils.EventFormatter;
import ru.evendate.android.ui.utils.UsersFormatter;
import ru.evendate.android.views.DatesView;
import ru.evendate.android.views.LoadStateView;
import ru.evendate.android.views.TagsRecyclerView;
import ru.evendate.android.views.UserFavoritedCard;

import static ru.evendate.android.ui.utils.UiUtils.revealView;

/**
 * contain details of events
 */
public class EventDetailActivity extends BaseActivity implements TagsRecyclerView.OnTagClickListener,
        LoadStateView.OnReloadListener, RegistrationFormFragment.OnRegistrationCallbackListener {
    private static String LOG_TAG = EventDetailActivity.class.getSimpleName();
    public static final String URI_KEY = "uri";
    @BindView(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.scroll_view) ScrollView mScrollView;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.event_image_mask) View mEventImageMask;
    @BindView(R.id.event_title_mask) View mEventTitleMask;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.event_toolbar_title) TextView mToolbarTitle;
    private ObjectAnimator mTitleAppearAnimation;
    private ObjectAnimator mTitleDisappearAnimation;
    @BindView(R.id.fab) FloatingActionButton mFAB;
    @BindView(R.id.event_image) ImageView mEventImageView;
    @BindView(R.id.event_organization_icon) ImageView mOrganizationIconView;
    @BindView(R.id.event_organization_name) TextView mOrganizationTextView;
    @BindView(R.id.event_description) TextView mDescriptionTextView;
    @BindView(R.id.event_title) TextView mTitleTextView;
    @BindView(R.id.event_place_button) View mPlaceButtonView;
    @BindView(R.id.event_place_text) TextView mPlacePlaceTextView;
    @BindView(R.id.event_link_card) View mLinkCard;
    @BindView(R.id.event_title_container) View mTitleContainer;
    @BindView(R.id.event_image_foreground) ImageView mEventForegroundImage;
    @BindView(R.id.tag_layout) TagsRecyclerView mTagsView;
    @BindView(R.id.event_registration_card) CardView mPriceCard;
    @BindView(R.id.event_price) TextView mPriceTextView;
    @BindView(R.id.event_registration) TextView mRegistrationTillTextView;
    @BindView(R.id.event_registration_button) Button mRegistrationButton;
    @BindView(R.id.event_registration_cap) TextView mRegistrationCap;
    @BindView(R.id.event_dates) DatesView mDatesView;
    @BindView(R.id.event_dates_light) CardView mDatesLightView;
    @BindView(R.id.event_dates_intervals) TextView mEventDateIntervalsTextView;
    @BindView(R.id.event_time) TextView mEventTimeTextView;
    @BindView(R.id.event_content_container) View mEventContentContainer;
    @BindView(R.id.event_image_container) View mEventImageContainer;
    @BindView(R.id.user_card) UserFavoritedCard mUserFavoritedCard;
    @BindString(R.string.event_free) String eventFreeLabel;
    @BindString(R.string.event_registration_not_required) String eventRegistrationNotRequiredLabel;
    @BindString(R.string.event_registration_till) String eventRegistrationTillLabel;
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private int mFabHeight;
    private boolean isFabDown = false;
    private ObjectAnimator mFabUpAnimation;
    private ObjectAnimator mFabDownAnimation;
    private Uri mUri;
    private int eventId;
    private EventAdapter mAdapter;
    private int mPalleteColor;
    private AlertDialog notificationDialog;
    private RegistrationFormFragment mTicketingFragment;

    private boolean backgroundLoaded = false;

    private final Target eventTarget = new Target() {
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (bitmap == null) {
                mEventImageView.setImageDrawable(ContextCompat.getDrawable(getBaseContext(),
                        R.drawable.default_background));
            } else {
                mEventImageView.setImageBitmap(bitmap);
                //palette(bitmap);
            }
            revealView(mEventImageContainer);
            backgroundLoaded = true;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            mEventImageView.setImageDrawable(errorDrawable);
            revealView(mEventImageContainer);
        }
    };
    private final Target eventMiniTarget = new Target() {
        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {}

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            if (bitmap == null || backgroundLoaded)
                return;
            mEventImageView.setImageBitmap(bitmap);
            revealView(mEventImageContainer);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {}
    };
    private final Target orgTarget = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            Log.d(LOG_TAG, "onBitmapLoaded");
            mOrganizationIconView.setImageBitmap(bitmap);
            revealView(mOrganizationIconView);
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
        if (Build.VERSION.SDK_INT > 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent == null)
            throw new RuntimeException("no intent with uri");
        mUri = intent.getData();

        eventId = Integer.parseInt(mUri.getLastPathSegment());
        new Statistics(this).sendEventView(eventId);

        initInterface();

        loadEvent();
        mLoadStateView.showProgress();
    }

    private void initInterface() {
        ButterKnife.bind(this);
        initToolbar();
        initTransitions();
        initFabY();
        initToolbarAndFabAnimation();
        initUserFavoriteCard();
        initDrawer();
        mTagsView.setOnTagClickListener(this);
        mAdapter = new EventAdapter(this);

        mPalleteColor = ContextCompat.getColor(this, R.color.primary);

        mFAB.hide();
        mEventImageContainer.setVisibility(View.INVISIBLE);
        mOrganizationIconView.setVisibility(View.INVISIBLE);
        mTitleContainer.setVisibility(View.INVISIBLE);
        mLoadStateView.setOnReloadListener(this);
        mEventContentContainer.setVisibility(View.GONE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(URI_KEY, mUri.toString());
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
    }

    private void initTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setEnterTransition(new Slide(Gravity.BOTTOM));
            getWindow().setExitTransition(new Slide(Gravity.TOP));
        }
    }

    private void initFabY() {
        ViewTreeObserver vto = mCoordinatorLayout.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mCoordinatorLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int bottom = mTitleContainer.getBottom();
                //int size = mFAB.getHeight();
                int size = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 56,
                        getResources().getDisplayMetrics());
                mFabHeight = bottom - size / 2;
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
        mToolbar.setBackgroundColor(mPalleteColor);
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
                Color.red(mPalleteColor), Color.green(mPalleteColor), Color.blue(mPalleteColor));
        mEventImageMask.setBackgroundColor(maskColor);
        mEventTitleMask.setBackgroundColor(maskColor);
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new DrawerWrapper.NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    private void initUserFavoriteCard() {
        mUserFavoritedCard.setOnAllButtonListener((View v) -> {
            if (mAdapter.getEvent() == null)
                return;
            Intent intent = new Intent(this, UserListActivity.class);
            intent.setData(EvendateContract.EventEntry.getContentUri(mAdapter.getEvent().getEntryId()));
            intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.EVENT.type());
            if (Build.VERSION.SDK_INT >= 21) {
                this.startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
            } else
                this.startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.event_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.event_share_text_i_like) +
                        mAdapter.getEvent().getTitle() + getString(R.string.event_share_text_in_org) + " " +
                        mAdapter.getEvent().getOrganizationName() + "\n" +
                        mAdapter.getEvent().getLink());
                shareIntent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(mEventImageView));
                shareIntent.setType("image/*");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(shareIntent, this.getString(R.string.action_share)));
                return true;
            case android.R.id.home:
                onUpPressed();
                return true;
            case R.id.action_add_notification:
                String token = EvendateAccountManager.peekToken(this);
                if (token == null) {
                    requestAuth().subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread()).subscribe(
                            (String newToken) -> loadNotifications());
                } else {
                    loadNotifications();
                }
                return true;
            //case R.id.action_export:
            //    exportEventToCalendar();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Picasso.with(this).cancelRequest(mEventImageView);
        Picasso.with(this).cancelRequest(mOrganizationIconView);
    }

    @Override
    public void onBackPressed() {
        if (mTicketingFragment != null && mTicketingFragment.isResumed()) {
            mTicketingFragment.onBackPressed();
        }
        super.onBackPressed();
        mFAB.show();
    }

    @Override
    public void onReload() {
        super.onReload();
        loadEvent();
    }

    private void loadEvent() {
        new DataRepository(this)
                .getEvent(EvendateAccountManager.peekToken(this), eventId)
                .subscribe(result -> {
                            if (result.isOk())
                                onLoaded(result.getData());
                            else
                                mLoadStateView.showErrorHint();
                        }, this::onError,
                        mLoadStateView::hideProgress);
    }

    private void onLoaded(ArrayList<Event> events) {
        Event event = events.get(0);
        mAdapter.setEvent(event);
        mAdapter.setEventInfo();
        // cause W/OpenGLRenderer﹕ Layer exceeds max. dimensions supported by the GPU (1080x5856, max=4096x4096)
        //TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        mEventContentContainer.setVisibility(View.VISIBLE);
        mTitleContainer.setVisibility(View.VISIBLE);
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    public void onRegistered() {
        Toast.makeText(this, R.string.event_registration_done, Toast.LENGTH_SHORT).show();
        //mRegistrationButton.setEnabled(false);
        mRegistrationCap.setText(R.string.event_registration_status_already_registered);
    }

    @Override
    public void onPaymentCompleted() {
        Toast.makeText(this, R.string.event_ticketing_payment_done, Toast.LENGTH_SHORT).show();
        mRegistrationCap.setText(R.string.event_registration_status_already_registered);
    }

    @Override
    public void onPaymentError() {
        Toast.makeText(this, R.string.event_ticketing_payment_error, Toast.LENGTH_LONG).show();
    }

    private void setFabIcon() {
        if (!mFAB.isShown())
            mFAB.show();
        if (mAdapter.getEvent().isFavorite()) {
            mFAB.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.accent)));
            mFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_star));
        } else {
            mFAB.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_star_border);
            mFAB.setImageDrawable(drawable);
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_org_card)
    public void onOrganizationClick(View v) {
        Intent intent = new Intent(this, OrganizationDetailActivity.class);
        intent.setData(EvendateContract.OrganizationEntry.getContentUri(mAdapter.getEvent().getOrganizationId()));
        if (Build.VERSION.SDK_INT >= 21) {
            this.startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else
            this.startActivity(intent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_place_card)
    public void onPlaceClick() {
        Uri gmmIntentUri = Uri.parse("geo:" + mAdapter.getEvent().getLatitude() +
                "," + mAdapter.getEvent().getLongitude() + "?q=" + mAdapter.mEvent.getLocation());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        PackageManager manager = getPackageManager();
        List<ResolveInfo> info = manager.queryIntentActivities(mapIntent, 0);
        if (info.size() > 0) {
            new Statistics(this).sendEventOpenMap(eventId);
            startActivity(mapIntent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.alert_maps_not_available);
            builder.setPositiveButton(R.string.alert_install_google_maps, (DialogInterface dialogInterface, int i) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.android.apps.maps"));
                startActivity(intent);
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_link_card)
    public void onLinkClick() {
        new Statistics(this).sendEventClickLinkAction(eventId);
        openSite();
    }

    @Override
    public void onTagClicked(String tag) {
        Intent searchIntent = new Intent(this, SearchResultsActivity.class);
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

        String token = EvendateAccountManager.peekToken(this);
        if (token == null) {
            requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(this::like);
        } else {
            like(token);
        }
    }

    private void like(@NonNull String token) {
        Event event = mAdapter.getEvent();
        DataSource dataSource = new DataRepository(this);
        Observable<Response> LikeEventObservable;

        if (event.isFavorite()) {
            LikeEventObservable = dataSource.unfaveEvent(token, event.getEntryId());
        } else {
            LikeEventObservable = dataSource.faveEvent(token, event.getEntryId());
        }
        LikeEventObservable.subscribe(result -> {
            if (result.isOk())
                Log.i(LOG_TAG, "performed like");
            else
                Log.e(LOG_TAG, "Error with response with like");
        }, error -> {
            Log.e(LOG_TAG, "" + error.getMessage());
            Toast.makeText(this, R.string.download_error, Toast.LENGTH_SHORT).show();
        });

        event.favore();

        Statistics statistics = new Statistics(this);
        if (event.isFavorite()) {
            statistics.sendEventFavoreAction(eventId);
            Toast.makeText(this, R.string.event_favorite_confirm, Toast.LENGTH_SHORT).show();
        } else {
            statistics.sendEventUnfavoreAction(eventId);
            Toast.makeText(this, R.string.event_favorite_remove_confirm, Toast.LENGTH_SHORT).show();
        }
        mAdapter.setEventInfo();
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.event_registration_card)
    public void onRegistrationButtonClick() {
        String token = EvendateAccountManager.peekToken(this);
        if (!mRegistrationButton.isEnabled())
            return;
        if (!mAdapter.mEvent.isRegistrationLocally() && !mAdapter.mEvent.isTicketingLocally() && mAdapter.mEvent.isRegistrationRequired())
            openSite();
        else {
            if (token == null) {
                requestAuth().subscribe((String newToken) -> openRegistrationForm());
            } else {
                openRegistrationForm();
            }
        }
    }

    private void openRegistrationForm() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mTicketingFragment = RegistrationFormFragment.newInstance(mAdapter.getEvent());
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.main_content, mTicketingFragment).addToBackStack(null).commit();
        mFAB.hide();
    }

    private void openSite() {
        if (mAdapter.getEvent().getDetailInfoUrl() == null)
            return;
        Intent openLink = new Intent(Intent.ACTION_VIEW);
        openLink.setData(Uri.parse(mAdapter.getEvent().getDetailInfoUrl()));
        startActivity(openLink);
    }

    /**
     * Returns the URI_KEY path to the Bitmap displayed in specified ImageView
     * https://github.com/codepath/android_guides/wiki/Sharing-Content-with-Intents
     */
    private Uri getLocalBitmapUri(ImageView imageView) {
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
            File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
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
        if (bitmap == null)
            return;
        Palette palette = Palette.from(bitmap).generate();
        mPalleteColor = palette.getDarkMutedColor(ContextCompat.getColor(this, R.color.primary));
        int red = (int)(Color.red(mPalleteColor) * 0.8);
        int blue = (int)(Color.blue(mPalleteColor) * 0.8);
        int green = (int)(Color.green(mPalleteColor) * 0.8);

        int vibrantDark = Color.argb(255, red, green, blue);
        int colorShadow = Color.argb(150, red, green, blue);
        int colorShadowEnd = Color.argb(0, red, green, blue);

        mTitleContainer.setBackgroundColor(mPalleteColor);
        GradientDrawable shadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{colorShadow, colorShadowEnd, colorShadowEnd, colorShadowEnd});
        mEventForegroundImage.setImageDrawable(shadow);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = this.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(vibrantDark);
        }
    }

    public void exportEventToCalendar() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        intent.setType("vnd.android.cursor.item/event");
        intent.putExtra(CalendarContract.Events.TITLE, mAdapter.getEvent().getTitle());
        intent.putExtra(CalendarContract.Events.DESCRIPTION, mAdapter.getEvent().getDescription());
        intent.putExtra(CalendarContract.Events.EVENT_LOCATION, mAdapter.getEvent().getLocation());
        intent.putExtra(CalendarContract.Events.RDATE, constructTime(mAdapter.getEvent()));
        startActivity(intent);
    }

    private String constructTime(Event event) {
        String dateString = "RDATE;VALUE=PERIOD:";
        boolean firstAdded = false;
        StringBuilder builder = new StringBuilder(dateString);
        for (EventDate date : event.getDateList()) {
            if (firstAdded) {
                builder.append(",");
            }
            builder.append(DateFormatter.formatExportCalendarDateTime(date.getStartDateTime(), date.getEndDateTime()));
            firstAdded = true;
        }
        return builder.toString();
    }

    //todo SOLID
    //todo user action not handle immediate on bad network condition
    private void loadNotifications() {
        ApiService apiService = ApiFactory.getService(this);

        Observable<ResponseArray<EventNotification>> eventObservable =
                apiService.getNotifications(EvendateAccountManager.peekToken(this),
                        eventId, EventNotification.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk())
                        initNotificationDialog(result.getData());
                }, error -> {
                    Log.e(LOG_TAG, "Error with response with notification");
                    Log.e(LOG_TAG, "" + error.getMessage());
                });
    }

    private void initNotificationDialog(ArrayList<EventNotification> notifications) {
        NotificationListAdapter adapter;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getString(R.string.action_add_notification));
        adapter = new NotificationListAdapter(this,
                NotificationConverter.convertNotificationList(notifications), mAdapter.getEvent());
        alertDialog.setAdapter(adapter, null);
        alertDialog.setPositiveButton(R.string.dialog_ok, (DialogInterface d, int which) -> adapter.update());
        alertDialog.setNegativeButton(R.string.dialog_cancel, (DialogInterface d, int which) -> notificationDialog.dismiss());
        alertDialog.setNeutralButton(R.string.notification_add_custom, (DialogInterface d, int which) -> {
            DialogFragment newFragment = DatePickerFragment.getInstance(this, eventId);
            newFragment.show(getSupportFragmentManager(), "datePicker");
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

                String date = ServiceUtils.formatDateTimeRequest(new Date(calendar.getTimeInMillis()));

                ApiService apiService = ApiFactory.getService(context);
                Observable<Response> notificationObservable =
                        apiService.setNotificationByTime(EvendateAccountManager.peekToken(context), eventId, date);

                notificationObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> {
                            Log.i(LOG_TAG, "loaded");
                            if (result.isOk())
                                //todo update notification list cause new list in return answer
                                Toast.makeText(context, R.string.event_notification_added, Toast.LENGTH_SHORT).show();
                            else
                                Toast.makeText(context, R.string.event_notification_error, Toast.LENGTH_SHORT).show();
                        }, error -> {
                            Toast.makeText(context, R.string.event_notification_error, Toast.LENGTH_SHORT).show();
                            Log.e(LOG_TAG, error.getMessage());
                        });
            }, 0, 0, true);
            newFragment2.show();
        }
    }

    private class EventAdapter {
        private Context mContext;
        private Event mEvent;

        EventAdapter(Context context) {
            this.mContext = context;
        }

        public Event getEvent() {
            return mEvent;
        }

        public void setEvent(Event event) {
            mEvent = event;
        }

        private void setEventInfo() {
            mOrganizationTextView.setText(mEvent.getOrganizationName());
            mDescriptionTextView.setText(mEvent.getDescription());
            mTitleTextView.setText(mEvent.getTitle());
            mPlacePlaceTextView.setText(mEvent.getLocation());
            mTagsView.setTags(mEvent.getTagList());
            String eventBackgroundUrl = ServiceUtils.constructEventBackgroundURL(
                    mEvent.getImageHorizontalUrl(),
                    (int)mContext.getResources().getDimension(R.dimen.event_background_width));
            Picasso.with(mContext)
                    .load(mEvent.getImageHorizontalMediumUrl())
                    .noFade()
                    .into(eventMiniTarget);
            Picasso.with(mContext)
                    .load(eventBackgroundUrl)
                    .error(R.drawable.default_background)
                    .noFade()
                    .into(eventTarget);
            Picasso.with(mContext)
                    .load(mEvent.getOrganizationLogoUrl())
                    .error(R.mipmap.ic_launcher)
                    .noFade()
                    .into(orgTarget);
            mUserFavoritedCard.setTitle(UsersFormatter.formatUsers(mContext, mEvent.getUserList()));
            if (mEvent.getUserList().size() == 0) {
                mUserFavoritedCard.setVisibility(View.GONE);
            }
            mToolbarTitle.setText(mEvent.getTitle());
            setFabIcon();
            mUserFavoritedCard.setUsers(mEvent.getUserList());
            setDates();

            mPriceTextView.setText(mEvent.isFree() ? eventFreeLabel :
                    EventFormatter.formatPrice(mContext, mEvent.getMinPrice()));
            setTicketingOrRegistration();

            if (mEvent.getDetailInfoUrl() == null) {
                mLinkCard.setVisibility(View.GONE);
            }
        }

        private void setDates() {
            if (mEvent.isSameTime()) {
                mDatesLightView.setVisibility(View.VISIBLE);
                EventDate date = mEvent.getDateList().get(0);
                mEventTimeTextView.setText(EventFormatter.formatEventTime(date.getStartDateTime(), date.getEndDateTime()));
                mEventDateIntervalsTextView.setText(EventFormatter.formatDateInterval(mEvent));
            } else {
                mDatesView.setVisibility(View.VISIBLE);
                mDatesView.setDates(mEvent.getDateList());
            }

        }

        /**
         * set Registration or Ticketing info cause very complex conditions
         */
        private void setTicketingOrRegistration() {
            if (mEvent.isTicketingLocally()) {
                mRegistrationButton.setText(getString(R.string.event_ticketing));
                if (mEvent.isTicketingAvailable()) {
                    mRegistrationButton.setEnabled(true);
                    mRegistrationCap.setVisibility(View.GONE);
                    setTicketsAlreadyBought();
                } else {
                    mRegistrationButton.setEnabled(false);
                    mRegistrationCap.setVisibility(View.VISIBLE);
                    mRegistrationCap.setText(getString(R.string.event_ticketing_status_not_available));
                }
            } else {
                if (mEvent.isRegistrationLocally()) {
                    if (mEvent.isRegistrationAvailable()) {
                        mRegistrationButton.setEnabled(true);
                        mRegistrationCap.setVisibility(View.GONE);
                        setTicketsAlreadyBought();
                    } else {
                        mRegistrationButton.setEnabled(false);
                        mRegistrationCap.setText(R.string.event_registration_status_not_available);
                        mRegistrationCap.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (mEvent.isRegistrationRequired()) {
                        mRegistrationButton.setEnabled(true);
                        mRegistrationCap.setVisibility(View.GONE);
                    } else {
                        mRegistrationTillTextView.setText(eventRegistrationNotRequiredLabel);
                        mRegistrationButton.setVisibility(View.GONE);
                        mRegistrationCap.setVisibility(View.GONE);
                        mRegistrationButton.setEnabled(false);
                    }
                }
            }
            //setRegistrationApproved();
            setRegistrationTillDate();
        }

        private void setTicketsAlreadyBought() {
            if (mEvent.getTickets() != null && !mEvent.getTickets().isEmpty()) {
                if (mEvent.isTicketingLocally()) {
                    mRegistrationCap.setText(R.string.event_ticketing_status_already_purchased);
                } else {
                    mRegistrationCap.setText(R.string.event_registration_status_already_registered);
                }
                mRegistrationCap.setVisibility(View.VISIBLE);
            }
        }

        private void setRegistrationTillDate() {
            if (!mEvent.isRegistrationAvailable() || mEvent.isTicketingAvailable()) {
                findViewById(R.id.event_registration_label).setVisibility(View.INVISIBLE);
                mRegistrationTillTextView.setVisibility(View.INVISIBLE);
            }
            if (!mEvent.isRegistrationRequired()) {
                mRegistrationTillTextView.setText(eventRegistrationNotRequiredLabel);
            } else {
                Date regDate;
                if (mEvent.getRegistrationTill() != null) {
                    regDate = mEvent.getRegistrationTill();
                } else {
                    regDate = mEvent.getFirstDateTime();
                }
                mRegistrationTillTextView.setText(eventRegistrationTillLabel + " "
                        + DateFormatter.formatRegistrationDate(regDate));
            }
        }

        private void setRegistrationApproved() {
            if (mEvent.isRegistrationApproved()) {
                mRegistrationCap.setText(R.string.event_registration_status_registration_approved);
                mRegistrationCap.setVisibility(View.VISIBLE);
            }
        }
    }
}