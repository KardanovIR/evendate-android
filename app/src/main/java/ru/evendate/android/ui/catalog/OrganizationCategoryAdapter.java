package ru.evendate.android.ui.catalog;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.WrapLinearLayoutManager;

/**
 * Created by Dmitry on 30.11.2015.
 */
public class OrganizationCategoryAdapter extends AbstractAdapter<OrganizationCategory, OrganizationCategoryAdapter.CategoryHolder> {

    private RecyclerView.RecycledViewPool mRecycledViewPool;

    public OrganizationCategoryAdapter(Context context) {
        super(context);
        mRecycledViewPool = new RecyclerView.RecycledViewPool();
        mRecycledViewPool.setMaxRecycledViews(R.layout.item_organization_category, 80);
    }

    public void setCategoryList(ArrayList<OrganizationCategory> categoryList) {
        super.replace(categoryList);
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.item_organization_category;
    }

    @Override
    public void onBindViewHolder(CategoryHolder holder, int position) {
        OrganizationCategory category = getItem(position);
        holder.mCategoryTextView.setText(category.getName());
        holder.mAdapter.setOrganizationList(category.getOrganizations());
    }

    @Override
    public void onViewRecycled(CategoryHolder holder) {
        super.onViewRecycled(holder);
    }

    class CategoryHolder extends RecyclerView.ViewHolder {
        TextView mCategoryTextView;
        RecyclerView mContainer;
        OrganizationCatalogAdapter mAdapter;

        CategoryHolder(View itemView) {
            super(itemView);
            mCategoryTextView = (TextView)itemView.findViewById(R.id.organization_category);
            mContainer = (RecyclerView)itemView.findViewById(R.id.container);
            mAdapter = new OrganizationCatalogAdapter(mContext);
            mContainer.setAdapter(mAdapter);
            mContainer.setRecycledViewPool(mRecycledViewPool);
            WrapLinearLayoutManager manager =
                    new WrapLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            manager.setAutoMeasureEnabled(false);
            mContainer.setLayoutManager(manager);
        }
    }
}