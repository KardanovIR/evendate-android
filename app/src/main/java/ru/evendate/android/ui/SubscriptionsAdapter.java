package ru.evendate.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.sync.models.OrganizationDetail;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class SubscriptionsAdapter extends RecyclerView.Adapter<SubscriptionsAdapter.SubscriptionHolder>{

    Context mContext;
    private ArrayList<OrganizationDetail> mSubscriptionList;
    public static Uri mUri = EvendateContract.OrganizationEntry.CONTENT_URI;


    public SubscriptionsAdapter(Context context){
        this.mContext = context;
    }

    public void setSubscriptionList(ArrayList<OrganizationDetail> subscriptionList){
        mSubscriptionList = subscriptionList;
        notifyDataSetChanged();
    }

    public ArrayList<OrganizationDetail> getSubscriptionList() {
        return mSubscriptionList;
    }

    @Override
    public SubscriptionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SubscriptionHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_sub, parent, false));
    }

    @Override
    public void onBindViewHolder(SubscriptionHolder holder, int position) {
        if(mSubscriptionList == null)
            return;
        OrganizationDetail subEntry = mSubscriptionList.get(position);
        holder.id = subEntry.getEntryId();
        holder.mOrganizationNameTextView.setText(subEntry.getName());
        Picasso.with(mContext)
                .load(subEntry.getLogoSmallUrl())
                .error(R.mipmap.ic_launcher)
                .into(holder.mOrganizationLogoImageView);
    }

    @Override
    public int getItemCount() {
        if(mSubscriptionList == null)
            return 0;
        return mSubscriptionList.size();
    }

    public class SubscriptionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        public TextView mOrganizationNameTextView;
        public CircleImageView mOrganizationLogoImageView;
        public int id;

        public SubscriptionHolder(View itemView){
            super(itemView);
            holderView = itemView;
            mOrganizationLogoImageView = (CircleImageView)itemView.findViewById(R.id.item_user_sub_organization_logo);
            mOrganizationNameTextView = (TextView)itemView.findViewById(R.id.item_user_sub_organization_name);
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v == holderView){
                Intent intent = new Intent(mContext, OrganizationDetailActivity.class);
                intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                mContext.startActivity(intent);
            }
        }
    }
}