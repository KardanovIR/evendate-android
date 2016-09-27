package ru.evendate.android.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Organization;

/**
 * Created by ds_gordeev on 14.03.2016.
 * handle clicks on items of navigation drawer list
 * used for all activities except main activity
 */

public class NavigationItemSelectedListener
        implements Drawer.OnDrawerItemClickListener {
    protected Activity mContext;
    protected Drawer mDrawer;

    public NavigationItemSelectedListener(Activity context, Drawer drawer) {
        mContext = context;
        mDrawer = drawer;
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switch (drawerItem.getIdentifier()) {
            case DrawerWrapper.REEL_IDENTIFIER:
                openReelActivity();
                break;
            case DrawerWrapper.CALENDAR_IDENTIFIER:
                openCalendarActivity();
                break;
            case DrawerWrapper.CATALOG_IDENTIFIER:
                openCatalogActivity();
                break;
            case DrawerWrapper.FRIENDS_IDENTIFIER:
                openFriendsActivity();
                break;
            case DrawerWrapper.SETTINGS_IDENTIFIER:
                openSettingsActivity();
                break;
            //case R.id.nav_add_account:
            //    Intent authIntent = new Intent(mContext, AuthActivity.class);
            //    mContext.startActivity(authIntent);
            //    break;
            default:
                openOrganizationFromSub(drawerItem);
        }
        mDrawer.closeDrawer();
        return true;
    }

    private void openReelActivity() {
        Intent reelIntent = new Intent(mContext, MainActivity.class);
        reelIntent = addFlags(reelIntent);
        openActivity(reelIntent);
    }

    private void openCalendarActivity() {
        Intent calendarIntent = new Intent(mContext, CalendarActivity.class);
        calendarIntent = addFlags(calendarIntent);
        openActivity(calendarIntent);
    }

    private void openCatalogActivity() {
        Intent orgIntent = new Intent(mContext, OrganizationCatalogActivity.class);
        orgIntent = addFlags(orgIntent);
        openActivity(orgIntent);
    }

    private void openFriendsActivity() {
        Intent friendsIntent = new Intent(mContext, UserListActivity.class);
        friendsIntent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.FRIENDS.type());
        openActivity(friendsIntent);
    }

    private void openSettingsActivity() {
        Intent settingsIntent = new Intent(mContext, SettingsActivity.class);
        settingsIntent = addFlags(settingsIntent);
        openActivity(settingsIntent);
    }

    private void openOrganizationFromSub(IDrawerItem drawerItem) {
        int id = getOrgIdFromDrawerItem(drawerItem);
        Intent detailIntent = new Intent(mContext, OrganizationDetailActivity.class);
        detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                .buildUpon().appendPath(Long.toString(id)).build());
        openActivity(detailIntent);
    }

    private int getOrgIdFromDrawerItem(IDrawerItem drawerItem) {
        return ((Organization)drawerItem.getTag()).getEntryId();
    }

    private Intent addFlags(Intent intent){
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    private void openActivity(Intent intent) {
        if (Build.VERSION.SDK_INT >= 21) {
            mContext.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(mContext).toBundle());
        } else
            mContext.startActivity(intent);
    }
}