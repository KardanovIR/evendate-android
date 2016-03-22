package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.UsersFormatter;
import ru.evendate.android.ui.ReelFragment;
import ru.evendate.android.views.UsersView;

/**
 * Created by ds_gordeev on 22.03.2016.
 */
public class OrganizationEventsAdapter extends EventsAdapter{
    private OrganizationDetail mOrganization;
    private OrganizationCardController mOrganizationCardController;

    public OrganizationEventsAdapter(Context context, OrganizationCardController controller) {
        super(context, ReelFragment.TypeFormat.ORGANIZATION.type());
        mOrganizationCardController = controller;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (mOrganization != null ? 1 : 0);
    }

    public void setOrganization(OrganizationDetail organization) {
        mOrganization = organization;
        super.setEventList(organization.getEventsList());
        notifyDataSetChanged();
    }

    public OrganizationDetail getOrganization() {
        return mOrganization;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0 && mOrganization != null)
            return R.layout.card_organization_detail;
        return super.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        switch (viewType) {
            case R.layout.card_organization_detail:
                viewHolder = new OrganizationHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
                break;
            default:
                viewHolder = super.onCreateViewHolder(parent, viewType);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if(mOrganization == null)
            return;
        if(position == 0){
            OrganizationHolder holder = (OrganizationHolder)viewHolder;
            holder.mNameView.setText(mOrganization.getName());
            holder.mUsersView.setUsers(mOrganization.getSubscribedUsersList());
            holder.mDescriptionView.setText(mOrganization.getDescription());
            holder.mPlaceView.setText(mOrganization.getDefaultAddress());
            holder.mUsersDescriptionView.setText(UsersFormatter.formatUsers(mContext, mOrganization));
            Picasso.with(mContext)
                    .load(mOrganization.getLogoMediumUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.mLogoView);
        } else{
            super.onBindViewHolder(viewHolder, position - 1);
        }
    }
    static final ButterKnife.Action<View> VISIBLE = new ButterKnife.Action<View>() {
        @Override public void apply(View view, int index) {
            view.setVisibility(View.VISIBLE);
        }
    };
    static final ButterKnife.Action<View> GONE = new ButterKnife.Action<View>() {
        @Override public void apply(View view, int index) {
            view.setVisibility(View.GONE);
        }
    };

    @SuppressWarnings("unused")
    public class OrganizationHolder extends RecyclerView.ViewHolder {
        public View holderView;
        public int id;

        @Bind(R.id.organization_icon) ImageView mLogoView;
        @Bind(R.id.organization_name) TextView mNameView;
        @Bind(R.id.organization_users) UsersView mUsersView;
        @Bind(R.id.organization_toggle_more) ToggleButton mMoreToggle;
        @Bind(R.id.organization_description) TextView mDescriptionView;
        @Bind(R.id.organization_place) TextView mPlaceView;
        @Bind(R.id.organization_users_description) TextView mUsersDescriptionView;

        @Bind({ R.id.organization_divider, R.id.organization_description_label,
                R.id.organization_description, R.id.organization_place_label,
                R.id.organization_place, R.id.organization_link_label, R.id.organization_link_container })
        List<View> mDescriptionViews;


        public OrganizationHolder(View itemView){
            super(itemView);
            holderView = itemView;
            ButterKnife.bind(this, itemView);
            ButterKnife.apply(mDescriptionViews, GONE);
            mMoreToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                        ButterKnife.apply(mDescriptionViews, VISIBLE);
                    }
                    else{
                        ButterKnife.apply(mDescriptionViews, GONE);
                    }
                }
            });
        }

        @OnClick(R.id.organization_place)
        public void onPlaceClick(View v) {
            if(mOrganization == null)
                return;
            mOrganizationCardController.onPlaceClicked();
        }
        @OnClick(R.id.organization_link_container)
        public void onLinkClick(View v) {
            if(mOrganization == null)
                return;
            mOrganizationCardController.onLinkClicked();
        }
        @OnClick(R.id.organization_subscribe_button)
        public void onSubscribeClick(View v) {
            if(mOrganization == null)
                return;
            mOrganizationCardController.onSubscribed();
        }
        @OnClick(R.id.organization_more_button)
        public void onMoreClick(View v) {
            if(mOrganization == null)
                return;
            mMoreToggle.setChecked(!mMoreToggle.isChecked());
        }
        @OnClick(R.id.organization_users_container)
        public void onUsersClick(View v) {
            if(mOrganization == null)
                return;
            mOrganizationCardController.onUsersClicked();
        }


    }
    public interface OrganizationCardController{
        void onSubscribed();
        void onUsersClicked();
        void onPlaceClicked();
        void onLinkClicked();
    }
}
