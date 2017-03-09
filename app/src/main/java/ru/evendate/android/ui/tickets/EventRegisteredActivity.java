package ru.evendate.android.ui.tickets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.parceler.Parcels;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.NavigationItemSelectedListener;
import ru.evendate.android.ui.ticketsdetail.TicketListActivity;

public class EventRegisteredActivity extends AppCompatActivity
        implements EventRegisteredListFragment.OnEventInteractionListener {

    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.pager) ViewPager mPager;
    @Bind(R.id.tabs) TabLayout mTabs;
    DrawerWrapper mDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_registered_list);
        ButterKnife.bind(this);
        initToolbar();
        initDrawer();

        EventRegisteredPagerAdapter eventRegisteredPagerAdapter = new EventRegisteredPagerAdapter(getSupportFragmentManager(), this);
        mPager.setAdapter(eventRegisteredPagerAdapter);
        mTabs.setupWithViewPager(mPager);
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setNavigationOnClickListener((View v) -> mDrawer.getDrawer().openDrawer());
    }

    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new TicketsNavigationItemClickListener(this, mDrawer.getDrawer()));
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.getDrawer().setSelection(DrawerWrapper.TICKETS_IDENTIFIER);
        mDrawer.start();
    }

    @Override
    public void onEventClick(EventRegistered item) {
        Intent ticketsIntent = new Intent(this, TicketListActivity.class);
        Bundle bundle = new Bundle();
        Parcelable parcelEvent = Parcels.wrap(item);
        bundle.putParcelable(TicketListActivity.EVENT_KEY, parcelEvent);
        ticketsIntent.putExtras(bundle);
        startActivity(ticketsIntent);
    }

    private class EventRegisteredPagerAdapter extends FragmentStatePagerAdapter {
        private Context mContext;

        EventRegisteredPagerAdapter(FragmentManager fragmentManager, Context context) {
            super(fragmentManager);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return EventRegisteredListFragment.newInstance(true);
                }
                case 1: {
                    return EventRegisteredListFragment.newInstance(false);
                }
                default:
                    throw new IllegalArgumentException("invalid page number");
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return mContext.getString(R.string.tab_event_registered_future);
                case 1:
                    return mContext.getString(R.string.tab_event_registered_past);
                default:
                    return null;
            }
        }
    }

    /**
     * handle clicks on items of navigation drawer list
     */
    private class TicketsNavigationItemClickListener extends NavigationItemSelectedListener {

        TicketsNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
                case DrawerWrapper.TICKETS_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }
}
