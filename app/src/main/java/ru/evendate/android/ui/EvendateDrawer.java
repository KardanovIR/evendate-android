package ru.evendate.android.ui;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.view.View;

import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import java.util.ArrayList;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.MeLoader;
import ru.evendate.android.loaders.SubscriptionLoader;
import ru.evendate.android.models.Organization;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 11.02.2016.
 */
public class EvendateDrawer implements LoaderListener<ArrayList<Organization>> {
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;
    private SubscriptionLoader mSubscriptionLoader;
    ArrayList<Organization> mSubscriptions;
    private MeLoader mMeLoader;
    final static int REEL_IDENTIFIER = 1;
    final static int CALENDAR_IDENTIFIER = 2;
    final static int CATALOG_IDENTIFIER = 3;
    Context mContext;

    PrimaryDrawerItem reelItem = new PrimaryDrawerItem().withName(R.string.reel)
            .withIcon(R.drawable.event_icon).withIdentifier(REEL_IDENTIFIER).withSelectable(true);
    PrimaryDrawerItem calendarItem = new PrimaryDrawerItem().withName(R.string.calendar)
            .withIcon(R.drawable.calendar_icon).withIdentifier(CALENDAR_IDENTIFIER).withSelectable(true);
    PrimaryDrawerItem organizationsItem = new PrimaryDrawerItem().withName(R.string.title_activity_organization)
            .withIcon(R.drawable.organization_icon).withIdentifier(CATALOG_IDENTIFIER).withSelectable(true);

    protected EvendateDrawer(Drawer drawer, AccountHeader accountHeader, final Context context) {
        mContext = context;
        mDrawer = drawer;
        mAccountHeader = accountHeader;
        mSubscriptionLoader = new SubscriptionLoader(context);
        mSubscriptionLoader.setLoaderListener(this);
        mMeLoader = new MeLoader(context);
        mMeLoader.setLoaderListener(new LoaderListener<ArrayList<UserDetail>>() {
            @Override
            public void onLoaded(ArrayList<UserDetail> users) {
                UserDetail user = users.get(0);
                Account account = EvendateAccountManager.getSyncAccount(context);
                if(account == null)
                    return;

                getAccountHeader().clear();
                getAccountHeader().addProfiles(
                        new ProfileDrawerItem().withName(user.getFirstName() + " " + user.getLastName())
                                .withEmail(account.name)
                                .withIcon(user.getAvatarUrl()),
                        new ProfileDrawerItem().withName(mContext.getString(R.string.log_out))
                                .withIcon(R.drawable.exit_icon)
                                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                                    @Override
                                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                        EvendateAccountManager.deleteAccount(mContext);
                                        return false;
                                    }
                                })
                );
            }

            @Override
            public void onError() {

            }
        });
    }

    public static EvendateDrawer newInstance(Activity context) {

        //create the drawer and remember the `Drawer` result object
        DrawerBuilder result = new DrawerBuilder()
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        return true;
                    }
                });
        AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(context)
                .withCompactStyle(false)
                .withHeaderBackground(R.drawable.gradient_profile)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .withAlternativeProfileHeaderSwitching(false)
                .withOnlySmallProfileImagesVisible(false)
                .withProfileImagesClickable(false)
                .withOnlyMainProfileImageVisible(true)
                .build();
        result.withActivity(context)
                .withAccountHeader(headerResult);
        EvendateDrawer drawer = new EvendateDrawer(result.build(), headerResult, context);
        drawer.setupMenu();

        if (Build.VERSION.SDK_INT >= 19) {
            drawer.getDrawer().getDrawerLayout().setFitsSystemWindows(false);
        }

        return drawer;
    }

    public void setupMenu() {
        mDrawer.removeAllItems();
        mDrawer.addItems(
                reelItem,
                calendarItem,
                organizationsItem,
                new SectionDrawerItem().withName(R.string.subscriptions)
        );
    }

    public AccountHeader getAccountHeader() {
        return mAccountHeader;
    }

    private void updateSubs() {
        setupMenu();
        for (Organization detail : mSubscriptions) {
            mDrawer.addItem(new SubscriptionDrawerItem().withName(detail.getName())
                    .withIcon(detail.getLogoUrl()).withTag(detail).withSelectable(false));
        }
    }

    protected Drawer getDrawer() {
        return mDrawer;
    }

    @Override
    public void onLoaded(ArrayList<Organization> subList) {
        mSubscriptions = subList;
        updateSubs();
    }

    public void update() {
        mSubscriptionLoader.startLoading();
    }

    @Override
    public void onError() {
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

    public void cancel() {
        mMeLoader.cancelLoad();
        mSubscriptionLoader.cancelLoad();
    }

    public void start() {
        mSubscriptionLoader.startLoading();
        mMeLoader.startLoading();
    }
}
