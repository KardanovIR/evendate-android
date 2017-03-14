package ru.evendate.android.ui.cities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.R;
import ru.evendate.android.models.City;
import ru.evendate.android.views.LoadStateView;

import static com.google.common.base.Preconditions.checkNotNull;

public class CityFragment extends Fragment implements CityContract.View,
        CityContract.OnCityInteractionListener {

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

    class CityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private final CityContract.OnCityInteractionListener mListener;
        private List<City> mCities = new ArrayList<>();

        CityRecyclerViewAdapter(CityContract.OnCityInteractionListener listener) {
            mListener = listener;
        }

        public void set(List<City> cities) {
            this.mCities = cities;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_city, parent, false);
                return new CityViewHolder(view);
            } else if (viewType == TYPE_HEADER) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_city_header, parent, false);
                return new HeaderViewHolder(view);
            } else
                throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CityViewHolder) {
                CityViewHolder castHolder = (CityViewHolder) holder;
                castHolder.mItem = mCities.get(position - 1);
                castHolder.mCityTitle.setText(mCities.get(position - 1).getNameLocally());

                castHolder.mView.setOnClickListener((View v) -> {
                    if (null != mListener) {
                        mListener.onCityChanged(castHolder.mItem);
                    }
                });
            } else {
                HeaderViewHolder castHolder = (HeaderViewHolder) holder;
                castHolder.mLocationButton.setOnClickListener((View v) -> {
                    if (null != mListener) {
                        mListener.onLocationClicked();
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return TYPE_HEADER;
            return TYPE_ITEM;
        }

        @Override
        public int getItemCount() {
            return mCities.size() + 1;
        }

        class CityViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            @Bind(R.id.city_title) TextView mCityTitle;
            City mItem;

            CityViewHolder(View view) {
                super(view);
                mView = view;
                ButterKnife.bind(this, view);
            }
        }

        class HeaderViewHolder extends RecyclerView.ViewHolder {
            final View mView;
            @Bind(R.id.location_button) LinearLayout mLocationButton;

            HeaderViewHolder(View view) {
                super(view);
                mView = view;
                ButterKnife.bind(this, view);
            }
        }
    }

}
