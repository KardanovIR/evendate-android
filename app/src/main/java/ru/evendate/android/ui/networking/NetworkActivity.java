package ru.evendate.android.ui.networking;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.ui.EndlessContract;

import static com.google.common.base.Preconditions.checkNotNull;

public class NetworkActivity extends AppCompatActivity implements NetworkContract.OnProfileInteractionListener {

    final static int INTRO_CODE = 100;
    ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mViewPager = findViewById(R.id.view_pager);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(mViewPager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            private final int TAB_COUNT = 2;
            private final int CONTACT_TAB = 0;
            private final int APPLICATION_TAB = 1;
            ProfileListFragment contactsFragment;
            ProfileListPresenter contactsPresenter;
            ProfileListFragment applicationFragment;
            ProfileListPresenter applicationPresenter;

            @Override
            public int getCount() {
                return TAB_COUNT;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case CONTACT_TAB:
                        return "Люди";
                    case APPLICATION_TAB:
                        return "Заявки";
                    default:
                        return "";
                }
            }

            @Override
            public Fragment getItem(int position) {
                DataSource dataSource = new DataRepository(getBaseContext());
                switch (position) {
                    case CONTACT_TAB:
                        contactsFragment = ProfileListFragment.newInstance();
                        contactsPresenter = new ProfileListPresenter(dataSource, contactsFragment, false);
                        return contactsFragment;
                    case APPLICATION_TAB:
                        applicationFragment = ProfileListFragment.newInstance(true);
                        applicationPresenter = new ProfileListPresenter(dataSource, applicationFragment, true);
                        return applicationFragment;
                    default:
                        throw new IllegalArgumentException("invalid page number");

                }
            }
        });

        startActivityForResult(new Intent(this, NetworkIntroActivity.class), INTRO_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTRO_CODE) {
            if (resultCode == RESULT_OK) {

                Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void openProfile(Profile profile) {
        getSupportFragmentManager().beginTransaction()
                .add(R.id.main_content, new ProfileFragment())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack(null).commit();
    }

    static class ProfileListPresenter implements EndlessContract.EndlessPresenter {
        private static final int LENGTH = 10;
        private static String LOG_TAG = ProfileListPresenter.class.getSimpleName();
        private DataSource mDataSource;
        private Disposable mDisposable;
        private EndlessContract.EndlessView<ProfileListPresenter, Profile> mView;
        boolean isApplication;

        private ProfileListPresenter(@NonNull DataSource dataRepository,
                                     @NonNull EndlessContract.EndlessView<ProfileListPresenter, Profile> view,
                                     boolean isApplication) {
            mDataSource = checkNotNull(dataRepository);
            mView = checkNotNull(view);
            mView.setPresenter(this);
            this.isApplication = isApplication;
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

        //todo token
        public void load(boolean forceLoad, int page) {
            mView.setLoadingIndicator(forceLoad);
            String token = EvendateAccountManager.peekToken(mView.getContext());
            //            mDisposable = mDataSource.getRegisteredEvents(token, isFuture, page, LENGTH)
            //                    .subscribe(result -> {
            //                                List<EventRegistered> list = new ArrayList<>(result.getData());
            //                                boolean isLast = list.size() < LENGTH;
            //                                if (result.isOk()) {
            //                                    if (list.isEmpty() && mView.isEmpty()) {
            //                                        mView.showEmptyState();
            //                                    } else if (forceLoad) {
            //                                        mView.reshowList(list, isLast);
            //                                    } else {
            //                                        mView.showList(list, isLast);
            //                                    }
            //                                } else {
            //                                    mView.showError();
            //                                }
            //                            },
            //                            this::onError,
            //                            () -> mView.setLoadingIndicator(false)
            //                    );
            List<Profile> list = new ArrayList<>();
            list.add(new Profile());
            mView.showList(list, true);
            mView.setLoadingIndicator(false);
        }

        private void onError(Throwable error) {
            Log.e(LOG_TAG, "" + error.getMessage());
            mView.showError();
        }
    }
}
