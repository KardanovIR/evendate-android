package ru.evendate.android.ui.checkin;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.DrawerWrapper;

//todo on confirm update pages tickets
public class CheckInActivity extends BaseActivity implements CheckInContract.ConfirmInteractionListener,
        CheckInContract.EventInteractionListener, CheckInContract.TicketInteractionListener,
        CheckInContract.QRReadListener, CheckInContract.TicketPagerTabInitializator, CheckInContract.SearchClickListener {

    private static final String KEY_EVENT_ID = "event_id";
    private static final String TAG_EVENTS = "events";
    private static final String TAG_TICKETS = "tickets";
    private static final String TAG_SEARCH_TICKETS = "search_tickets";
    private static final String TAG_QR_SCANNER = "qr_scanner";
    private static final String TAG_CONFIRM = "confirm";
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.tabs) TabLayout mTabs;
    @BindView(R.id.toolbar) Toolbar toolbar;
    private int selectedEventId;
    private int lastBackStackCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener((View v) -> {
            if (getSupportFragmentManager().findFragmentById(R.id.container) instanceof EventAdminListFragment) {
                mDrawer.getDrawer().openDrawer();
            } else {
                onBackPressed();
            }
        });

        initDrawer();
        mFab.setOnClickListener((View v) -> {
            if (getSupportFragmentManager().findFragmentByTag(TAG_QR_SCANNER) != null)
                return;
            getSupportFragmentManager().beginTransaction().add(R.id.container, QrScannerFragment.newInstance(selectedEventId), TAG_QR_SCANNER)
                    .hide(getSupportFragmentManager().findFragmentByTag(TAG_TICKETS))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack("").commit();
            TransitionManager.beginDelayedTransition(mCoordinatorLayout);
            mFab.hide();
            mTabs.setVisibility(View.GONE);
        });

        mTabs.setVisibility(View.GONE);
        mFab.setVisibility(View.INVISIBLE);

        CheckInContract.EventAdminView fragment = (CheckInContract.EventAdminView)getSupportFragmentManager().findFragmentByTag(TAG_EVENTS);
        if (fragment == null) {
            fragment = new EventAdminListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, (Fragment)fragment, TAG_EVENTS).commit();
        }
        new EventAdminListPresenter(new DataRepository(this), fragment);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
            if (lastBackStackCount != backStackEntryCount) {
                lastBackStackCount = backStackEntryCount;
                return;
            }
            if (getSupportFragmentManager().findFragmentByTag(TAG_QR_SCANNER) != null ||
                    getSupportFragmentManager().findFragmentByTag(TAG_SEARCH_TICKETS) != null) {
                TransitionManager.beginDelayedTransition(mCoordinatorLayout);
                mTabs.setVisibility(View.GONE);
                mFab.hide();
            } else if (getSupportFragmentManager().findFragmentByTag(TAG_TICKETS) != null) {
                TransitionManager.beginDelayedTransition(mCoordinatorLayout);
                mTabs.setVisibility(View.VISIBLE);
                mFab.show();
            } else {
                TransitionManager.beginDelayedTransition(mCoordinatorLayout);
                mTabs.setVisibility(View.GONE);
                mFab.hide();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.getDrawer().isDrawerOpen()) {
            mDrawer.getDrawer().closeDrawer();
            return;
        }
        //hide keyboard
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        super.onBackPressed();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_TICKETS);
        if (fragment != null && fragment.isAdded()) {
            TransitionManager.beginDelayedTransition(mCoordinatorLayout);
            mTabs.setVisibility(View.VISIBLE);
            mFab.show();
        } else {
            TransitionManager.beginDelayedTransition(mCoordinatorLayout);
            mTabs.setVisibility(View.GONE);
            mFab.hide();
            toolbar.setNavigationIcon(R.drawable.ic_menu);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_EVENT_ID, selectedEventId);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        selectedEventId = savedInstanceState.getInt(KEY_EVENT_ID);
        Fragment fragmentSearchTickets = getSupportFragmentManager().findFragmentByTag(TAG_SEARCH_TICKETS);
        Fragment fragmentEvent = getSupportFragmentManager().findFragmentByTag(TAG_EVENTS);
        if (fragmentEvent != null) {
            new EventAdminListPresenter(new DataRepository(this), (CheckInContract.EventAdminView)fragmentEvent);
        }
        if (getSupportFragmentManager().findFragmentByTag(TAG_SEARCH_TICKETS) == null &&
                getSupportFragmentManager().findFragmentByTag(TAG_QR_SCANNER) == null) {
            TransitionManager.beginDelayedTransition(mCoordinatorLayout);
            mTabs.setVisibility(View.VISIBLE);
            mFab.show();
        }
        if (fragmentSearchTickets != null) {
            new TicketsAdminPresenter(new DataRepository(this), (CheckInContract.TicketsAdminView)fragmentSearchTickets);
        }
        if (getSupportFragmentManager().findFragmentByTag(TAG_CONFIRM) != null) {
            new CheckInConfirmPresenter(this, new DataRepository(this), (CheckInContract.TicketConfirmView)getSupportFragmentManager().findFragmentByTag(TAG_CONFIRM));
        }
    }

    @Override
    protected void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this, this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new CheckInNavigationItemClickListener(this, mDrawer.getDrawer()));
        mDrawer.setListener(() -> mDrawer.getDrawer().setSelection(DrawerWrapper.ADMINISTRATION_IDENTIFIER));
    }

    @Override
    public void searchAction() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_SEARCH_TICKETS);
        Fragment fragmentTickets = getSupportFragmentManager().findFragmentByTag(TAG_TICKETS);
        if (fragment == null) {
            fragment = TicketsAdminFragment.TicketsAdminListFragment.newSearchInstance(selectedEventId);
        }
        new TicketsAdminPresenter(new DataRepository(this), (CheckInContract.TicketsAdminView)fragment);
        TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, TAG_SEARCH_TICKETS)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .hide(fragmentTickets).addToBackStack("").commit();
    }

    @Override
    public void onEventSelected(EventRegistered event) {
        //todo send model
        selectedEventId = event.getEntryId();
        Fragment fragment = TicketsAdminFragment.newInstance(event.getEntryId());
        getSupportFragmentManager().beginTransaction().add(R.id.container, fragment, TAG_TICKETS)
                .hide(getSupportFragmentManager().findFragmentByTag(TAG_EVENTS))
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .addToBackStack("")
                .commit();
        TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        mTabs.setVisibility(View.VISIBLE);
        mFab.show();
    }

    @Override
    public void onTicketSelected(int eventId, String ticketUuid) {
        if (getSupportFragmentManager().findFragmentByTag(TAG_CONFIRM) != null)
            return;
        CheckInConfirmDialogFragment fragment = CheckInConfirmDialogFragment.newInstance(eventId, ticketUuid);
        new CheckInConfirmPresenter(this, new DataRepository(this), fragment);
        fragment.show(getSupportFragmentManager(), TAG_CONFIRM);
    }

    @Override
    public void onConfirmCheckIn() {
        Toast.makeText(this, R.string.check_in_confirm, Toast.LENGTH_LONG).show();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_QR_SCANNER);
        if (fragment != null) {
            ((QrScannerFragment)fragment).startQrDecoding();
        }
        Fragment fragmentTickets = getSupportFragmentManager().findFragmentByTag(TAG_TICKETS);
        if (fragmentTickets != null) {
            ((TicketsAdminFragment)fragmentTickets).updateData();
        }

    }

    @Override
    public void onConfirmCheckInRevert() {
        Toast.makeText(this, R.string.check_in_confirm_dialog_button_revert, Toast.LENGTH_LONG).show();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_QR_SCANNER);
        if (fragment != null) {
            ((QrScannerFragment)fragment).startQrDecoding();
        }
        Fragment fragmentTickets = getSupportFragmentManager().findFragmentByTag(TAG_TICKETS);
        if (fragmentTickets != null) {
            ((TicketsAdminFragment)fragmentTickets).updateData();
        }
    }

    @Override
    public void onConfirmCheckInCancel() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_QR_SCANNER);
        if (fragment != null) {
            ((QrScannerFragment)fragment).startQrDecoding();
        }
    }

    @Override
    public void onConfirmCheckInError() {
        Toast.makeText(this, R.string.check_in_confirm_error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onQrRead(int eventId, String ticketUuid) {
        CheckInConfirmDialogFragment fragment = CheckInConfirmDialogFragment.newInstance(eventId, ticketUuid);
        new CheckInConfirmPresenter(this, new DataRepository(this), fragment);
        fragment.show(getSupportFragmentManager(), TAG_CONFIRM);
    }

    @Override
    public void onQrReadError() {
        Snackbar.make(mCoordinatorLayout, R.string.check_in_qr_error, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void initTabs(ViewPager viewPager) {
        mTabs.setupWithViewPager(viewPager);
    }

    /**
     * handle clicks on items of navigation drawer list in main activity
     */
    private class CheckInNavigationItemClickListener extends DrawerWrapper.NavigationItemSelectedListener {

        CheckInNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch ((int)drawerItem.getIdentifier()) {
                case DrawerWrapper.ADMINISTRATION_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }
}
