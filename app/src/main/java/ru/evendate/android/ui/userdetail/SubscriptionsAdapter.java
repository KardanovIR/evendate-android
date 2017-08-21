package ru.evendate.android.ui.userdetail;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.orgdetail.OrganizationDetailActivity;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class SubscriptionsAdapter extends AbstractAdapter<OrganizationSubscription, SubscriptionsAdapter.SubscriptionHolder> {


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

    class SubscriptionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View holderView;
        @BindView(R.id.item_user_sub_organization_name) TextView mOrganizationNameTextView;
        @BindView(R.id.item_user_sub_organization_icon) ImageView mOrganizationLogoImageView;
        public int id;

        SubscriptionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                Intent intent = new Intent(mContext, OrganizationDetailActivity.class);
                intent.setData(EvendateContract.OrganizationEntry.getContentUri(id));
                if (Build.VERSION.SDK_INT > 21)
                    mContext.startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation((Activity)mContext).toBundle());
                else
                    mContext.startActivity(intent);
            }
        }
    }
}