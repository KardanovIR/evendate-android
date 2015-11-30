package ru.getlect.evendate.evendate;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ru.getlect.evendate.evendate.sync.LocalDataFetcher;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;

/**
 * Created by Dmitry on 30.11.2015.
 */
public class OrganizationCategoryAdapter extends RecyclerView.Adapter<OrganizationCategoryAdapter.CategoryHolder>{

    Context mContext;
    private ArrayList<String> mCategoryList;

    public OrganizationCategoryAdapter(Context context){
        this.mContext = context;
    }

    public void setCategoryList(ArrayList<String> categoryList){
        mCategoryList = categoryList;
        notifyDataSetChanged();
    }

    public ArrayList<String> getCategoryList() {
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
        String category = mCategoryList.get(position);
        holder.mCategoryTextView.setText(category);

        holder.mAdapter = new OrganizationCatalogAdapter(mContext);

        LocalDataFetcher localDataFetcher = new LocalDataFetcher(mContext.getContentResolver(), mContext);
        ArrayList<DataModel> list = localDataFetcher.getOrganizationDataFromDB(category);
        ArrayList<OrganizationModel> organizationList = new ArrayList<>();
        for(DataModel data: list){
            organizationList.add((OrganizationModel)data);
        }
        holder.mAdapter.setOrganizationList(organizationList);
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