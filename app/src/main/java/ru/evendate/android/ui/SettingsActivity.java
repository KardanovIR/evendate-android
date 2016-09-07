package ru.evendate.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import ru.evendate.android.BuildConfig;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.Settings;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsActivity extends AppCompatActivity {
    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    private DrawerWrapper mDrawer;

    public static final String KEY_NOTIFICATION = "key_notification";
    public static final boolean KEY_NOTIFICATION_DEFAULT = true;
    public static final String KEY_INDICATOR = "key_indicator";
    public static final boolean KEY_INDICATOR_DEFAULT = true;
    public static final String KEY_VIBRATION = "key_vibration";
    public static final boolean KEY_VIBRATION_DEFAULT = true;
    public static final String KEY_INDICATOR_COLOR = "key_indicator_color";
    public static final int KEY_INDICATOR_COLOR_DEFAULT = 0xffff17a8;
    public static final String KEY_HTTPS = "key_https";
    public static final boolean KEY_HTTPS_DEFAULT = false;

    private static final String KEY_PRIVACY_FEED = "key_indicator_color";
    private static final String KEY_INFO = "key_info";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        initToolbar();
        initDrawer();

        getFragmentManager().beginTransaction().replace(R.id.main_content, new SettingsFragment()).commit();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_menu_white);
        toolbar.setNavigationOnClickListener((View v) -> mDrawer.getDrawer().openDrawer());
    }
    private void initDrawer(){
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new SettingsNavigationItemClickListener(this, mDrawer.getDrawer()));
    }
    private class SettingsNavigationItemClickListener extends NavigationItemSelectedListener {

        public SettingsNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
                case DrawerWrapper.SETTINGS_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.getDrawer().setSelection(DrawerWrapper.SETTINGS_IDENTIFIER);
        mDrawer.start();
    }

    public static class SettingsFragment extends PreferenceFragment {
        Context context;
        private int indicatorColor;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.context = context;
        }

        @Override
        public void onCreate(final Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);

            CheckBoxPreference feedPrivacyPreference = (CheckBoxPreference)getPreferenceScreen().findPreference(KEY_PRIVACY_FEED);

            feedPrivacyPreference.setOnPreferenceChangeListener((Preference preference, Object newValue) -> {
                updateFeedPrivacy((boolean)newValue);
                return true;
            });
            ApiService apiService = ApiFactory.getService(context);
            Observable<ResponseArray<Settings>> notificationObservable =
                    apiService.getSettings(EvendateAccountManager.peekToken(context));

            //todo refactor and add a progress bar
            notificationObservable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                        if(result.isOk())
                            feedPrivacyPreference.setChecked(result.getData().get(0).isFeedShowedToFriend());
                    }, error -> {
                        Log.e(LOG_TAG, error.getMessage());
                    });

            Preference dialogPreference = getPreferenceScreen().findPreference(KEY_INFO);
            dialogPreference.setOnPreferenceClickListener((Preference preference) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AlertDialogCustom);

                builder.setTitle(R.string.settings_dialog_about);
                builder.setMessage(getString(R.string.settings_dialog_about_version) + " " + BuildConfig.VERSION_NAME);
                //LayoutInflater factory = LayoutInflater.from(context);
                //final View view = factory.inflate(R.layout.dialog_version, null);
                //builder.setView(view);
                builder.setIcon(R.drawable.evendate_logo);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            });


            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            indicatorColor = sp.getInt(SettingsActivity.KEY_INDICATOR_COLOR, KEY_INDICATOR_COLOR_DEFAULT);

            Preference colorPreference = getPreferenceScreen().findPreference(KEY_INDICATOR_COLOR);
            colorPreference.setOnPreferenceClickListener((Preference preference) -> {
                ColorPickerDialogBuilder
                        .with(context, R.style.AlertDialogCustom)
                        .noSliders()
                        .setTitle(R.string.settings_dialog_led_color)
                        .initialColor(indicatorColor)
                        .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                        .density(10)
                        .setPositiveButton(getString(R.string.dialog_ok),
                                (DialogInterface dialog, int selectedColor, Integer[] allColors) -> {
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putInt(KEY_INDICATOR_COLOR, selectedColor);
                            editor.commit();
                            indicatorColor = selectedColor;
                        })
                        .build()
                        .show();
                return true;
            });
        }

        private void updateFeedPrivacy(boolean newValue){
            ApiService apiService = ApiFactory.getService(context);
            Observable<Response> notificationObservable =
                    apiService.setSettings(EvendateAccountManager.peekToken(context), newValue);

            notificationObservable.subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(result -> {
                    }, error -> {
                        Log.e(LOG_TAG, error.getMessage());
                    });
        }
    }

}