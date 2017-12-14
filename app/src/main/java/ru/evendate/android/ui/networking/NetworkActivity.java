package ru.evendate.android.ui.networking;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.EndlessContract;
import ru.evendate.android.views.LoadStateView;

import static com.google.common.base.Preconditions.checkNotNull;

public class NetworkActivity extends BaseActivity implements NetworkContract.OnProfileInteractionListener {
    private String LOG_TAG = NetworkActivity.class.getSimpleName();

    final static int INTRO_CODE = 100;
    ViewPager mViewPager;
    public final static String EVENT_ID_KEY = "event_id";
    int eventId;
    NetworkingProfile mNetworkingProfile;
    LoadStateView mLoadStateView;
    NetworkingPagerAdapter mAdapter;
    CoordinatorLayout mMainLayout;
    Disposable mApplyDisposable;
    private List<OnProfileListener> mProfileListeners = new ArrayList<>();

    public enum NetworkingListType {
        PEOPLE(0),
        REQUESTS(1),
        CONTACTS(2);

        final int type;

        NetworkingListType(int type) {
            this.type = type;
        }

        static public NetworkingListType getType(int pType) {
            for (NetworkingListType type : NetworkingListType.values()) {
                if (type.type() == pType) {
                    return type;
                }
            }
            throw new RuntimeException("unknown type");
        }

        public int type() {
            return type;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eventId = getIntent().getIntExtra(EVENT_ID_KEY, 0);

        setContentView(R.layout.activity_network);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
        mMainLayout = findViewById(R.id.main_content);
        mLoadStateView = findViewById(R.id.load_state);
        mLoadStateView.setOnReloadListener(this::getNetworkingProfile);
        mViewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);
        mAdapter = new NetworkingPagerAdapter(this, getSupportFragmentManager(), eventId);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);

        getNetworkingProfile();
        initDrawer();
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new DrawerWrapper.NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTRO_CODE) {
            if (resultCode == RESULT_OK) {
                mAdapter.reload();
            } else {
                finish();
            }
        }
    }

    private void getNetworkingProfile() {
        mLoadStateView.showProgress();
        String token = EvendateAccountManager.peekToken(this);
        ApiFactory.getService(this).getMyNetworkingProfile(token, eventId).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result.isOk()) {
                                onLoaded(result.getData());
                            } else {
                                onProfilesNotExists();
                            }
                        },
                        this::onError,
                        mLoadStateView::hideProgress
                );
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    private void onLoaded(NetworkingProfile profile) {
        mNetworkingProfile = profile;
        if (!profile.isSignedUp()) {
            Intent intent = new Intent(this, NetworkIntroActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable(NetworkIntroActivity.PROFILE_KEY, Parcels.wrap(profile));
            intent.putExtra(NetworkIntroActivity.EVENT_ID_KEY, eventId);
            startActivityForResult(intent, INTRO_CODE);
        } else {
            mViewPager.setVisibility(View.VISIBLE);
        }
    }

    private void onProfilesNotExists() {
        Intent intent = new Intent(this, NetworkIntroActivity.class);
        intent.putExtra(NetworkIntroActivity.EVENT_ID_KEY, eventId);
        startActivityForResult(intent, INTRO_CODE);
    }

    @Override
    public void openProfile(NetworkingProfile networkingProfile) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_content, ProfileFragment.newInstance(networkingProfile, eventId))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null).commit();
    }

    @Override
    public Observable<NetworkingProfile> applyRequest(NetworkingProfile networkingProfile) {
        String token = EvendateAccountManager.peekToken(this);
        mApplyDisposable = ApiFactory.getService(this).acceptNetworkingRequest(token, eventId, networkingProfile.requestUuid, true).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result.isOk()) {
                                Snackbar.make(mMainLayout, R.string.networking_snack_apply_accepted, Snackbar.LENGTH_SHORT).show();
                                for (OnProfileListener listener : mProfileListeners) {
                                    listener.onHide(networkingProfile);
                                }
                            } else {
                                Snackbar.make(mMainLayout, R.string.networking_snack_error, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.networking_snack_retry, (View v) -> applyRequest(networkingProfile)).show();
                            }
                        }, throwable -> Snackbar.make(mMainLayout, R.string.networking_snack_error, Snackbar.LENGTH_LONG)
                                .setAction(R.string.networking_snack_retry, (View v) -> applyRequest(networkingProfile)).show()
                );
        return Observable.create((ObservableEmitter<NetworkingProfile> e) ->
                mProfileListeners.add((NetworkingProfile profile) -> {
                    e.onNext(profile);
                    e.onComplete();
                })
        );
    }

    @Override
    public Observable<NetworkingProfile> hideRequest(NetworkingProfile networkingProfile) {

        String token = EvendateAccountManager.peekToken(this);
        ApiFactory.getService(this).acceptNetworkingRequest(token, eventId, networkingProfile.requestUuid, false).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (result.isOk()) {
                                Snackbar.make(mMainLayout, R.string.networking_snack_apply_hide, Snackbar.LENGTH_SHORT).show();
                                networkingProfile.request.accept_status = true;
                                for (OnProfileListener listener : mProfileListeners) {
                                    listener.onHide(networkingProfile);
                                }
                                mAdapter.contactsFragment.appendProfile(networkingProfile);
                            } else {
                                Snackbar.make(mMainLayout, R.string.networking_snack_error, Snackbar.LENGTH_LONG)
                                        .setAction(R.string.networking_snack_retry, (View v) -> hideRequest(networkingProfile)).show();
                            }
                        }, throwable -> Snackbar.make(mMainLayout, R.string.networking_snack_error, Snackbar.LENGTH_LONG)
                                .setAction(R.string.networking_snack_retry, (View v) -> hideRequest(networkingProfile)).show()
                );
        return Observable.create((ObservableEmitter<NetworkingProfile> e) ->
                mProfileListeners.add((NetworkingProfile profile) -> {
                    e.onNext(profile);
                    e.onComplete();
                })
        );
    }

    public interface OnProfileListener {
        void onHide(NetworkingProfile profile);
    }

    static class NetworkingPagerAdapter extends FragmentStatePagerAdapter {
        private final int TAB_COUNT = 3;
        private final int PARTICIPANT_TAB = 0;
        private final int APPLICATION_TAB = 1;
        private final int CONTACT_TAB = 2;
        ProfileListFragment participantFragment;
        ProfileListPresenter participantPresenter;
        ProfileListFragment contactsFragment;
        ProfileListPresenter contactsPresenter;
        ProfileListFragment applicationFragment;
        ProfileListPresenter applicationPresenter;
        AppCompatActivity mContext;
        int eventId;

        NetworkingPagerAdapter(AppCompatActivity context, FragmentManager fm, int eventId) {
            super(fm);
            mContext = context;
            this.eventId = eventId;
        }

        void reload() {
            participantPresenter.reload();
            applicationPresenter.reload();
            contactsPresenter.reload();
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
            super.restoreState(state, loader);
            DataSource dataSource = new DataRepository(mContext);
            participantFragment = (ProfileListFragment)mContext.getSupportFragmentManager().getFragment((Bundle)state, "f0");
            participantPresenter = new ProfileListPresenter(dataSource, participantFragment, NetworkingListType.PEOPLE, eventId);

            applicationFragment = (ProfileListFragment)mContext.getSupportFragmentManager().getFragment((Bundle)state, "f1");
            applicationPresenter = new ProfileListPresenter(dataSource, applicationFragment, NetworkingListType.REQUESTS, eventId);

            contactsFragment = (ProfileListFragment)mContext.getSupportFragmentManager().getFragment((Bundle)state, "f2");
            contactsPresenter = new ProfileListPresenter(dataSource, contactsFragment, NetworkingListType.CONTACTS, eventId);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case PARTICIPANT_TAB:
                    return mContext.getString(R.string.networking_tab_participants);
                case APPLICATION_TAB:
                    return mContext.getString(R.string.networking_tab_applies);
                case CONTACT_TAB:
                    return mContext.getString(R.string.networking_tab_contacts);
                default:
                    return "";
            }
        }

        @Override
        public Fragment getItem(int position) {
            DataSource dataSource = new DataRepository(mContext);
            switch (position) {
                case PARTICIPANT_TAB:
                    participantFragment = ProfileListFragment.newInstance();
                    participantPresenter = new ProfileListPresenter(dataSource, participantFragment, NetworkingListType.PEOPLE, eventId);
                    return participantFragment;
                case APPLICATION_TAB:
                    applicationFragment = ProfileListFragment.newInstance();
                    applicationPresenter = new ProfileListPresenter(dataSource, applicationFragment, NetworkingListType.REQUESTS, eventId);
                    return applicationFragment;
                case CONTACT_TAB:
                    contactsFragment = ProfileListFragment.newInstance();
                    contactsPresenter = new ProfileListPresenter(dataSource, contactsFragment, NetworkingListType.CONTACTS, eventId);
                    return contactsFragment;
                default:
                    throw new IllegalArgumentException("invalid page number");

            }
        }
    }

    static class ProfileListPresenter implements EndlessContract.EndlessPresenter {
        private static final int LENGTH = 10;
        private static String LOG_TAG = ProfileListPresenter.class.getSimpleName();
        private DataSource mDataSource;
        private Disposable mDisposable;
        private EndlessContract.EndlessView<ProfileListPresenter, NetworkingProfile> mView;
        NetworkActivity.NetworkingListType type = NetworkActivity.NetworkingListType.PEOPLE;
        int eventId;

        private ProfileListPresenter(@NonNull DataSource dataRepository,
                                     @NonNull EndlessContract.EndlessView<ProfileListPresenter, NetworkingProfile> view,
                                     NetworkingListType type, int eventId) {
            mDataSource = checkNotNull(dataRepository);
            mView = checkNotNull(view);
            mView.setPresenter(this);
            this.type = type;
            this.eventId = eventId;
        }

        @Override
        public void start() {
            reload();
        }

        @Override
        public void stop() {
            if (mDisposable != null)
                mDisposable.dispose();
        }

        @Override
        public void reload() {
            load(true, 0);
        }

        private Observable<ResponseArray<NetworkingProfile>> getObservable(String token, int page) {
            switch (type) {
                case PEOPLE:
                    return mDataSource.getNetworkingProfiles(token, eventId, page, LENGTH);
                case REQUESTS:
                    return mDataSource.getNetworkingRequests(token, eventId, page, LENGTH);
                case CONTACTS:
                    return mDataSource.getNetworkingContacts(token, eventId, page, LENGTH);
                default:
                    throw new IllegalArgumentException("Invalid type");
            }
        }

        //todo token
        public void load(boolean forceLoad, int page) {
            mView.setLoadingIndicator(forceLoad);
            String token = EvendateAccountManager.peekToken(mView.getContext());
            mDisposable = getObservable(token, page)
                    .subscribe(result -> {
                                List<NetworkingProfile> list = new ArrayList<>(result.getData());
                                boolean isLast = list.size() < LENGTH;
                                if (result.isOk()) {
                                    if (list.isEmpty() && mView.isEmpty()) {
                                        mView.showEmptyState();
                                    } else if (forceLoad) {
                                        mView.reshowList(list, isLast);
                                    } else {
                                        mView.showList(list, isLast);
                                    }
                                } else {
                                    mView.showError();
                                }
                            },
                            this::onError,
                            () -> mView.setLoadingIndicator(false)
                    );
        }

        private void onError(Throwable error) {
            Log.e(LOG_TAG, "" + error.getMessage());
            mView.showError();
        }
    }
}
