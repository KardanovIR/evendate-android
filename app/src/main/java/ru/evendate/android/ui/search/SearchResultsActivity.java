package ru.evendate.android.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.models.Tag;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.AdapterController;
import ru.evendate.android.ui.AppendableAdapter;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.EventsAdapter;
import ru.evendate.android.ui.NpaLinearLayoutManager;
import ru.evendate.android.ui.ReelFragment;
import ru.evendate.android.ui.catalog.OrganizationCatalogAdapter;
import ru.evendate.android.ui.users.UsersAdapter;
import ru.evendate.android.views.LoadStateView;
import ru.evendate.android.views.TagsRecyclerView;

public class SearchResultsActivity extends AppCompatActivity {
    public static final String SEARCH_BY_TAG = "search_by_tag";
    String query = "";
    SearchView mSearchView;
    @Bind(R.id.main_content) RelativeLayout mRelativeLayout;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.tabs) TabLayout mTabs;
    @Bind(R.id.pager) ViewPager mViewPager;
    @Bind(R.id.hint) TextView mHintView;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;
    @Bind(R.id.tags) TagsRecyclerView tagsView;
    private String LOG_TAG = SearchResultsActivity.class.getSimpleName();
    private SearchPagerAdapter mSearchPagerAdapter;
    private DrawerWrapper mDrawer;
    private boolean isSearchByTag;
    private SearchEventFragment searchEventByTagFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);

        initToolbar();
        initDrawer();

        mSearchPagerAdapter = new SearchPagerAdapter(getSupportFragmentManager(), this);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mSearchPagerAdapter);

        mTabs.setupWithViewPager(mViewPager);
        setupPagerStat();
        searchEventByTagFragment = (SearchEventFragment)getSupportFragmentManager()
                .findFragmentById(R.id.search_tag_fragment);

        handleIntent(getIntent());
        tagsView.setOnTagClickListener((String tag) -> {
            isSearchByTag = true;
            query = tag;
            setSearchHint();
            setSearchQueryAndStart();
        });
        mLoadStateView.setOnReloadListener(this::loadTags);
        loadTags();
        mDrawer.start();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener((View v) -> finish());
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new DrawerWrapper.NavigationItemSelectedListener(this, mDrawer.getDrawer()));
        mDrawer.getDrawer().keyboardSupportEnabled(this, true);
    }

    /**
     * setup screen names of fragments for statistic screen tracking
     */
    private void setupPagerStat() {
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                new Statistics(getApplicationContext()).sendCurrentScreenName("Search Screen ~" +
                        mSearchPagerAdapter.getPageLabel(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView)menu.findItem(R.id.action_search).getActionView();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                openPager();
                tagsView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!newText.equals("")) {
                    mHintView.setVisibility(View.GONE);
                    tagsView.setVisibility(View.GONE);
                }
                return false;
            }
        });

        if (!query.equals(""))
            setSearchQueryAndStart();
        removeCollapseIcon();
        setSearchHint();
        return true;
    }

    private void removeCollapseIcon() {
        ImageView collapsedIcon = (ImageView)mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        collapsedIcon.setLayoutParams(new LinearLayout.LayoutParams(0, 0));
    }

    private void setSearchHint() {
        if (isSearchByTag)
            mSearchView.setQueryHint(getString(R.string.search_hint_tag));
        else
            mSearchView.setQueryHint(getString(R.string.search_hint));

    }

    //todo solid
    private void loadTags() {
        mLoadStateView.showProgress();
        ApiService apiService = ApiFactory.getService(this);

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_MONTH, -10);

        Observable<ResponseArray<Tag>> observable =
                apiService.getTopTags(EvendateAccountManager.peekToken(this), ServiceUtils.formatDateRequest(c.getTime()), 10);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> tagsView.setTags(result.getData(), ChipsLayoutManager.STRATEGY_CENTER),
                        this::onError,
                        mLoadStateView::hideProgress
                );
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
        setSearchQueryAndStart();
    }

    private void handleIntent(Intent intent) {
        Log.d(LOG_TAG, "handle intent");
        if (intent == null)
            return;
        //todo ?
        if (!isSearchByTag)
            isSearchByTag = intent.getBooleanExtra(SEARCH_BY_TAG, false);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(LOG_TAG, query);
            this.query = query;
        }
    }

    private void setSearchQueryAndStart() {
        if (mSearchView == null)
            return;
        mSearchView.setQuery(query, false);
        search();
    }

    private void search() {
        if (isSearchByTag)
            searchEventByTagFragment.search(query);
        else {
            for (SearchResultFragment fragment : mSearchPagerAdapter.getFragments()) {
                fragment.search(query);
            }
        }
    }

    private void openPager() {
        if (isSearchByTag)
            return;
        mTabs.setVisibility(View.VISIBLE);
        mViewPager.setVisibility(View.VISIBLE);
    }

    private void hidePager() {
        mTabs.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);
    }

    public abstract static class SearchResultFragment extends Fragment implements LoadStateView.OnReloadListener {
        protected static final String LOG_TAG = SearchResultFragment.class.getSimpleName();
        protected AbstractAdapter mAdapter;
        protected String query;
        @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
        @Bind(R.id.load_state) LoadStateView mLoadStateView;

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_search, container, false);
            ButterKnife.bind(this, rootView);
            initRecyclerView();
            initLoadStateView();
            return rootView;
        }

        private void initLoadStateView() {
            mLoadStateView.setOnReloadListener(this);
            mLoadStateView.setEmptyHeader(getContext().getString(R.string.search_empty_header));
            mLoadStateView.setEmptyDescription(getContext().getString(R.string.search_empty_description));
        }

        protected void initRecyclerView() {
            mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getContext()));
            mRecyclerView.setItemAnimator(new LandingAnimator());
        }

        abstract void loadData();

        protected void onStartLoad() {
            mLoadStateView.hide();
            mAdapter.reset();
        }

        public void onError(Throwable error) {
            Log.e(LOG_TAG, "" + error.getMessage());
            mLoadStateView.showErrorHint();
        }

        public void search(String query) {
            this.query = query;
            loadData();
        }

        protected void checkListAndShowHint() {
            if (mAdapter.isEmpty())
                mLoadStateView.showEmptyHint();
        }

        @Override
        public void onReload() {
            loadData();
        }
    }

    public static class SearchEventFragment extends SearchResultFragment
            implements AdapterController.AdapterContext {

        protected AdapterController mAdapterController;

        @Override
        protected void initRecyclerView() {
            super.initRecyclerView();
            mAdapter = new EventsAdapter(getContext(), mRecyclerView, ReelFragment.ReelType.CALENDAR.type());
            mAdapterController = new AdapterController(this, (AppendableAdapter)mAdapter);
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        protected void loadData() {
            onStartLoad();
            mAdapterController.reset();
            loadAdaptive();
        }

        protected void loadAdaptive() {
            ApiService apiService = ApiFactory.getService(getContext());

            final int length = mAdapterController.getLength();
            final int offset = mAdapterController.getOffset();

            Observable<ResponseArray<Event>> observable =
                    apiService.findEvents(EvendateAccountManager.peekToken(getContext()), query, true,
                            Event.FIELDS_LIST, EventFeed.ORDER_BY_FAVORITE_AND_FIRST_TIME, length, offset);

            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> onLoadedEvents(result.getData()),
                            this::onError,
                            mLoadStateView::hideProgress
                    );
        }

        @Override
        public void requestNext() {
            loadAdaptive();
        }

        public void onLoadedEvents(ArrayList<Event> subList) {
            Log.i(LOG_TAG, "loaded " + subList.size() + " events");
            mAdapterController.loaded(subList);
            checkListAndShowHint();
        }
    }

    public static class SearchOrgFragment extends SearchResultFragment
            implements AdapterController.AdapterContext {

        @Override
        protected void initRecyclerView() {
            super.initRecyclerView();
            mAdapter = new OrganizationCatalogAdapter(getContext());
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        protected void loadData() {
            onStartLoad();
            ApiService apiService = ApiFactory.getService(getContext());
            Observable<ResponseArray<OrganizationFull>> observable =
                    apiService.findOrganizations(EvendateAccountManager.peekToken(getContext()), query,
                            OrganizationSubscription.FIELDS_LIST);

            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> onLoadedOrgs(new ArrayList<>(result.getData())),
                            this::onError,
                            mLoadStateView::hideProgress
                    );
        }

        @Override
        public void requestNext() {
            loadData();
        }


        public void onLoadedOrgs(ArrayList<OrganizationSubscription> subList) {
            Log.i(LOG_TAG, "loaded " + subList.size() + " orgs");
            mAdapter.replace(new ArrayList<>(subList));
            checkListAndShowHint();
        }
    }

    public static class SearchUsersFragment extends SearchResultFragment
            implements AdapterController.AdapterContext {

        @Override
        protected void initRecyclerView() {
            super.initRecyclerView();
            mAdapter = new UsersAdapter(getContext());
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        protected void loadData() {
            onStartLoad();
            ApiService apiService = ApiFactory.getService(getContext());
            Observable<ResponseArray<UserDetail>> observable =
                    apiService.findUsers(EvendateAccountManager.peekToken(getContext()), query,
                            UserDetail.FIELDS_LIST);

            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> onLoaded(result.getData()),
                            this::onError,
                            mLoadStateView::hideProgress
                    );
        }

        @Override
        public void requestNext() {
            loadData();
        }


        public void onLoaded(ArrayList<UserDetail> subList) {
            Log.i(LOG_TAG, "loaded " + subList.size() + " orgs");
            mAdapter.replace(new ArrayList<>(subList));
            checkListAndShowHint();
        }
    }

    public static class SearchEventByTagFragment extends SearchEventFragment {

        @Override
        protected void loadAdaptive() {
            ApiService apiService = ApiFactory.getService(getContext());

            final int length = mAdapterController.getLength();
            final int offset = mAdapterController.getOffset();

            Observable<ResponseArray<Event>> observable =
                    apiService.findEventsByTags(EvendateAccountManager.peekToken(getContext()), query, true,
                            Event.FIELDS_LIST, EventFeed.ORDER_BY_FAVORITE_AND_FIRST_TIME, length, offset);

            observable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            result -> onLoadedEvents(result.getData()),
                            this::onError,
                            mLoadStateView::hideProgress
                    );
        }
    }

    class SearchPagerAdapter extends FragmentPagerAdapter {
        private final int TAB_COUNT = 3;
        private final int EVENT_TAB = 0;
        private final int ORG_TAB = 1;
        private final int USER_TAB = 2;
        private Context mContext;
        private ArrayList<SearchResultFragment> fragments = new ArrayList<>();

        SearchPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            mContext = context;
            fragments.add(EVENT_TAB, new SearchEventFragment());
            fragments.add(ORG_TAB, new SearchOrgFragment());
            fragments.add(USER_TAB, new SearchUsersFragment());

        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case EVENT_TAB: {
                    return fragments.get(EVENT_TAB);
                }
                case ORG_TAB: {
                    return fragments.get(ORG_TAB);
                }
                case USER_TAB: {
                    return fragments.get(USER_TAB);
                }
                default:
                    throw new IllegalArgumentException("invalid page number");
            }
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        ArrayList<SearchResultFragment> getFragments() {
            return fragments;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case EVENT_TAB:
                    return mContext.getString(R.string.tab_search_events);
                case ORG_TAB:
                    return mContext.getString(R.string.tab_search_organizations);
                case USER_TAB:
                    return mContext.getString(R.string.tab_search_users);
                default:
                    return null;
            }
        }

        /**
         * return strings for statistics
         */
        String getPageLabel(int position) {
            switch (position) {
                case EVENT_TAB:
                    return mContext.getString(R.string.stat_page_search_events);
                case ORG_TAB:
                    return mContext.getString(R.string.stat_page_search_orgs);
                case USER_TAB:
                    return mContext.getString(R.string.stat_page_search_users);
                default:
                    return null;
            }
        }
    }
}