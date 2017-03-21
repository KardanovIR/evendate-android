package ru.evendate.android.ui.cities;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.parceler.Parcels;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.City;

import static android.app.Activity.RESULT_OK;
import static com.google.common.base.Preconditions.checkNotNull;
import static ru.evendate.android.ui.cities.CityFragment.KEY_CITY;

/**
 * Created by Aedirn on 13.03.17.
 */

class CityPresenter implements CityContract.Presenter, LocationFragment.OnLocationListener {
    @NonNull
    private final CityContract.View mCityView;
    @NonNull
    private final LocationFragment mLocationFragment;
    private String LOG_TAG = CityPresenter.class.getSimpleName();
    @NonNull
    private DataRepository mDataRepository;
    private Disposable mDisposable;
    @Nullable
    private City mSelectedCity;
    private List<City> mCities;
    private Activity mContext;

    CityPresenter(Activity context, @NonNull DataRepository dataRepository,
                  @NonNull CityContract.View cityView, @NonNull LocationFragment locationFragment) {
        mContext = context;
        mDataRepository = checkNotNull(dataRepository);
        mCityView = checkNotNull(cityView);
        mCityView.setPresenter(this);
        mLocationFragment = locationFragment;
        mLocationFragment.setListener(this);
    }

    @Override
    public void start() {
        loadCities();
    }

    @Override
    public void stop() {
        mDisposable.dispose();
    }

    @Override
    public void loadCities() {
        mCityView.setLoadingIndicator(true);
        mDisposable = mDataRepository.getCities().subscribe(
                result -> {
                    mCities = result.getData();
                    pushUpSelectedCity(mCities);
                    mCityView.showCities(mCities);
                },
                this::onError,
                () -> mCityView.setLoadingIndicator(false)
        );
    }

    private void pushUpSelectedCity(List<City> list) {
        if (mSelectedCity == null)
            return;
        for (City city : list) {
            if (city.getEntryId() == mSelectedCity.getEntryId()) {
                list.remove(city);
                list.add(0, city);
                break;
            }
        }
    }

    @Override
    public void getCurrentLocation() {
        mLocationFragment.getLocation();
    }

    /**
     * check city in evendate list and select it
     */
    @Override
    public void onRecognizeAddress(Location location, Address address) {
        for (City city : mCities) {
            if (city.getNameLocally().equals(address.getLocality()) ||
                    city.getName().equals(address.getLocality())) {
                selectCity(city);
                //pushUpSelectedCity(mCities);
                updateSelectedCity();
                return;
            }
        }
        loadNearestCities(location);
    }

    @Override
    public void onRecognizeAddressFail(Location location) {
        loadNearestCities(location);
    }

    @Override
    public void loadNearestCities(Location location) {
        mDisposable = mDataRepository.getNearestCities(location.getLatitude(), location.getLongitude()).subscribe(
                result -> {
                    mSelectedCity = result.getData().get(0);
                    pushUpSelectedCity(mCities);
                    mCityView.showCities(mCities);
                    updateSelectedCity();
                }
                //todo on error
        );
    }

    @Override
    public void updateSelectedCity() {
        //todo solid
        EvendatePreferences.newInstance(mContext).putUserCity(mSelectedCity);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_CITY, Parcels.wrap(mSelectedCity));
        mContext.setResult(RESULT_OK, resultIntent);
        mContext.finish();
    }

    @Override
    public void selectCity(City city) {
        mSelectedCity = city;
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mCityView.showError();
    }

}
