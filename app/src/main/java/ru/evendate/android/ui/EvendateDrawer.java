package ru.evendate.android.ui;

import android.app.Activity;
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
import ru.evendate.android.sync.models.OrganizationModel;

/**
 * Created by Dmitry on 11.02.2016.
 */
public class EvendateDrawer{
    private Drawer mDrawer;
    private AccountHeader mAccountHeader;


    PrimaryDrawerItem reel_item = new PrimaryDrawerItem().withName(R.string.reel)
            .withIcon(R.drawable.event_icon);
    PrimaryDrawerItem calendar_item = new PrimaryDrawerItem().withName(R.string.calendar)
            .withIcon(R.drawable.calendar_icon);
    PrimaryDrawerItem organizations_item = new PrimaryDrawerItem().withName(R.string.organizations)
            .withIcon(R.drawable.organization_icon);
    //PrimaryDrawerItem item = new PrimaryDrawerItem().withName(R.string.reel);
    //PrimaryDrawerItem item = new PrimaryDrawerItem().withName(R.string.reel);

    protected EvendateDrawer(Drawer drawer, AccountHeader accountHeader) {
        mDrawer = drawer;
        mAccountHeader = accountHeader;
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
        EvendateDrawer drawer =  new EvendateDrawer(result.build(), headerResult);
        drawer.setupMenu();
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
    public void updateSubs(ArrayList<OrganizationModel> subList){
        setupMenu();
        for (OrganizationModel detail: subList) {
            mDrawer.addItem(new ProfileDrawerItem().withName(detail.getName()).withIcon(detail.getLogoUrl()));
        }
    }

    private class NavigationItemSelectedListener
            implements Drawer.OnDrawerItemClickListener{
        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            return false;
        }
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
