package ru.evendate.android.ui;

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

import ru.evendate.android.R;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.MeLoader;
import ru.evendate.android.loaders.SubscriptionLoader;
import ru.evendate.android.models.OrganizationModel;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 11.02.2016.
 */
public class EvendateDrawer implements LoaderListener<ArrayList<OrganizationModel>> {
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;
    private SubscriptionLoader mSubscriptionLoader;
    ArrayList<OrganizationModel> mSubscriptions;
    private MeLoader mMeLoader;
    final static int REEL_IDENTIFIER = 1;
    final static int CALENDAR_IDENTIFIER = 2;
    final static int ORGANIZATION_IDENTIFIER = 3;
    Context mContext;

    PrimaryDrawerItem reel_item = new PrimaryDrawerItem().withName(R.string.reel)
            .withIcon(R.drawable.event_icon).withIdentifier(REEL_IDENTIFIER).withSelectable(true);
    PrimaryDrawerItem calendar_item = new PrimaryDrawerItem().withName(R.string.calendar)
            .withIcon(R.drawable.calendar_icon).withIdentifier(CALENDAR_IDENTIFIER).withSelectable(true);
    PrimaryDrawerItem organizations_item = new PrimaryDrawerItem().withName(R.string.organizations)
            .withIcon(R.drawable.organization_icon).withIdentifier(ORGANIZATION_IDENTIFIER).withSelectable(true);
    //PrimaryDrawerItem item = new PrimaryDrawerItem().withName(R.string.reel);
    //PrimaryDrawerItem item = new PrimaryDrawerItem().withName(R.string.reel);

    protected EvendateDrawer(Drawer drawer, AccountHeader accountHeader, Context context) {
        mContext = context;
        mDrawer = drawer;
        mAccountHeader = accountHeader;
        mSubscriptionLoader = new SubscriptionLoader(context);
        mSubscriptionLoader.setLoaderListener(this);
        mSubscriptionLoader.getSubscriptions();
        mMeLoader = new MeLoader(context);
        mMeLoader.setLoaderListener(new LoaderListener<UserDetail>() {
            @Override
            public void onLoaded(UserDetail user) {
                getAccountHeader().addProfiles(
                        new ProfileDrawerItem().withName(user.getFirstName() + " " + user.getLastName())
                                //.withEmail()
                                .withIcon(user.getAvatarUrl())
                );
            }

            @Override
            public void onError() {

            }
        });
        mMeLoader.getData();
    }

    public static EvendateDrawer newInstance(Activity context){

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
                .withHeaderBackground(R.drawable.default_background)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();
        result.withActivity(context)
                .withAccountHeader(headerResult);
        EvendateDrawer drawer =  new EvendateDrawer(result.build(), headerResult, context);
        drawer.setupMenu();

        if (Build.VERSION.SDK_INT >= 19) {
            drawer.getDrawer().getDrawerLayout().setFitsSystemWindows(false);
        }
        return drawer;
    }
    public void setupMenu(){
        mDrawer.removeAllItems();
        mDrawer.addItems(
                reel_item,
                calendar_item,
                organizations_item,
                new SectionDrawerItem().withName(R.string.subscriptions)
        );
    }
    public AccountHeader getAccountHeader(){
        return mAccountHeader;
    }
    private void updateSubs(){
        setupMenu();
        for (OrganizationModel detail: mSubscriptions) {
            mDrawer.addItem(new SubscriptionDrawerItem().withName(detail.getName())
                    .withIcon(detail.getLogoUrl()).withTag(detail).withSelectable(false));
        }
    }

    protected Drawer getDrawer(){
        return mDrawer;
    }
    @Override
    public void onLoaded(ArrayList<OrganizationModel> subList) {
        mSubscriptions = subList;
        updateSubs();
    }

    public void update(){
        mSubscriptionLoader.getSubscriptions();
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
}
