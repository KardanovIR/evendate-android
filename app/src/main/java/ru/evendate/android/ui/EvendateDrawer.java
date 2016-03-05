package ru.evendate.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

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
import ru.evendate.android.loaders.SubscriptionLoader;
import ru.evendate.android.models.OrganizationModel;

/**
 * Created by Dmitry on 11.02.2016.
 */
public class EvendateDrawer implements LoaderListener<ArrayList<OrganizationModel>> {
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;
    private SubscriptionLoader mSubscriptionLoader;
    ArrayList<OrganizationModel> mSubscriptions;
    final int REEL_IDENTIFIER = 1;
    final int CALENDAR_IDENTIFIER = 2;
    final int ORGANIZATION_IDENTIFIER = 3;

    PrimaryDrawerItem reel_item = new PrimaryDrawerItem().withName(R.string.reel)
            .withIcon(R.drawable.event_icon).withIdentifier(REEL_IDENTIFIER).withSelectable(true);
    PrimaryDrawerItem calendar_item = new PrimaryDrawerItem().withName(R.string.calendar)
            .withIcon(R.drawable.calendar_icon).withIdentifier(CALENDAR_IDENTIFIER).withSelectable(true);
    PrimaryDrawerItem organizations_item = new PrimaryDrawerItem().withName(R.string.organizations)
            .withIcon(R.drawable.organization_icon).withIdentifier(ORGANIZATION_IDENTIFIER).withSelectable(true);
    //PrimaryDrawerItem item = new PrimaryDrawerItem().withName(R.string.reel);
    //PrimaryDrawerItem item = new PrimaryDrawerItem().withName(R.string.reel);

    protected EvendateDrawer(Drawer drawer, AccountHeader accountHeader, Context context) {
        mDrawer = drawer;
        mAccountHeader = accountHeader;
        mSubscriptionLoader = new SubscriptionLoader(context);
        mSubscriptionLoader.setLoaderListener(this);
        mSubscriptionLoader.getSubscriptions();
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
                .addProfiles(
                        new ProfileDrawerItem().withName("Deniz Ozdemir")
                                .withEmail("withoutOlezhka@evendate.ru")
                                .withIcon(context.getResources().getDrawable(R.mipmap.ic_launcher))
                )
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
            mDrawer.addItem(new SubscriptionDrawerItem().withName(detail.getName()).withIcon(detail.getLogoUrl()));
        }
    }

    private class NavigationItemSelectedListener
            implements Drawer.OnDrawerItemClickListener{
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()){
                case REEL_IDENTIFIER:{

                }
            }
            return false;
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
    /*
    private class MainNavigationItemSelectedListener
            implements Drawer.OnDrawerItemClickListener{
        private Context mContext;

        public MainNavigationItemSelectedListener(Context context) {
            mContext = context;
        }
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            drawerItem.withSetSelected(true);
            switch (drawerItem.) {
                case R.id.reel:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof MainPagerFragment)){
                        mFragment = new MainPagerFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                        drawerLayout.closeDrawers();
                    }
                    checkedMenuItemId = menuItem.getItemId();
                    menuItem.setChecked(true);
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(0f);
                    return true;
                }
                case R.id.settings:
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.calendar:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof CalendarFragment)){
                        mFragment = new CalendarFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                        drawerLayout.closeDrawers();
                    }
                    checkedMenuItemId = menuItem.getItemId();
                    menuItem.setChecked(true);
                    // Converts 4 dip into its equivalent px
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(px);
                    return true;
                }
                case R.id.organizations:{
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    if(!(mFragment instanceof OrganizationCatalogFragment)){
                        mFragment = new OrganizationCatalogFragment();
                        fragmentManager.beginTransaction().replace(R.id.main_content, mFragment).commit();
                        drawerLayout.closeDrawers();
                    }
                    checkedMenuItemId = menuItem.getItemId();
                    menuItem.setChecked(true);
                    // Converts 4 dip into its equivalent px
                    float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
                    if (Build.VERSION.SDK_INT >= 21)
                        mToolbar.setElevation(px);
                    return true;
                }
                case R.id.nav_add_account:
                    Intent authIntent = new Intent(mContext, AuthActivity.class);
                    startActivity(authIntent);
                    drawerLayout.closeDrawers();
                    return true;
                case R.id.nav_account_management:
                    Intent intent = new Intent(Settings.ACTION_SYNC_SETTINGS);
                    //TODO не робит(
                    intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.content_authority));
                    intent.putExtra(Settings.EXTRA_AUTHORITIES, getResources().getString(R.string.account_type));
                    startActivity(intent);
                    drawerLayout.closeDrawers();
                    return true;
                default:
                    if(!mAccountToggle.isChecked()){
                        //open organization from subs
                        Intent detailIntent = new Intent(mContext, OrganizationDetailActivity.class);
                        detailIntent.setData(EvendateContract.OrganizationEntry.CONTENT_URI
                                .buildUpon().appendPath(Long.toString(menuItem.getItemId())).build());
                        startActivity(detailIntent);
                    }
                    else{
                        //change account
                        //TODO does it work? Need to remove this fucking code with direct operating on SharedPref
                        //String account_name = String.valueOf(menuItem.getTitle());
                        //SharedPreferences sPref = getSharedPreferences(EvendateAuthenticator.ACCOUNT_PREFERENCES, MODE_PRIVATE);
                        //SharedPreferences.Editor ed = sPref.edit();
                        //ed.putString(EvendateAuthenticator.ACTIVE_ACCOUNT_NAME, account_name);
                        //ed.apply();
                        //setAccountInfo();
                        mAccountAdapter.updateAccountMenu();
                    }
                    drawerLayout.closeDrawers();
                    return true;
            }
        }
    }
    */
}
