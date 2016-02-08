package ru.evendate.android.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.sync.models.OrganizationType;

/**
 * Created by Dmitry on 30.11.2015.
 */
public class OrganizationCategoryAdapter extends RecyclerView.Adapter<OrganizationCategoryAdapter.CategoryHolder>{

    Context mContext;
    private ArrayList<OrganizationType> mCategoryList;

    public OrganizationCategoryAdapter(Context context){
        this.mContext = context;
    }

    public void setCategoryList(ArrayList<OrganizationType> categoryList){
        mCategoryList = categoryList;
        notifyDataSetChanged();
    }

    public ArrayList<OrganizationType> getCategoryList() {
        return mCategoryList;
    }

    @Override
    public CategoryHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new CategoryHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.organization_category_item, parent, false));
    }

    @Override
    public void onBindViewHolder(CategoryHolder holder, int position) {
        if(mCategoryList == null)
            return;
        String category = mCategoryList.get(position).getName();
        holder.mCategoryTextView.setText(category);
        holder.mAdapter = new OrganizationCatalogAdapter(mContext);
        holder.mAdapter.setOrganizationList(mCategoryList.get(position).getOrganizations());
        holder.mContainer.setAdapter(holder.mAdapter);
        holder.mContainer.setLayoutManager(new OrganizationCatalogAdapter.CatalogLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
    }

    @Override
    public int getItemCount() {
        if(mCategoryList == null)
            return 0;
        return mCategoryList.size();
    }

    @Override
    public void onViewRecycled(CategoryHolder holder) {
        super.onViewRecycled(holder);
    }

    public class CategoryHolder extends RecyclerView.ViewHolder {
        public TextView mCategoryTextView;
        public RecyclerView mContainer;
        public OrganizationCatalogAdapter mAdapter;
        public CategoryHolder(View itemView){
            super(itemView);
            mCategoryTextView = (TextView)itemView.findViewById(R.id.organization_category);
            mContainer = (RecyclerView)itemView.findViewById(R.id.container);
        }
    }
}