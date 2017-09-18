package ru.evendate.android.ui.tinder;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;

public class RecommenderActivity extends BaseActivity {

    private static final String TAG_RECOMMENDER = "tag_recommender";
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.container) FrameLayout mContainer;
    @BindView(R.id.main_content) CoordinatorLayout mMainContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tinder_recommender);
        ButterKnife.bind(this);
        initToolbar();
        initDrawer();

        RecommenderFragment recommenderFragment = (RecommenderFragment) getSupportFragmentManager()
                .findFragmentByTag(TAG_RECOMMENDER);
        if (recommenderFragment == null) {
            recommenderFragment = RecommenderFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, recommenderFragment, TAG_RECOMMENDER).commit();
        }
        new RecommenderPresenter(new DataRepository(this), recommenderFragment);
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setNavigationOnClickListener((View v) -> mDrawer.getDrawer().openDrawer());
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new RecommenderActivity.RecommenderNavigationItemClickListener(this, mDrawer.getDrawer()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.getDrawer().setSelection(DrawerWrapper.RECOMMENDER_IDENTIFIER);
    }

    @Override
    protected void onReload() {
        super.onReload();
        RecommenderFragment recommenderFragment = (RecommenderFragment)getSupportFragmentManager()
                .findFragmentByTag(TAG_RECOMMENDER);
        if (recommenderFragment != null) {
            recommenderFragment.onReload();
        }
    }

    /**
     * handle clicks on items of navigation drawer list
     */
    private class RecommenderNavigationItemClickListener extends DrawerWrapper.NavigationItemSelectedListener {

        RecommenderNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch ((int)drawerItem.getIdentifier()) {
                case DrawerWrapper.RECOMMENDER_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }
}
