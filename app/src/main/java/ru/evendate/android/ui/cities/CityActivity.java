package ru.evendate.android.ui.cities;

import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.ui.BaseActivity;

public class CityActivity extends BaseActivity implements CityPromptDialog.PromptInteractionListener {

    public final static String KEY_PROMPT = "force_prompt";
    public final static String TAG_LOCATION = "location";
    private final static String TAG_PROMPT = "tag_prompt";
    @Bind(R.id.toolbar) Toolbar mToolbar;
    boolean shouldShowPrompt = false;
    LocationFragment mLocationFragment;
    CityContract.Presenter mCityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        ButterKnife.bind(this);

        if (getIntent() != null)
            shouldShowPrompt = getIntent().getBooleanExtra(KEY_PROMPT, false);

        initToolbar();

        mLocationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag(TAG_LOCATION);
        if (mLocationFragment == null) {
            mLocationFragment = LocationFragment.newInstance(new LocationFragment.OnLocationListener() {
                @Override
                public void onRecognizeAddress(Location location, Address address) {
                    mCityPresenter.checkCity(location, address);
                }

                @Override
                public void onRecognizeAddressFail(Location location) {
                    mCityPresenter.loadNearestCities(location);
                }
            });
            getSupportFragmentManager().beginTransaction().add(mLocationFragment, TAG_LOCATION).commit();
        }
        CityFragment cityFragment = (CityFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        if (cityFragment == null) {
            cityFragment = new CityFragment();

            //todo not mvp?
            cityFragment.setLocationButtonListener(() -> mLocationFragment.getLocation());
            getSupportFragmentManager().beginTransaction().add(R.id.container, cityFragment).commit();
        }
        mCityPresenter = new CityPresenter(this, new DataRepository(this), cityFragment);


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
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
    }


    @Override
    public void onPlaceButtonClick() {
        getSupportFragmentManager().beginTransaction().
                remove(getSupportFragmentManager().findFragmentByTag(TAG_PROMPT)).commit();
    }


}
