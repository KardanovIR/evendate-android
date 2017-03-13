package ru.evendate.android.ui.cities;

import android.location.Address;
import android.location.Location;

import java.util.List;

import ru.evendate.android.models.City;
import ru.evendate.android.ui.BasePresenter;
import ru.evendate.android.ui.BaseView;

/**
 * Created by Aedirn on 13.03.17.
 */

public interface CityContract {

    interface View extends BaseView<Presenter> {
        void setLoadingIndicator(boolean active);

        void showCities(List<City> cities);

        void showError();
    }

    interface Presenter extends BasePresenter {
        void loadCities();

        void loadNearestCities(Location location);

        void checkCity(Location location, Address address);

        void selectCity(City city);

        void updateSelectedCity();
    }
}
