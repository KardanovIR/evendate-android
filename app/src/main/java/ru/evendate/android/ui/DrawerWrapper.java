package ru.evendate.android.ui;

import android.accounts.Account;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.view.View;

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

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.auth.AuthActivity;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dmitry on 11.02.2016.
 */
public class DrawerWrapper {
    public final static int TICKETS_IDENTIFIER = 6;
    public final static int ADMINISTRATION_IDENTIFIER = 7;
    final static int REEL_IDENTIFIER = 1;
    final static int CALENDAR_IDENTIFIER = 2;
    final static int CATALOG_IDENTIFIER = 3;
    final static int FRIENDS_IDENTIFIER = 4;
    final static int SETTINGS_IDENTIFIER = 5;
    private static UserDetail mUser;

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    private final String LOG_TAG = DrawerWrapper.class.getSimpleName();
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;
    private ArrayList<OrganizationSubscription> mSubscriptions;
    private Context mContext;
    private OnSubLoadListener listener;
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

    protected DrawerWrapper(Drawer drawer, AccountHeader accountHeader, final Context context) {
        mContext = context;
        mDrawer = drawer;
        mAccountHeader = accountHeader;
    }

    public static DrawerWrapper newInstance(Activity context) {
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
        drawer.keyboardSupportEnabled(context, true);
        DrawerWrapper drawerWrapper = new DrawerWrapper(drawer, headerResult, context);
        drawerWrapper.setupMenu();

        if (Build.VERSION.SDK_INT >= 19) {
            drawer.getDrawerLayout().setFitsSystemWindows(false);
        }

        return drawerWrapper;
    }

    private static void openAccount(Context context) {
        if (mUser == null)
            return;
        Intent intent = new Intent(context, UserProfileActivity.class);
        intent.setData(EvendateContract.UserEntry.getContentUri(mUser.getEntryId()));
        if (Build.VERSION.SDK_INT >= 21) {
            context.startActivity(intent,
                    ActivityOptions.makeSceneTransitionAnimation((Activity) context).toBundle());
        } else
            context.startActivity(intent);
    }

    private void setupMenu() {
        mDrawer.removeAllItems();
        mDrawer.addItems(
                reelItem);
        if (mUser != null && mUser.isEditor()) {
            mDrawer.addItems(administrationItem);
        }
        mDrawer.addItems(
                calendarItem,
                ticketsItem,
                friendsItem,
                settingsItem,
                organizationsItem,
                new SectionDrawerItem().withName(R.string.drawer_subscriptions)
        );
    }

    private AccountHeader getAccountHeader() {
        return mAccountHeader;
    }

    private void updateSubs() {
        setupMenu();
        for (OrganizationSubscription org : mSubscriptions) {
            SubscriptionDrawerItem item = (SubscriptionDrawerItem) new SubscriptionDrawerItem().withName(org.getShortName())
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

    ArrayList<OrganizationSubscription> getSubs() {
        return mSubscriptions;
    }

    void setListener(OnSubLoadListener listener) {
        this.listener = listener;
    }

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
                        onLoaded(mUser.getSubscriptions());
                        Account account = EvendateAccountManager.getSyncAccount(mContext);
                        if (account == null)
                            return;

                        getAccountHeader().clear();
                        getAccountHeader().addProfiles(
                                new ProfileDrawerItem().withName(mUser.getFirstName() + " " + mUser.getLastName())
                                        .withEmail(account.name)
                                        .withIcon(R.drawable.ic_avatar_cap)
                                        .withIcon(mUser.getAvatarUrl()),
                                new ProfileDrawerItem().withName(mContext.getString(R.string.drawer_log_out))
                                        .withIcon(R.drawable.ic_exit_to_app_black)
                                        .withOnDrawerItemClickListener((View view, int position, IDrawerItem drawerItem) -> {
                                            EvendateAccountManager.deleteAccount(mContext);
                                            //todo ditch
                                            ((Activity) mContext).startActivityForResult(new Intent(mContext, AuthActivity.class), MainActivity.REQUEST_AUTH);
                                            return false;
                                        })
                        );
                    } else
                        onError();
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, "" + error.getMessage());
                });
    }

    private void onLoaded(ArrayList<OrganizationSubscription> subList) {
        mSubscriptions = subList;
        updateSubs();
        if (listener != null)
            listener.onSubLoaded();
    }

    void update() {
        loadSubs();
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
        //loadSubs();
        loadMe();
    }

    interface OnSubLoadListener {
        void onSubLoaded();
    }
}
