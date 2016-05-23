package ru.evendate.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.adapters.OrganizationCategoryAdapter;
import ru.evendate.android.loaders.CatalogLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.OrganizationType;

/**
 * Created by Dmitry on 28.01.2016.
 */
public class OrganizationCatalogActivity extends AppCompatActivity
        implements OrganizationFilterDialog.OnCategorySelectListener,
        View.OnClickListener, LoaderListener<ArrayList<OrganizationType>> {
    @Bind(R.id.recyclerView)
    android.support.v7.widget.RecyclerView mRecyclerView;
    private CatalogLoader mLoader;
    private OrganizationCategoryAdapter mAdapter;
    private boolean[] mSelectedItems;
    private ArrayList<OrganizationType> mCategoryList;
    @Bind(R.id.fab)
    FloatingActionButton mFAB;
    @Bind(R.id.progressBar)
    ProgressBar mProgressBar;
    private EvendateDrawer mDrawer;
    /*
    @Bind(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    */
    @Bind(R.id.toolbar)
    Toolbar mToolbar;

    @Nullable
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_catalog);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        //just change that fucking home icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.mipmap.ic_menu_white);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.getDrawer().openDrawer();
            }
        });
        mAdapter = new OrganizationCategoryAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mLoader = new CatalogLoader(this);
        mLoader.setLoaderListener(this);

        mFAB.setOnClickListener(this);
        mFAB.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_filter_list_white_48dp));
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        mLoader.startLoading();
        mDrawer = EvendateDrawer.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new CatalogNavigationItemClickListener(this, mDrawer.getDrawer()));
        mDrawer.getDrawer().setSelection(EvendateDrawer.ORGANIZATION_IDENTIFIER);
        mDrawer.start();
    }

    @Override
    public void onClick(View v) {
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

    @Override
    public void onLoaded(ArrayList<OrganizationType> subList) {
        mCategoryList = subList;
        mAdapter.setCategoryList(subList);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onError() {
        mProgressBar.setVisibility(View.GONE);
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(this, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLoader.startLoading();
                mProgressBar.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLoader.cancelLoad();
    }

    /**
     * handle clicks on items of navigation drawer list in main activity
     */
    private class CatalogNavigationItemClickListener
            extends NavigationItemSelectedListener {

        public CatalogNavigationItemClickListener(Context context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
                case EvendateDrawer.ORGANIZATION_IDENTIFIER:
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            mDrawer.closeDrawer();
            return true;
        }
    }
}
