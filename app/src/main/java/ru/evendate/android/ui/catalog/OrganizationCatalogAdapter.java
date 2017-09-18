package ru.evendate.android.ui.catalog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.ui.AbstractAdapter;

/**
 * Created by Dmitry on 01.12.2015.
 * adapter for organization model into catalog item
 */
public class OrganizationCatalogAdapter extends AbstractAdapter<OrganizationFull,
        OrganizationCatalogAdapter.OrganizationHolder> {
    private Context mContext;
    private OrganizationInteractionListener mListener;

    public OrganizationCatalogAdapter(@NonNull Context context,
                                      @NonNull OrganizationInteractionListener listener) {
        mContext = context;
        mListener = listener;
    }

    void setOrganizationList(ArrayList<OrganizationFull> organizationList) {
        set(organizationList);
    }

    @Override
    public OrganizationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new OrganizationHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organization_catalog, parent, false));
    }

    @Override
    public void onBindViewHolder(OrganizationHolder holder, int position) {
        OrganizationSubscription organizationEntry = getItem(position);
        holder.mOrganization = organizationEntry;
        holder.mTitle.setText(organizationEntry.getShortName());
        String subs = organizationEntry.getSubscribedCount() + " " +
                mContext.getResources().getString(R.string.organization_card_subscribers);
        holder.mSubCounts.setText(subs);
        Picasso.with(mContext)
                .load(organizationEntry.getLogoSmallUrl())
                .error(R.mipmap.ic_launcher)
                .into(holder.mImageView);
    }

    public class OrganizationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View mItem;
        @BindView(R.id.item_title) TextView mTitle;
        @BindView(R.id.organization_item_subs) TextView mSubCounts;
        @BindView(R.id.organization_icon) ImageView mImageView;
        OrganizationSubscription mOrganization;

        OrganizationHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItem = itemView;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.equals(mItem)) {
                mListener.openOrg(mOrganization);
            }
        }
    }

    public interface OrganizationInteractionListener {
        void openOrg(OrganizationSubscription organization);
    }
}
