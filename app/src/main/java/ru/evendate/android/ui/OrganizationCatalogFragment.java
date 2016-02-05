package ru.evendate.android.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Arrays;

import ru.evendate.android.R;
import ru.evendate.android.sync.LocalDataFetcher;

/**
 * Created by Dmitry on 28.01.2016.
 */
public class OrganizationCatalogFragment extends Fragment
        implements OrganizationFilterDialog.OnCategorySelectListener,
        View.OnClickListener{
    private android.support.v7.widget.RecyclerView mRecyclerView;
    private OrganizationCategoryAdapter mAdapter;
    private boolean[] mSelectedItems;

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization_catalog, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        mAdapter = new OrganizationCategoryAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setCategoryList(initDummy());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        rootView.findViewById(R.id.button).setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        if(mSelectedItems == null){
            mSelectedItems = new boolean[initDummy().size()];
            Arrays.fill(mSelectedItems, Boolean.TRUE);
        }
        OrganizationFilterDialog dialog = OrganizationFilterDialog.newInstance(initDummy(), mSelectedItems);
        dialog.setCategorySelectListener(this);
        dialog.show(getChildFragmentManager(), "OrganizationFilterDialog");
    }

    private ArrayList<String> initDummy(){
        String[] Dummy = {"category1", "category2", "category3", "category4", "category5",
                "category6", "category7", "category8", "category9", "category10"};
        ArrayList<String> items = new ArrayList<>();
        items.addAll(Arrays.asList(Dummy));
        return items;
    }

    @Override
    public void onCategorySelected(boolean[] itemsSelected) {
        ArrayList<String> itemSelectedStr = new ArrayList<>();
        ArrayList<String> strings = initDummy();
        for(int i = 0; i < itemsSelected.length; i++){
            if(itemsSelected[i])
                itemSelectedStr.add(strings.get(i));
        }
        mAdapter.setCategoryList(itemSelectedStr);
    }
}
