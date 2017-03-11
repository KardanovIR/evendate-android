package ru.evendate.android.ui.cities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import org.parceler.Parcels;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.City;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.views.LoadStateView;
import rx.Subscription;

public class CityActivity extends BaseActivity implements
        CityRecyclerViewAdapter.OnCityInteractionListener, LocationListener,
        CityPromptDialog.PromptInteractionListener {
    private String LOG_TAG = CityActivity.class.getSimpleName();

    public final static String KEY_CITY = "city";
    public final static String KEY_PROMPT = "force_prompt";
    private final static String TAG_PROMPT = "tag_prompt";
    List<City> mCities;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    City mSelectedCity;
    Subscription mSubscription;
    CityRecyclerViewAdapter mAdapter;
    @Bind(R.id.load_state) LoadStateView mLoadState;
    private LocationManager mLocationManager;
    private String mBestProvider;
    boolean shouldShowPrompt = false;

    private final static int PERMISSIONS_REQUEST_LOCATION = 1;
    private final static int REQUEST_CHECK_SETTINGS = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        ButterKnife.bind(this);

        if (getIntent() != null)
            shouldShowPrompt = getIntent().getBooleanExtra(KEY_PROMPT, false);

        initToolbar();

        mSelectedCity = EvendatePreferences.newInstance(this).getUserCity();
        mAdapter = new CityRecyclerViewAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        mLoadState.setOnReloadListener(this::loadCities);
        loadCities();

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        mBestProvider = mLocationManager.getBestProvider(criteria, false);
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        mToolbar.setNavigationOnClickListener((View v) -> onUpPressed());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (shouldShowPrompt) {

            CityPromptDialog fragment = new CityPromptDialog();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.main_content, fragment, TAG_PROMPT)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
        }
    }

    @Override
    public void onCityChanged(City item) {
        mSelectedCity = item;
        EvendatePreferences.newInstance(this).putUserCity(item);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_CITY, Parcels.wrap(mSelectedCity));
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onLocationClicked() {
        getLocation();
    }

    void requestPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            //if (ActivityCompat.shouldShowRequestPermissionRationale(this,
            //        Manifest.permission.ACCESS_FINE_LOCATION)) {
            //todo
            // Show an explanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

            //} else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
            //}
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }

    private void getLocation() {
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }

        Location location = null;
        if (mBestProvider != null)
            location = mLocationManager.getLastKnownLocation(mBestProvider);
        if (location == null)
            location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location == null)
            location = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location == null)
            location = mLocationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (location == null) {
            promptUserToEnableLocation();
            return;
        }
        onLocationReceived(location);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
    }

    private void promptUserToEnableLocation() {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient

        PendingResult<LocationSettingsResult> checkingLocationSettingIntent =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        checkingLocationSettingIntent.setResultCallback((LocationSettingsResult result) -> {
            final Status status = result.getStatus();
            final LocationSettingsStates state = result.getLocationSettingsStates();
            switch (status.getStatusCode()) {
                case LocationSettingsStatusCodes.SUCCESS:
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    break;
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    // Location settings are not satisfied. But could be fixed by showing the user
                    // a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        startResolution(status);
                    } catch (IntentSender.SendIntentException e) {
                        // Ignore the error.
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    // Location settings are not satisfied. However, we have no way to fix the
                    // settings so we won't show the dialog.
                    break;
            }
        });
    }

    private void startResolution(Status status) throws IntentSender.SendIntentException {
        status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        int permissionCheck = ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_FINE_LOCATION);
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, this);
                            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 400, 1, this);
                            mLocationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, 400, 1, this);
                        }
                        break;
                    case RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    private void onLocationReceived(Location location) {

        Log.d(LOG_TAG, "onLocationReceived: " + location);
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        StringBuilder builder = new StringBuilder();
        try {
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
            if (address.size() == 0) {
                loadNearestCities(location);
                return;
            }
            int maxLines = address.get(0).getMaxAddressLineIndex();
            for (int i = 0; i < maxLines; i++) {
                String addressStr = address.get(0).getAddressLine(i);
                builder.append(addressStr);
                builder.append(" ");
            }

            String fnialAddress = builder.toString(); //This is the complete address.
            checkCity(location, address.get(0));
        } catch (IOException e) {
            loadNearestCities(location);
        }
    }

    private void checkCity(Location location, Address address) {
        for (City city : mCities) {
            if (city.getNameLocally().equals(address.getLocality()) ||
                    city.getName().equals(address.getLocality())) {
                onCityChanged(city);
                return;
            }
        }
        loadNearestCities(location);
    }

    private void loadCities() {
        mLoadState.showProgress();
        DataRepository dataRepository = new DataRepository(this);
        mSubscription = dataRepository.getCities().subscribe(
                result -> onLoaded(result.getData()),
                this::onError,
                mLoadState::hideProgress
        );
    }

    private void loadNearestCities(Location location) {
        DataRepository dataRepository = new DataRepository(this);
        mSubscription = dataRepository.getNearestCities(location.getLatitude(), location.getLongitude()).subscribe(
                result -> onLoadedNearest(result.getData())
        );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }

    public void onLoaded(List<City> list) {
        if (mSelectedCity != null)
            list = pushUpSelectedCity(list);
        mCities = list;
        mAdapter.set(list);
        mAdapter.notifyDataSetChanged();
    }

    public void onLoadedNearest(List<City> list) {
        //todo ask comfirm??
        onCityChanged(list.get(0));
    }

    private List<City> pushUpSelectedCity(List<City> list) {
        for (City city : list) {
            if (city.getEntryId() == mSelectedCity.getEntryId()) {
                list.remove(city);
                list.add(0, city);
                break;
            }
        }
        return list;
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, error.getMessage());
        mLoadState.showErrorHint();
    }

    @Override
    public void onLocationChanged(Location location) {
        onLocationReceived(location);
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onPlaceButtonClick() {
        getSupportFragmentManager().beginTransaction().
                remove(getSupportFragmentManager().findFragmentByTag(TAG_PROMPT)).commit();
    }


}
