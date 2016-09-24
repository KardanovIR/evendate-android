package ru.evendate.android.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.ui.OrganizationDetailActivity;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class SubscriptionsAdapter extends AbstractAdapter<OrganizationSubscription, SubscriptionsAdapter.SubscriptionHolder> {

    public static Uri mUri = EvendateContract.OrganizationEntry.CONTENT_URI;


    public SubscriptionsAdapter(Context context) {
        super(context);
    }

    public void setSubscriptionList(ArrayList<OrganizationSubscription> subscriptionList) {
        replace(subscriptionList);
    }

    @Override
    public SubscriptionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SubscriptionHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_sub, parent, false));
    }

    @Override
    public void onBindViewHolder(SubscriptionHolder holder, int position) {
        OrganizationSubscription subEntry = getItem(position);
        holder.id = subEntry.getEntryId();
        holder.mOrganizationNameTextView.setText(subEntry.getShortName());
        Picasso.with(mContext)
                .load(subEntry.getLogoSmallUrl())
                .error(R.mipmap.ic_launcher)
                .into(holder.mOrganizationLogoImageView);
    }

    public class SubscriptionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        @Bind(R.id.item_user_sub_organization_name) TextView mOrganizationNameTextView;
        @Bind(R.id.item_user_sub_organization_logo) ImageView mOrganizationLogoImageView;
        public int id;

        public SubscriptionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                Intent intent = new Intent(mContext, OrganizationDetailActivity.class);
                intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                if (Build.VERSION.SDK_INT > 21)
                    mContext.startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation((Activity)mContext).toBundle());
                else
                    mContext.startActivity(intent);
            }
        }
    }
}