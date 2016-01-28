package ru.evendate.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.evendate.android.R;
import ru.evendate.android.sync.LocalDataFetcher;

/**
 * Created by Dmitry on 28.01.2016.
 */
public class OrganizationCatalogFragment extends Fragment{
    private android.support.v7.widget.RecyclerView mRecyclerView;
    private OrganizationCategoryAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization_catalog, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        mAdapter = new OrganizationCategoryAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        LocalDataFetcher localDataFetcher = new LocalDataFetcher(getActivity().getContentResolver(), getActivity());
        mAdapter.setCategoryList(localDataFetcher.getOrganizationCategoriesDataFromDB());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        return rootView;
    }
}
