package ru.evendate.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import ru.evendate.android.models.OrganizationType;
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

    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    private OrganizationCategoryAdapter mAdapter;
    private boolean[] mSelectedItems;
    private ArrayList<OrganizationType> mCategoryList;
    @Bind(R.id.fab) FloatingActionButton mFAB;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    private EvendateDrawer mDrawer;
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
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_menu_white);
        mToolbar.setNavigationOnClickListener((View v) -> mDrawer.getDrawer().openDrawer());
    }
    private void initRecyclerView(){
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
    private void initFAB(){
        mFAB.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_filter_list_white_48dp));
    }
    private void initProgressBar(){
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
    }
    private void initDrawer(){
        mDrawer = EvendateDrawer.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new CatalogNavigationItemClickListener(this, mDrawer.getDrawer()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadCatalog();
        mDrawer.getDrawer().setSelection(EvendateDrawer.CATALOG_IDENTIFIER);
        mDrawer.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mDrawer.cancel();
        if(errorDialog != null)
            errorDialog.dismiss();
    }

    private void loadCatalog(){
        displayProgress();

        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<OrganizationType>> observable =
                apiService.getCatalog(EvendateAccountManager.peekToken(this), OrganizationType.FIELDS_LIST);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoaded(result.getData()),
                        this::onError,
                        this::hideProgress
                );
    }

    public void onLoaded(ArrayList<OrganizationType> subList) {
        Log.i(LOG_TAG, "loaded");
        mCategoryList = subList;
        mAdapter.setCategoryList(subList);
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

    private void displayProgress(){
        mProgressBar.setVisibility(View.VISIBLE);
    }
    private void hideProgress(){
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
        ArrayList<OrganizationType> newItemSelected = new ArrayList<>();
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

        public CatalogNavigationItemClickListener(Context context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
                case EvendateDrawer.CATALOG_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }
}
