package ru.evendate.android.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import android.widget.ProgressBar;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.adapters.OrganizationCategoryAdapter;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dmitry on 28.01.2016.
 */
public class OrganizationCatalogActivity extends AppCompatActivity
        implements OrganizationFilterDialog.OnCategorySelectListener {
    private final String LOG_TAG = OrganizationCatalogActivity.class.getSimpleName();

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    private OrganizationCategoryAdapter mAdapter;
    private boolean[] mSelectedItems;
    private ArrayList<OrganizationCategory> mCategoryList;
    @Bind(R.id.fab) FloatingActionButton mFAB;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    private DrawerWrapper mDrawer;
    AlertDialog errorDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_catalog);
        ButterKnife.bind(this);

        initToolbar();
        initRecyclerView();
        initProgressBar();
        initFAB();
        initDrawer();
        displayProgress();
        mFAB.setVisibility(View.INVISIBLE);

        loadCatalog();
        mDrawer.getDrawer().setSelection(DrawerWrapper.CATALOG_IDENTIFIER);
        mDrawer.start();
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
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

    private void initProgressBar() {
        mProgressBar.getProgressDrawable()
                .setColorFilter(ContextCompat.getColor(this, R.color.accent), PorterDuff.Mode.SRC_IN);
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
    public void onStop() {
        super.onStop();
        if (errorDialog != null)
            errorDialog.dismiss();
    }

    private void loadCatalog() {
        ApiService apiService = ApiFactory.getService(this);
        Observable<ResponseArray<OrganizationCategory>> observable =
                apiService.getCatalog(EvendateAccountManager.peekToken(this), OrganizationCategory.FIELDS_LIST);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoaded(result.getData()),
                        this::onError,
                        this::hideProgress
                );
    }

    public void onLoaded(ArrayList<OrganizationCategory> subList) {
        Log.i(LOG_TAG, "loaded");
        mCategoryList = subList;
        mAdapter.setCategoryList(subList);
        mFAB.show();
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, error.getMessage());
        errorDialog = ErrorAlertDialogBuilder.newInstance(this,
                (DialogInterface dialog, int which) -> {
                    loadCatalog();
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
