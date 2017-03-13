package ru.evendate.android.ui.cities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.models.City;
import ru.evendate.android.views.LoadStateView;

import static com.google.common.base.Preconditions.checkNotNull;

public class CityFragment extends Fragment implements CityContract.View,
        CityRecyclerViewAdapter.OnCityInteractionListener {

    public final static String KEY_CITY = "city";
    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    @Bind(R.id.load_state) LoadStateView mLoadState;
    CityRecyclerViewAdapter mAdapter;
    private CityContract.Presenter mPresenter;

    @Override
    public void setPresenter(@NonNull CityContract.Presenter presenter) {
        mPresenter = checkNotNull(presenter);
    }

    @Override
    public void onLocationClicked() {
        mPresenter.getCurrentLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_city, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new CityRecyclerViewAdapter(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setAdapter(mAdapter);

        mLoadState.setOnReloadListener(mPresenter::loadCities);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.selectCity(EvendatePreferences.newInstance(getContext()).getUserCity());
        mPresenter.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.stop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active || mAdapter.getItemCount() == 0)
            mLoadState.showProgress();
        else {
            mLoadState.hideProgress();
        }
    }

    @Override
    public void showError() {
        mLoadState.showErrorHint();
        //todo anim
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showCities(List<City> cities) {
        mAdapter.set(cities);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCityChanged(City item) {
        mPresenter.selectCity(item);
        mPresenter.updateSelectedCity();
    }
}
