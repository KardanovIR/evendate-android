package ru.evendate.android.ui;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.adapters.AbstractAdapter;
import ru.evendate.android.adapters.AppendableAdapter;
import ru.evendate.android.adapters.EventsAdapter;
import ru.evendate.android.adapters.NpaLinearLayoutManager;
import ru.evendate.android.adapters.OrganizationCatalogAdapter;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SearchResultsActivity extends AppCompatActivity implements AdapterController.AdapterContext{
    private String LOG_TAG = SearchResultsActivity.class.getSimpleName();

    String query = "";
    SearchView mSearchView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    private DrawerWrapper mDrawer;

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    private AbstractAdapter mAdapter;
    private AdapterController mAdapterController;
    AlertDialog errorDialog;
    SearchType type;

    @Bind(R.id.search_empty_container) LinearLayout mEmptyLayout;
    //@Bind(R.id.search_empty_header) TextView mEmptyHeader;
    @Bind(R.id.search_empty_description) TextView mEmptyDescription;

    public enum SearchType {
        ORGANIZATION(1),
        EVENT(2);

        final int type;

        SearchType(int type) {
            this.type = type;
        }

        static public SearchType getType(int pType) {
            for (SearchType type: SearchType.values()) {
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
    public static final String SEARCH_TYPE = "search_type";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        ButterKnife.bind(this);

        initToolbar();
        initDrawer();
        initProgressBar();
        handleIntent(getIntent());
        initRecyclerView();
        displayCap();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                Log.d(LOG_TAG, query);
                this.query = query;
                setSearchQuery(query);
            }
            else{
                type = SearchType.getType(intent.getIntExtra(SEARCH_TYPE, 0));
            }
        }
        if(!query.equals("")){
            if(mAdapterController != null){
                mAdapterController.reset();
                mAdapter.reset();
            }
            loadData();
            displayProgress();
        }
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener((View v) -> finish());
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    private void initProgressBar() {
        mProgressBar.getProgressDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.accent), PorterDuff.Mode.SRC_IN);
    }

    private void initRecyclerView() {
        mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new LandingAnimator());
        switch (type) {
            case EVENT:
                mAdapter = new EventsAdapter(this, mRecyclerView, ReelFragment.ReelType.CALENDAR.type());
                mAdapterController = new AdapterController(this, (AppendableAdapter)mAdapter);
                break;
            case ORGANIZATION:
                mAdapter = new OrganizationCatalogAdapter(this);
                break;
        }
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        setSearchQuery(query);
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        return true;
    }

    private void setSearchQuery(String query){
        if(mSearchView != null)
            mSearchView.setQuery(query, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mDrawer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (errorDialog != null)
            errorDialog.dismiss();
    }

    private void loadData() {
        hideCap();
        setCap();
        switch (type){
            case EVENT:
                loadEvents();
                break;
            case ORGANIZATION:
                loadOrg();
                break;
        }
    }

    private void loadOrg() {
        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<OrganizationFull>> observable =
                apiService.findOrganizations(EvendateAccountManager.peekToken(this), query,
                        OrganizationSubscription.FIELDS_LIST);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoadedOrgs(result.getData()),
                        this::onError,
                        this::hideProgress
                );
    }

    private void loadEvents() {
        ApiService apiService = ApiFactory.getEvendateService();

        final int length = mAdapterController.getLength();
        final int offset = mAdapterController.getOffset();

        Observable<ResponseArray<EventDetail>> observable =
                apiService.findEvents(EvendateAccountManager.peekToken(this), query, true,
                        EventDetail.FIELDS_LIST, EventFeed.ORDER_BY_TIME, length, offset);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoadedEvents(result.getData()),
                        this::onError,
                        this::hideProgress
                );
    }

    @Override
    public void requestNext() {
        loadEvents();
    }

    public void onLoadedOrgs(ArrayList<OrganizationFull> subList) {
        Log.i(LOG_TAG, "loaded " + subList.size() + " orgs");
        ((OrganizationCatalogAdapter)mAdapter).setOrganizationList(new ArrayList<>(subList));
        if (mAdapter.isEmpty()) {
            displayCap();
        }
    }

    public void onLoadedEvents(ArrayList<EventDetail> subList) {
        Log.i(LOG_TAG, "loaded " + subList.size() + " events");
        mAdapterController.loaded(subList);
        if (mAdapter.isEmpty()) {
            displayCap();
        }
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, error.getMessage());
        errorDialog = ErrorAlertDialogBuilder.newInstance(this,
                (DialogInterface dialog, int which) -> {
                    loadData();
                    dialog.dismiss();
                });
        errorDialog.show();
    }

    private void displayProgress() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void setCap(){
        switch (type){
            case EVENT:
                mEmptyDescription.setText(getResources().getString(R.string.search_empty_event_desc));
                break;
            case ORGANIZATION:
                mEmptyDescription.setText(getResources().getString(R.string.search_empty_org_desc));
                break;
        }
    }

    private void displayCap(){
        mEmptyLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void hideCap(){
        mEmptyLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}