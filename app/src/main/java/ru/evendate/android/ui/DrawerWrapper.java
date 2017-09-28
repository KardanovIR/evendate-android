package ru.evendate.android.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.BadgeStyle;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationModel;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.ui.calendar.CalendarActivity;
import ru.evendate.android.ui.catalog.OrganizationCatalogActivity;
import ru.evendate.android.ui.checkin.CheckInActivity;
import ru.evendate.android.ui.feed.MainActivity;
import ru.evendate.android.ui.orgdetail.OrganizationDetailActivity;
import ru.evendate.android.ui.settings.SettingsActivity;
import ru.evendate.android.ui.tickets.EventRegisteredActivity;
import ru.evendate.android.ui.tinder.RecommenderActivity;
import ru.evendate.android.ui.userdetail.UserProfileActivity;
import ru.evendate.android.ui.users.UserListActivity;
import ru.evendate.android.ui.users.UserListFragment;

/**
 * Builds and manages Material Drawer
 * Created by Dmitry on 11.02.2016.
 */
public class DrawerWrapper {
    private final String LOG_TAG = DrawerWrapper.class.getSimpleName();
    public final static int REEL_IDENTIFIER = 1;
    public final static int CALENDAR_IDENTIFIER = 2;
    public final static int CATALOG_IDENTIFIER = 3;
    private final static int FRIENDS_IDENTIFIER = 4;
    public final static int SETTINGS_IDENTIFIER = 5;
    public final static int TICKETS_IDENTIFIER = 6;
    public final static int ADMINISTRATION_IDENTIFIER = 7;
    public final static int RECOMMENDER_IDENTIFIER = 8;
    private static UserDetail mUser;

    // enable vector drawables support
    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private Drawer mDrawer;
    private AccountHeader mAccountHeader;
    private ArrayList<OrganizationSubscription> mSubscriptions;
    private Context mContext;
    private OnSubLoadListener listener;
    private OnLogOutListener mLogOutListener;
    /**
     * Call when we need authorization into Evendate Account
     */
    private AuthHandler mAuthHandler;

    /**
     * config drawable items
     */
    private PrimaryDrawerItem reelItem = new PrimaryDrawerItem().withName(R.string.drawer_reel)
            .withIcon(R.drawable.ic_local_play_black).withIdentifier(REEL_IDENTIFIER).withSelectable(true);
    private PrimaryDrawerItem calendarItem = new PrimaryDrawerItem().withName(R.string.drawer_calendar)
            .withIcon(R.drawable.ic_insert_invitation_black).withIdentifier(CALENDAR_IDENTIFIER).withSelectable(true);
    private PrimaryDrawerItem organizationsItem = new PrimaryDrawerItem().withName(R.string.drawer_organizations)
            .withIcon(R.drawable.ic_account_balance_black).withIdentifier(CATALOG_IDENTIFIER).withSelectable(true);
    private PrimaryDrawerItem friendsItem = new PrimaryDrawerItem().withName(R.string.drawer_friends)
            .withIcon(R.drawable.ic_people_black).withIdentifier(FRIENDS_IDENTIFIER).withSelectable(true);
    private PrimaryDrawerItem settingsItem = new PrimaryDrawerItem().withName(R.string.drawer_settings)
            .withIcon(R.drawable.ic_settings_black).withIdentifier(SETTINGS_IDENTIFIER).withSelectable(true);
    private PrimaryDrawerItem ticketsItem = new PrimaryDrawerItem().withName(R.string.drawer_tickets)
            .withIcon(R.drawable.ic_receipt_black).withIdentifier(TICKETS_IDENTIFIER).withSelectable(true);
    private PrimaryDrawerItem administrationItem = new PrimaryDrawerItem().withName(R.string.drawer_check_in)
            .withIcon(R.drawable.ic_event_note_black).withIdentifier(ADMINISTRATION_IDENTIFIER).withSelectable(true);
    private PrimaryDrawerItem recommenderItem = new PrimaryDrawerItem().withName(R.string.drawer_recommendations)
            .withIcon(R.drawable.ic_whatshot).withIdentifier(RECOMMENDER_IDENTIFIER).withSelectable(true);

    private DrawerWrapper(Drawer drawer, AccountHeader accountHeader, final Context context) {
        mContext = context;
        mDrawer = drawer;
        mAccountHeader = accountHeader;
    }

    public static DrawerWrapper newInstance(@NonNull Activity context, @NonNull AuthHandler authHandler) {
        DrawerBuilder result = new DrawerBuilder()
                .withOnDrawerItemClickListener((View view, int position, IDrawerItem drawerItem) -> true);
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(context)
                .withCompactStyle(false)
                .withHeaderBackground(R.drawable.gradient_profile)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        openAccount(context);
                        return true;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .withAlternativeProfileHeaderSwitching(false)
                .withOnlySmallProfileImagesVisible(false)
                .withProfileImagesClickable(true)
                .withOnlyMainProfileImageVisible(true)
                .build();
        result.withActivity(context)
                .withAccountHeader(headerResult);
        Drawer drawer = result.build();
        DrawerWrapper drawerWrapper = new DrawerWrapper(drawer, headerResult, context);
        drawerWrapper.setupMenu();

        drawerWrapper.mAuthHandler = authHandler;
        return drawerWrapper;
    }

    private static void openAccount(Context context) {
        if (mUser == null)
            return;
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.setData(EvendateContract.UserEntry.getContentUri(mUser.getEntryId()));
        if (Build.VERSION.SDK_INT >= 21) {
            context.startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation((Activity)context).toBundle());
        } else
            context.startActivity(intent);
    }

    private void setupMenu() {
        mDrawer.removeAllItems();
        mDrawer.addItems(
                reelItem
        );
        if (mUser != null && mUser.isEditor()) {
            mDrawer.addItems(administrationItem);
        }
        String token = EvendateAccountManager.peekToken(mContext);

        if (token != null) {
            mDrawer.addItems(
                    recommenderItem,
                    calendarItem,
                    ticketsItem,
                    friendsItem,
                    organizationsItem,
                    settingsItem,
                    new SectionDrawerItem().withName(R.string.drawer_subscriptions)
            );
        } else {
            mDrawer.addItems(
                    recommenderItem,
                    calendarItem,
                    organizationsItem,
                    settingsItem
            );
        }
    }

    private AccountHeader getAccountHeader() {
        return mAccountHeader;
    }

    private void updateSubs() {
        setupMenu();
        for (OrganizationSubscription org : mSubscriptions) {
            SubscriptionDrawerItem item = new SubscriptionDrawerItem().withName(org.getShortName())
                    .withIcon(org.getLogoSmallUrl()).withTag(org).withSelectable(false);
            if (org.getNewEventsCount() != 0) {
                item.withBadge(String.valueOf(org.getNewEventsCount())).withBadgeStyle(new BadgeStyle().withTextColorRes(R.color.accent));
            }
            mDrawer.addItem(item);
        }
        getDrawer().getRecyclerView().smoothScrollToPosition(0);
    }

    public Drawer getDrawer() {
        return mDrawer;
    }

    public ArrayList<OrganizationSubscription> getSubs() {
        return mSubscriptions;
    }

    public void setListener(OnSubLoadListener listener) {
        this.listener = listener;
    }

    void setLogOutListener(OnLogOutListener logOutListener) {
        mLogOutListener = logOutListener;
    }

    /**
     * load user model with subscription
     */
    //todo solid
    private void loadMe() {
        ApiService service = ApiFactory.getService(mContext);
        Observable<ResponseArray<UserDetail>> subsObservable =
                service.getMe(EvendateAccountManager.peekToken(mContext), UserDetail.FIELDS_LIST);

        subsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk()) {
                        mUser = result.getData().get(0);
                        EvendatePreferences.newInstance(mContext).putUser(mUser);
                        buildProfile();
                        setupMenu();
                        if (mUser.getSubscriptions() != null) {
                            onLoaded(mUser.getSubscriptions());
                        }
                    } else
                        onError();
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, "" + error.getMessage());
                });
    }

    /**
     * load only subscriptions
     */
    //todo solid
    private void loadSubs() {
        ApiService service = ApiFactory.getService(mContext);
        Observable<ResponseArray<OrganizationFull>> subsObservable =
                service.getSubscriptions(EvendateAccountManager.peekToken(mContext), OrganizationSubscription.FIELDS_LIST);

        subsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk())
                        onLoaded(new ArrayList<>(result.getData()));
                    else
                        onError();
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, "" + error.getMessage());
                });
    }

    private void buildProfile() {
        getAccountHeader().clear();
        Account account = EvendateAccountManager.getAccount(mContext);
        if (account == null) {
            View header = LayoutInflater.from(mContext).inflate(R.layout.item_header_drawer_nologin, null);
            Button login = header.findViewById(R.id.login_button);
            login.setOnClickListener((View v) -> {
                mAuthHandler.requestAuth().subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(token -> loadMe());
                mDrawer.closeDrawer();
            });
            mDrawer.setHeader(header);
            return;
        }

        mDrawer.setHeader(getAccountHeader().getView());
        getAccountHeader().addProfiles(
                new ProfileDrawerItem().withName(mUser.getFirstName() + " " + mUser.getLastName())
                        .withEmail(account.name)
                        .withIcon(R.drawable.ic_avatar_cap)
                        .withIcon(mUser.getAvatarUrl()),
                new ProfileDrawerItem().withName(mContext.getString(R.string.drawer_log_out))
                        .withIcon(R.drawable.ic_exit_to_app_black)
                        .withOnDrawerItemClickListener((View view, int position, IDrawerItem drawerItem) -> {
                            EvendateAccountManager.deleteAllAppAccounts(mContext);
                            EvendateAccountManager.setOnboardingUndone(mContext);
                            mAuthHandler.requestAuth().subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(token -> loadMe());
                            if (mLogOutListener != null) {
                                mLogOutListener.onLogOut();
                            }
                            buildProfile();
                            return false;
                        })
        );
        setupMenu();
    }

    private void onLoaded(ArrayList<OrganizationFull> subList) {
        mSubscriptions = new ArrayList<>(subList);
        updateSubs();
        if (listener != null)
            listener.onSubLoaded();
    }

    void update() {
        loadMe();
    }

    private void onError() {
        //if(isDestroyed())
        //    return;
        //mAlertDialog = ErrorAlertDialogBuilder.newInstance(this, new DialogInterface.OnClickListener() {
        //    @Override
        //    public void onClick(DialogInterface dialog, int which) {
        //        mSubscriptionLoader.getSubscriptions();
        //        mAlertDialog.dismiss();
        //    }
        //});
        //mAlertDialog.show();
    }

    public void start() {
        loadMe();
    }

    public interface OnSubLoadListener {
        void onSubLoaded();
    }

    public interface OnLogOutListener {
        void onLogOut();
    }

    /**
     * handle clicks on items of navigation drawer list
     */
    public static class NavigationItemSelectedListener
            implements Drawer.OnDrawerItemClickListener {
        protected Activity mContext;
        protected Drawer mDrawer;

        public NavigationItemSelectedListener(Activity context, Drawer drawer) {
            mContext = context;
            mDrawer = drawer;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch ((int)drawerItem.getIdentifier()) {
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
                case DrawerWrapper.TICKETS_IDENTIFIER:
                    openTicketsActivity();
                    break;
                case DrawerWrapper.ADMINISTRATION_IDENTIFIER:
                    openAdminActivity();
                    break;
                case DrawerWrapper.RECOMMENDER_IDENTIFIER:
                    openRecommenderActivity();
                    break;
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

        private void openTicketsActivity() {
            Intent ticketsIntent = new Intent(mContext, EventRegisteredActivity.class);
            ticketsIntent = addFlags(ticketsIntent);
            openActivity(ticketsIntent);
        }

        private void openAdminActivity() {
            Intent adminIntent = new Intent(mContext, CheckInActivity.class);
            adminIntent = addFlags(adminIntent);
            openActivity(adminIntent);
        }

        private void openRecommenderActivity() {
            Intent intent = new Intent(mContext, RecommenderActivity.class);
            intent = addFlags(intent);
            openActivity(intent);
        }

        private void openOrganizationFromSub(IDrawerItem drawerItem) {
            int id = getOrgIdFromDrawerItem(drawerItem);
            Intent detailIntent = new Intent(mContext, OrganizationDetailActivity.class);
            detailIntent.setData(EvendateContract.OrganizationEntry.getContentUri(id));
            openActivity(detailIntent);
        }

        private int getOrgIdFromDrawerItem(IDrawerItem drawerItem) {
            return ((OrganizationModel)drawerItem.getTag()).getEntryId();
        }

        private Intent addFlags(Intent intent) {
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
}
