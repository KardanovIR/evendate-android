package ru.evendate.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.ui.OrganizationDetailActivity;

/**
 * Created by Dmitry on 01.12.2015.
 * adapter for organization model into catalog item
 */
public class OrganizationCatalogAdapter extends RecyclerView.Adapter<OrganizationCatalogAdapter.OrganizationHolder> {

    private Context mContext;
    private ArrayList<OrganizationSubscription> mOrganizationList;
    private Uri mUri = EvendateContract.OrganizationEntry.CONTENT_URI;

    public OrganizationCatalogAdapter(Context context) {
        this.mContext = context;
    }

    public void setOrganizationList(ArrayList<OrganizationSubscription> organizationList) {
        mOrganizationList = organizationList;
        notifyDataSetChanged();
    }

    @Override
    public OrganizationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new OrganizationHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organization_catalog, parent, false));
    }

    @Override
    public void onBindViewHolder(OrganizationHolder holder, int position) {
        if (mOrganizationList != null) {
            OrganizationSubscription organizationEntry = mOrganizationList.get(position);
            holder.id = organizationEntry.getEntryId();
            holder.mTitle.setText(organizationEntry.getShortName());
            String subs = organizationEntry.getSubscribedCount() + " " +
                    mContext.getResources().getString(R.string.organization_card_subscribers);
            holder.mSubCounts.setText(subs);
            Picasso.with(mContext)
                    .load(organizationEntry.getLogoSmallUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.mImageView);
        }
    }

    @Override
    public int getItemCount() {
        if (mOrganizationList == null) {
            return 0;
        } else {
            return mOrganizationList.size();
        }
    }


    public class OrganizationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View mItem;
        public TextView mTitle;
        public TextView mSubCounts;
        public ImageView mImageView;
        public long id;

        public OrganizationHolder(View itemView) {
            super(itemView);
            mItem = itemView;
            mSubCounts = (TextView)itemView.findViewById(R.id.organization_item_subs);
            mTitle = (TextView)itemView.findViewById(R.id.item_title);
            mImageView = (ImageView)itemView.findViewById(R.id.organization_icon);
            this.id = (int)this.getItemId();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.equals(mItem)) {
                Intent intent = new Intent(mContext, OrganizationDetailActivity.class);
                intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                mContext.startActivity(intent);
            }
        }
    }

}
