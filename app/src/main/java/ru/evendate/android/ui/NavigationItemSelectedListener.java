package ru.evendate.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
            case EvendateDrawer.REEL_IDENTIFIER: {
                Intent reelIntent = new Intent(mContext, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(MainActivity.TYPE, MainActivity.REEL);
                mContext.startActivity(reelIntent, bundle);
            }
            break;
            case R.id.settings:
                break;
            case EvendateDrawer.CALENDAR_IDENTIFIER: {
                Intent calendarIntent = new Intent(mContext, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(MainActivity.TYPE, MainActivity.CALENDAR);
                mContext.startActivity(calendarIntent, bundle);
            }
            break;
            case EvendateDrawer.ORGANIZATION_IDENTIFIER: {
                Intent organizationIntent = new Intent(mContext, MainActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(MainActivity.TYPE, MainActivity.CATALOG);
                mContext.startActivity(organizationIntent, bundle);
            }
            break;
            //case R.id.nav_add_account:
            //    Intent authIntent = new Intent(mContext, AuthActivity.class);
            //    mContext.startActivity(authIntent);
            //    break;
            //case R.id.nav_account_management:
            //    Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
            //    //TODO не робит(
            //    intent.putExtra(Settings.EXTRA_AUTHORITIES, mContext.getResources().getString(R.string.content_authority));
            //    intent.putExtra(Settings.EXTRA_AUTHORITIES, mContext.getResources().getString(R.string.account_type));
            //    mContext.startActivity(intent);
            //    break;
            default: {
                //open organization from subs
                int id = ((Organization)drawerItem.getTag()).getEntryId();
                Intent detailIntent = new Intent(mContext, OrganizationDetailActivity.class);
                detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                        .buildUpon().appendPath(Long.toString(id)).build());
                mContext.startActivity(detailIntent);
            }
            mDrawer.closeDrawer();
        }
        return true;
    }
}