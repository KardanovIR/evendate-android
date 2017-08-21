package ru.evendate.android.ui.cities;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.ui.BaseActivity;

public class CityActivity extends BaseActivity implements CityPromptDialog.PromptInteractionListener {

    public final static String KEY_PROMPT = "force_prompt";
    public final static String TAG_LOCATION = "location";
    private final static String TAG_PROMPT = "tag_prompt";
    private final static String TAG_CITIES = "tag_cities";
    @BindView(R.id.toolbar) Toolbar mToolbar;
    boolean shouldShowPrompt = false;
    CityPresenter mCityPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city);
        ButterKnife.bind(this);

        if (getIntent() != null)
            shouldShowPrompt = getIntent().getBooleanExtra(KEY_PROMPT, false);

        initToolbar();

        LocationFragment locationFragment = (LocationFragment)getSupportFragmentManager()
                .findFragmentByTag(TAG_LOCATION);
        if (locationFragment == null) {
            locationFragment = new LocationFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(locationFragment, TAG_LOCATION).commit();
        }

        CityFragment cityFragment = (CityFragment)getSupportFragmentManager()
                .findFragmentByTag(TAG_CITIES);
        if (cityFragment == null) {
            cityFragment = new CityFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, cityFragment, TAG_CITIES).commit();
        }

        mCityPresenter = new CityPresenter(this, new DataRepository(this),
                cityFragment, locationFragment);

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
                remove(getSupportFragmentManager().findFragmentByTag(TAG_PROMPT)).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE).commit();
    }

}
