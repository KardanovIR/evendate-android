package ru.evendate.android.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.models.Tag;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.feed.ReelFragment;
import ru.evendate.android.ui.feed.ReelPresenter;
import ru.evendate.android.views.LoadStateView;
import ru.evendate.android.views.TagsRecyclerView;

import static ru.evendate.android.ui.feed.ReelFragment.ReelType.SEARCH;

public class SearchResultsActivity extends BaseActivity {
    public static final String SEARCH_BY_TAG = "search_by_tag";
    private String query = "";
    private SearchView mSearchView;
    @BindView(R.id.main_content) RelativeLayout mRelativeLayout;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.tabs) TabLayout mTabs;
    @BindView(R.id.pager) ViewPager mViewPager;
    @BindView(R.id.hint) TextView mHintView;
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    @BindView(R.id.tags) TagsRecyclerView tagsView;
    private String LOG_TAG = SearchResultsActivity.class.getSimpleName();
    private SearchPagerAdapter mSearchPagerAdapter;
    private boolean isSearchByTag;
    private SearchResultFragment.SearchEventFragment searchEventByTagFragment;

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
        searchEventByTagFragment = (SearchResultFragment.SearchEventFragment)getSupportFragmentManager()
                .findFragmentById(R.id.search_tag_fragment);
        ReelPresenter.newInstance(new DataRepository(this), searchEventByTagFragment, ReelFragment.ReelType.SEARCH_BY_TAG);

        handleIntent(getIntent());
        tagsView.setOnTagClickListener((String tag) -> {
            isSearchByTag = true;
            query = tag;
            setSearchHint();
            setSearchQueryAndStart();
        });
        mLoadStateView.setOnReloadListener(this::loadTags);
        loadTags();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener((View v) -> finish());
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new DrawerWrapper.NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.start();
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
        ImageView collapsedIcon = mSearchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
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

    private void onError(Throwable error) {
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

    class SearchPagerAdapter extends FragmentPagerAdapter {
        private final int TAB_COUNT = 3;
        private final int EVENT_TAB = 0;
        private final int ORG_TAB = 1;
        private final int USER_TAB = 2;
        private Context mContext;
        private ArrayList<SearchResultFragment> fragments = new ArrayList<>();

        SearchPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            DataSource dataSource = new DataRepository(getBaseContext());
            mContext = context;
            SearchResultFragment.SearchEventFragment searchEventsFragment = new SearchResultFragment.SearchEventFragment();
            fragments.add(EVENT_TAB, searchEventsFragment);
            ReelPresenter.newInstance(dataSource, searchEventsFragment, SEARCH);

            SearchResultFragment.SearchOrgFragment searchOrgFragment = new SearchResultFragment.SearchOrgFragment();
            fragments.add(ORG_TAB, searchOrgFragment);
            new OrganizationSearchPresenter(dataSource, searchOrgFragment);

            SearchResultFragment.SearchUsersFragment searchUsersFragment = new SearchResultFragment.SearchUsersFragment();
            fragments.add(USER_TAB, searchUsersFragment);
            new UserSearchPresenter(dataSource, searchUsersFragment);

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