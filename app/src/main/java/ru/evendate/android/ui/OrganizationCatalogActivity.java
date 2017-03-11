package ru.evendate.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.adapters.OrganizationCategoryAdapter;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.City;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.ui.cities.CityActivity;
import ru.evendate.android.views.LoadStateView;
import rx.Subscription;

import static ru.evendate.android.ui.cities.CityActivity.KEY_CITY;

/**
 * Created by Dmitry on 28.01.2016.
 */
public class OrganizationCatalogActivity extends AppCompatActivity
        implements OrganizationFilterDialog.OnCategorySelectListener, LoadStateView.OnReloadListener {
    private final String LOG_TAG = OrganizationCatalogActivity.class.getSimpleName();

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.city_title) TextView mCityTitle;
    @Bind(R.id.popup_city_down) ImageButton mPopupCityDown;
    private OrganizationCategoryAdapter mAdapter;
    private boolean[] mSelectedItems;
    private ArrayList<OrganizationCategory> mCategoryList;
    @Bind(R.id.fab) FloatingActionButton mFAB;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    private DrawerWrapper mDrawer;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;
    City mSelectedCity;
    Subscription mSubscription;

    public final static int SELECT_CITY_REQUEST = 0;

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

        mDrawer.getDrawer().setSelection(DrawerWrapper.CATALOG_IDENTIFIER);
        mDrawer.start();

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
        mAdapter = new OrganizationCategoryAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initFAB() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        mFAB.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_filter_list_white));
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
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
    protected void onStop() {
        super.onStop();
        if (mSubscription != null)
            mSubscription.unsubscribe();
    }

    private void startSelectCity() {
        Intent cityIntent = new Intent(this, CityActivity.class);
        startActivityForResult(cityIntent, SELECT_CITY_REQUEST);
    }

    public void onCityChanged(City city) {
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
        mSubscription = dataRepository.getCatalog(mSelectedCity.getEntryId()).subscribe(
                result -> onLoaded(result.getData()),
                this::onError,
                mLoadStateView::hideProgress
        );
    }

    @Override
    public void onReload() {
        loadCatalog();
    }

    public void onLoaded(ArrayList<OrganizationCategory> subList) {
        mCategoryList = subList;
        mAdapter.setCategoryList(subList);
        mFAB.show();
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
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
    private class CatalogNavigationItemClickListener extends NavigationItemSelectedListener {

        CatalogNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
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
