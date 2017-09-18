package ru.evendate.android.ui.catalog;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.Disposable;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.City;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.cities.CityActivity;
import ru.evendate.android.ui.orgdetail.OrganizationDetailActivity;
import ru.evendate.android.ui.search.SearchResultsActivity;
import ru.evendate.android.views.LoadStateView;

import static ru.evendate.android.ui.cities.CityFragment.KEY_CITY;

/**
 * Created by Dmitry on 28.01.2016.
 */
public class OrganizationCatalogActivity extends BaseActivity
        implements OrganizationFilterDialog.OnCategorySelectListener, LoadStateView.OnReloadListener,
        OrganizationCatalogAdapter.OrganizationInteractionListener {
    public final static int SELECT_CITY_REQUEST = 0;
    private final String LOG_TAG = OrganizationCatalogActivity.class.getSimpleName();
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.city_title) TextView mCityTitle;
    @BindView(R.id.popup_city_down) ImageButton mPopupCityDown;
    @BindView(R.id.fab) FloatingActionButton mFAB;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.load_state) LoadStateView mLoadStateView;
    private City mSelectedCity;
    private Disposable mDisposable;
    private OrganizationCategoryAdapter mAdapter;
    private boolean[] mSelectedItems;
    private ArrayList<OrganizationCategory> mCategoryList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_organization_catalog);
        ButterKnife.bind(this);

        initToolbar();
        initRecyclerView();
        initFAB();
        initDrawer();
        mFAB.setVisibility(View.INVISIBLE);
        mLoadStateView.setOnReloadListener(this);


        mSelectedCity = EvendatePreferences.newInstance(this).getUserCity();

        mCityTitle.setText(mSelectedCity.getNameLocally());
        loadCatalog();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setNavigationOnClickListener((View v) -> mDrawer.getDrawer().openDrawer());
    }

    private void initRecyclerView() {
        mAdapter = new OrganizationCategoryAdapter(this, this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setPreserveFocusAfterLayout(false);
    }

    private void initFAB() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filter_list_white));
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new CatalogNavigationItemClickListener(this, mDrawer.getDrawer()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.org_catalog_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchResultsActivity.class);
                startActivity(searchIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.getDrawer().setSelection(DrawerWrapper.CATALOG_IDENTIFIER);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mDisposable != null)
            mDisposable.dispose();
    }

    private void startSelectCity() {
        Intent cityIntent = new Intent(this, CityActivity.class);
        startActivityForResult(cityIntent, SELECT_CITY_REQUEST);
    }

    private void onCityChanged(City city) {
        mSelectedCity = city;
        mCityTitle.setText(city.getNameLocally());
        loadCatalog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_CITY_REQUEST) {
            if (resultCode == RESULT_OK) {
                onCityChanged(Parcels.unwrap(data.getParcelableExtra(KEY_CITY)));
            }
        }
    }

    private void loadCatalog() {
        mLoadStateView.showProgress();
        DataRepository dataRepository = new DataRepository(this);
        String token = EvendateAccountManager.peekToken(this);
        mDisposable = dataRepository.getCatalog(token, mSelectedCity.getEntryId()).subscribe(
                result -> onLoaded(result.getData()),
                this::onError,
                mLoadStateView::hideProgress
        );
    }

    @Override
    public void onReload() {
        super.onReload();
        loadCatalog();
    }

    private void onLoaded(ArrayList<OrganizationCategory> subList) {
        mCategoryList = subList;
        mAdapter.setCategoryList(subList);
        mFAB.show();
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    public void openOrg(OrganizationSubscription organization) {
        Intent intent = new Intent(this, OrganizationDetailActivity.class);
        intent.setData(EvendateContract.OrganizationEntry.getContentUri(organization.getEntryId()));
        if (Build.VERSION.SDK_INT > 21)
            startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        else
            startActivity(intent);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.fab)
    public void onFabClick(View v) {
        if (v == mFAB) {
            if (mCategoryList == null)
                return;
            if (mSelectedItems == null) {
                mSelectedItems = new boolean[mCategoryList.size()];
                Arrays.fill(mSelectedItems, Boolean.FALSE);
            }
            OrganizationFilterDialog dialog = OrganizationFilterDialog
                    .newInstance(mCategoryList, mSelectedItems);
            dialog.setCategorySelectListener(this);
            dialog.show(getSupportFragmentManager(), "OrganizationFilterDialog");
        }
    }

    @Override
    public void onCategorySelected(boolean[] itemsSelected) {
        ArrayList<OrganizationCategory> newItemSelected = new ArrayList<>();
        mSelectedItems = itemsSelected;
        for (int i = 0; i < itemsSelected.length; i++) {
            if (itemsSelected[i])
                newItemSelected.add(mCategoryList.get(i));
        }
        mAdapter.setCategoryList(newItemSelected);
    }

    @OnClick(R.id.title_container)
    public void onClick() {
        startSelectCity();
    }

    /**
     * handle clicks on items of navigation drawer list in main activity
     */
    private class CatalogNavigationItemClickListener extends DrawerWrapper.NavigationItemSelectedListener {

        CatalogNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch ((int)drawerItem.getIdentifier()) {
                case DrawerWrapper.CATALOG_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }
}
