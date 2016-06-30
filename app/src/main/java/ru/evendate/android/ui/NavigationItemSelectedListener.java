package ru.evendate.android.ui;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Organization;

/**
 * Created by ds_gordeev on 14.03.2016.
 * handle clicks on items of navigation drawer list
 * used for all activities except main activity
 */

public class NavigationItemSelectedListener
        implements Drawer.OnDrawerItemClickListener {
    protected Context mContext;
    protected Drawer mDrawer;

    public NavigationItemSelectedListener(Context context, Drawer drawer) {
        mContext = context;
        mDrawer = drawer;
    }

    @Override
    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
        switch (drawerItem.getIdentifier()) {
            case DrawerWrapper.REEL_IDENTIFIER:
                openReelActivity(); break;
            case R.id.settings:
                break;
            case DrawerWrapper.CALENDAR_IDENTIFIER:
                openCalendarActivity(); break;
            case DrawerWrapper.CATALOG_IDENTIFIER:
                openCatalogActivity(); break;
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

    private void openReelActivity(){
        Intent reelIntent = new Intent(mContext, MainActivity.class);
        mContext.startActivity(reelIntent);
    }
    private void openCalendarActivity(){
        Intent calendarIntent = new Intent(mContext, CalendarActivity.class);
        mContext.startActivity(calendarIntent);
    }
    private void openCatalogActivity(){
        Intent orgIntent = new Intent(mContext, OrganizationCatalogActivity.class);
        mContext.startActivity(orgIntent);
    }
    private void openOrganizationFromSub(IDrawerItem drawerItem){
        int id = getOrgIdFromDrawerItem(drawerItem);
        Intent detailIntent = new Intent(mContext, OrganizationDetailActivity.class);
        detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                .buildUpon().appendPath(Long.toString(id)).build());
        mContext.startActivity(detailIntent);
    }
    private int getOrgIdFromDrawerItem(IDrawerItem drawerItem){
        return ((Organization)drawerItem.getTag()).getEntryId();
    }
}