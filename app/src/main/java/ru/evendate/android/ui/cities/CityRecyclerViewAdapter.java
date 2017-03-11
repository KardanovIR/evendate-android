package ru.evendate.android.ui.cities;

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
import ru.evendate.android.R;
import ru.evendate.android.models.City;

public class CityRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final OnCityInteractionListener mListener;
    private List<City> mCities = new ArrayList<>();

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    CityRecyclerViewAdapter(OnCityInteractionListener listener) {
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

    interface OnCityInteractionListener {
        void onCityChanged(City item);

        void onLocationClicked();
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
