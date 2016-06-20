package ru.evendate.android.ui;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.adapters.OrganizationEventsAdapter;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.SubOrganizationLoader;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Contain details of organization
 */
public class OrganizationDetailFragment extends Fragment implements
        OrganizationEventsAdapter.OrganizationCardController, AdapterController.AdapterContext{
    private final String LOG_TAG = "OrganizationFragment";

    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;

    private int organizationId = -1;
    public static final String URI = "uri";
    private Uri mUri;

    @Bind(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.organization_image) ImageView mBackgroundView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.organization_toolbar_title) TextView mToolbarTitle;
    LinearLayoutManager mLayoutManager;
    int scrollOffset = 0;
    int toolbarColor = Color.TRANSPARENT;
    boolean isToolbarTransparent = true;
    ValueAnimator colorAnimation;
    EvendateDrawer mDrawer;

    private OrganizationEventsAdapter mAdapter;
    private AdapterController mAdapterController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization, container, false);
        ButterKnife.bind(this, rootView);

        mToolbar.setTitle("");
        mToolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle args = getArguments();
        if (args != null) {
            mUri = Uri.parse(args.getString(URI));
            organizationId = Integer.parseInt(mUri.getLastPathSegment());
        }

        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new OrganizationEventsAdapter(getContext(), mRecyclerView, this);
        mAdapterController = new AdapterController(this, mAdapter);
        mRecyclerView.setAdapter(mAdapter);
        initParallax();
        loadOrg();
        mDrawer = EvendateDrawer.newInstance(getActivity());
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(getActivity(), mDrawer.getDrawer()));
        mDrawer.start();

        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        return rootView;
    }

    private void initParallax(){
        mToolbar.setBackgroundColor(toolbarColor);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                setImageViewY(recyclerView);
                int targetColor;
                if (checkOrgCardScrolling(recyclerView) == isToolbarTransparent)
                    return;
                if (isToolbarTransparent) {
                    targetColor = getResources().getColor(R.color.primary);
                } else
                    targetColor = Color.TRANSPARENT;
                if (colorAnimation != null)
                    colorAnimation.cancel();
                colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), toolbarColor, targetColor);
                colorAnimation.setDuration(200); // milliseconds
                colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animator) {
                        toolbarColor = (int)animator.getAnimatedValue();
                        mToolbar.setBackgroundColor(toolbarColor);
                    }
                });
                colorAnimation.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        if (Build.VERSION.SDK_INT < 21)
                            return;
                        if (isToolbarTransparent) {
                            mAppBarLayout.setElevation(0);
                        }
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (Build.VERSION.SDK_INT < 21)
                            return;
                        if (!isToolbarTransparent) {
                            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4,
                                    getResources().getDisplayMetrics());
                            mAppBarLayout.setElevation(px);
                        }
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {}

                    @Override
                    public void onAnimationRepeat(Animator animation) {}
                });
                isToolbarTransparent = !isToolbarTransparent;
                colorAnimation.start();
            }
        });
        //mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
        //    @Override
        //    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //        appBarOffset = Math.abs(verticalOffset);
        //        setImageViewY();
        //    }
        //});
    }

    private boolean checkOrgCardScrolling(RecyclerView recyclerView) {
        float imageHeight = getResources().getDimension(R.dimen.organization_background_height);
        float actionBarHeight = 0;
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            actionBarHeight = TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        int index = mLayoutManager.findFirstVisibleItemPosition();
        return mAdapter.getItemViewType(index) == R.layout.card_organization_detail
                && (Math.abs(recyclerView.computeVerticalScrollOffset()) + actionBarHeight) < imageHeight;
    }

    private boolean checkOrgCardVisible() {
        int index = mLayoutManager.findFirstVisibleItemPosition();
        return mAdapter.getItemViewType(index) == R.layout.card_organization_detail;
    }

    private void setImageViewY(RecyclerView recyclerView) {
        scrollOffset = Math.abs(recyclerView.computeVerticalScrollOffset());
        if (checkOrgCardVisible()) {
            mBackgroundView.setVisibility(View.VISIBLE);
            mBackgroundView.setY(-scrollOffset * 0.5f);
        } else
            mBackgroundView.setVisibility(View.INVISIBLE);
    }

    private void loadOrg(){
        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        Observable<EvendateServiceResponseArray<OrganizationFull>> organizationObservable =
                evendateService.getOrganization(EvendateAccountManager.peekToken(getActivity()),
                        organizationId, OrganizationDetail.FIELDS_LIST);
        organizationObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    onLoaded(new ArrayList<>(result.getData()));
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "Complete!"));
    }

    private void loadEvents(){
        EvendateService evendateService = EvendateApiFactory.getEvendateService();
        Observable<EvendateServiceResponseArray<EventDetail>> observable =
                evendateService.getEvents(EvendateAccountManager.peekToken(getActivity()),
                        organizationId, true, EventDetail.FIELDS_LIST, "created_at",
                        mAdapterController.getLength(), mAdapterController.getOffset());

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    onLoadedEvents(new ArrayList<>(result.getData()));
                }, error -> {
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "Complete!"));
    }

    /**
     * handle subscription button
     * start subscribe/unsubscribe loader to carry it to server
     * push subscribe/unsubscribe stat to analytics
     */
    public void onSubscribed() {
        OrganizationDetail organization = mAdapter.getOrganization();
        SubOrganizationLoader subOrganizationLoader = new SubOrganizationLoader(getActivity(),
                organization, organization.isSubscribed());
        subOrganizationLoader.setLoaderListener(new LoaderListener<ArrayList<Void>>() {
            @Override
            public void onLoaded(ArrayList<Void> subList) {

            }

            @Override
            public void onError() {
                Toast.makeText(getActivity(), R.string.download_error, Toast.LENGTH_SHORT).show();
            }
        });
        organization.subscribe();
        Tracker tracker = EvendateApplication.getTracker();
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(getActivity().getString(R.string.stat_category_organization))
                .setLabel((Long.toString(organization.getEntryId())));
        if (organization.isSubscribed()) {
            event.setAction(getActivity().getString(R.string.stat_action_subscribe));
            Snackbar.make(mCoordinatorLayout, R.string.subscription_confirm, Snackbar.LENGTH_LONG).show();
        } else {
            event.setAction(getActivity().getString(R.string.stat_action_unsubscribe));
            Snackbar.make(mCoordinatorLayout, R.string.removing_subscription_confirm, Snackbar.LENGTH_LONG).show();
        }
        tracker.send(event.build());
        subOrganizationLoader.startLoading();
    }

    /**
     * handle place button click and open google map
     */
    public void onPlaceClicked() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + mAdapter.getOrganization().getDefaultAddress());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    /**
     * handle all user click and open user list for organization
     */
    @Override
    public void onUsersClicked() {
        Intent intent = new Intent(getContext(), UserListActivity.class);
        intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(mAdapter.getOrganization().getEntryId())).build());
        intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.organization.nativeInt);
        startActivity(intent);
    }

    /**
     * handle link button click and open organization page in browser
     */
    @Override
    public void onLinkClicked() {
        Intent openLink = new Intent(Intent.ACTION_VIEW);
        openLink.setData(Uri.parse(mAdapter.getOrganization().getSiteUrl()));
        startActivity(openLink);
    }

    public void onLoaded(ArrayList<OrganizationDetail> organizations) {
        if (!isAdded())
            return;
        mProgressBar.setVisibility(View.GONE);
        OrganizationDetail organization = organizations.get(0);
        mAdapter.setOrganization(organization);
        mAdapterController.loaded(organization.getEventsList());
        Picasso.with(getActivity())
                .load(organization.getBackgroundUrl())
                .error(R.drawable.default_background)
                .into(mBackgroundView);
        mToolbarTitle.setText(organization.getShortName());
    }

    public void onLoadedEvents(ArrayList<EventFeed> events) {
        if (!isAdded())
            return;
        mAdapterController.loaded(events);
    }

    public void onError() {
        if (!isAdded())
            return;
        mProgressBar.setVisibility(View.GONE);
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        loadOrg();
                        dialog.dismiss();
                    }
                });
        mAdapterController.notLoadedCauseError();
        mAdapterController.disableNext();
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDrawer.cancel();
    }

    public void requestNext() {
        loadEvents();
    }
}
