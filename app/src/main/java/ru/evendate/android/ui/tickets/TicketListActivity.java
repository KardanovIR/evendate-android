package ru.evendate.android.ui.tickets;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;
import ru.evendate.android.ui.eventdetail.EventDetailActivity;

public class TicketListActivity extends BaseActivity implements TicketDetailFragment.OnTicketInteractionListener {
    public static final String EVENT_KEY = "event";
    private static final String TICKETS_KEY = "tickets";
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.pager) ViewPager mViewPager;
    private PagerAdapter mAdapter;
    private Disposable mDisposable;
    private DataRepository mDataRepository;
    private EventRegistered mEvent;
    private List<Ticket> mTickets;
    private boolean isLoading = false;
    private boolean loadMoreAvailable = true;
    private int PAGE_LOAD_OFFSET = 2;
    private int LENGTH = 10;
    private String LOG_TAG = TicketListActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);
        ButterKnife.bind(this);

        handleIntent(getIntent());

        initToolbar();
        initDrawer();
        mDataRepository = new DataRepository(this);
        mAdapter = new TicketPagerAdapter(getSupportFragmentManager());

        mViewPager.setAdapter(mAdapter);
        mViewPager.setPageMargin(16);
        //mViewPager.setPadding(32, 0, 32, 0);
        mViewPager.setOffscreenPageLimit(4);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position > mTickets.size() + PAGE_LOAD_OFFSET) {
                    if (!isLoading) {
                        loadTickets(mTickets.size() % LENGTH);
                        isLoading = true;
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        mEvent = Parcels.unwrap(intent.getParcelableExtra(EVENT_KEY));
        mTickets = Parcels.unwrap(intent.getParcelableExtra(TICKETS_KEY));
        if (mTickets == null)
            mTickets = mEvent.getTickets();
        if (mTickets == null)
            mTickets = new ArrayList<>();
        mToolbar.setTitle(mEvent.getTitle());
        if (mTickets.size() < LENGTH)
            loadMoreAvailable = false;
    }

    @SuppressWarnings("ConstantConditions")
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new DrawerWrapper.NavigationItemSelectedListener(this, mDrawer.getDrawer()));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EVENT_KEY, Parcels.wrap(mEvent));
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mEvent = Parcels.unwrap(savedInstanceState.getParcelable(EVENT_KEY));
        mTickets = new ArrayList<>();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onUpPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadTickets(int page) {
        //todo token
        String token = EvendateAccountManager.peekToken(this);
        mDisposable = mDataRepository.getTickets(token, page, LENGTH).subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk()) {
                        onLoaded(new ArrayList<>(result.getData()));
                    }
                },
                this::onError
        );
    }

    @Override
    public void onEventClicked(EventRegistered Event) {
        Intent intent = new Intent(this, EventDetailActivity.class);
        intent.setData(EvendateContract.EventEntry.getContentUri(mEvent.getEntryId()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDisposable != null)
            mDisposable.dispose();
        isLoading = false;
    }

    private void onLoaded(List<Ticket> list) {
        if (list.size() < LENGTH)
            loadMoreAvailable = false;
        mTickets.addAll(list);
        isLoading = false;
        mAdapter.notifyDataSetChanged();
    }


    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        //todo not implemented edge case
    }

    private class TicketPagerAdapter extends FragmentStatePagerAdapter {

        TicketPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        public Fragment getItem(int position) {
            return TicketDetailFragment.newInstance(mEvent, mTickets.get(position));
        }

        @Override
        public int getCount() {
            return mTickets.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                default:
                    return null;
            }
        }
    }

}
