package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;

import ru.evendate.android.R;
import ru.evendate.android.loaders.CatalogLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.OrganizationType;

/**
 * Created by Dmitry on 28.01.2016.
 */
public class OrganizationCatalogFragment extends Fragment
        implements OrganizationFilterDialog.OnCategorySelectListener,
        View.OnClickListener{
    private android.support.v7.widget.RecyclerView mRecyclerView;
    private CatalogLoader mLoader;
    private OrganizationCategoryAdapter mAdapter;
    private boolean[] mSelectedItems;
    private ArrayList<OrganizationType> mCategoryList;
    private FloatingActionButton mFAB;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization_catalog, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        mAdapter = new OrganizationCategoryAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLoader = new CatalogLoader(getActivity());
        mLoader.setLoaderListener(new LoaderListener<ArrayList<OrganizationType>>() {
            @Override
            public void onLoaded(ArrayList<OrganizationType> subList) {
                mCategoryList = subList;
                mAdapter.setCategoryList(subList);
            }

            @Override
            public void onError() {
                if (isAdded())
                    return;
                AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLoader.getData();
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }
        });

        mFAB = (FloatingActionButton) rootView.findViewById((R.id.fab));
        mFAB.setOnClickListener(this);
        mFAB.setImageDrawable(this.getResources().getDrawable(R.drawable.ic_filter_list_white_48dp));
        mLoader.getData();
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(v == mFAB) {
            if(mCategoryList == null)
                return;
            if(mSelectedItems == null){
                mSelectedItems = new boolean[mCategoryList.size()];
                Arrays.fill(mSelectedItems, Boolean.TRUE);
            }
            OrganizationFilterDialog dialog = OrganizationFilterDialog
                    .newInstance(mCategoryList, mSelectedItems);
            dialog.setCategorySelectListener(this);
            dialog.show(getChildFragmentManager(), "OrganizationFilterDialog");
        }
    }

    @Override
    public void onCategorySelected(boolean[] itemsSelected) {
        ArrayList<OrganizationType> newItemSelected = new ArrayList<>();
        mSelectedItems = itemsSelected;
        for(int i = 0; i < itemsSelected.length; i++){
            if(itemsSelected[i])
                newItemSelected.add(mCategoryList.get(i));
        }
        mAdapter.setCategoryList(newItemSelected);
    }
}
