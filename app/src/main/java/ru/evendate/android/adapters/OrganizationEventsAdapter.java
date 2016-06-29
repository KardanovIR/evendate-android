package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
public class OrganizationEventsAdapter extends EventsAdapter {
    private OrganizationDetail mOrganization;
    private OrganizationCardController mOrganizationCardController;
    //base adapter know nothing about organization card
    final int ORGANIZATION_ITEM = 1;

    public OrganizationEventsAdapter(Context context, RecyclerView recyclerView,
                                     OrganizationCardController cardController) {
        super(context, recyclerView, ReelFragment.ReelType.ORGANIZATION.type());
        mOrganizationCardController = cardController;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (mOrganization != null ? ORGANIZATION_ITEM : 0);
    }

    public void setOrganization(OrganizationDetail organization) {
        mOrganization = organization;
        //super.setList(organization.getEventsList());
        notifyDataSetChanged();
    }

    public OrganizationDetail getOrganization() {
        return mOrganization;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 && mOrganization != null)
            return R.layout.card_organization_detail;
        return super.getItemViewType(position - ORGANIZATION_ITEM);
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
        final int ORG_CARD_POSITION = 0;
        if (mOrganization == null)
            return;
        if (position == ORG_CARD_POSITION) {
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
            holder.mSubscribeButton.setChecked(mOrganization.isSubscribed());
            if(mOrganization.getSubscribedUsersList().size() == 0)
                holder.mUserContainer.setVisibility(View.GONE);

        } else {
            super.onBindViewHolder(viewHolder, position - ORGANIZATION_ITEM);
        }
    }

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
        @Bind(R.id.organization_users_container) RelativeLayout mUserContainer;
        @Bind(R.id.organization_more_container) RelativeLayout mOrganizationMoreContainer;
        @Bind(R.id.organization_subscribe_button) ToggleButton mSubscribeButton;

        @Bind({R.id.organization_divider, R.id.organization_description_label,
                R.id.organization_description, R.id.organization_place_label,
                R.id.organization_place, R.id.organization_link_label, R.id.organization_link_container})
        List<View> mDescriptionViews;


        public OrganizationHolder(View itemView) {
            super(itemView);
            holderView = itemView;
            ButterKnife.bind(this, itemView);
            mOrganizationMoreContainer.setVisibility(View.GONE);
            mMoreToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        expand(mOrganizationMoreContainer);
                    } else {
                        collapse(mOrganizationMoreContainer);
                    }
                }
            });
        }

        @OnClick(R.id.organization_place)
        public void onPlaceClick(View v) {
            if (mOrganization == null)
                return;
            mOrganizationCardController.onPlaceClicked();
        }

        @OnClick(R.id.organization_link_container)
        public void onLinkClick(View v) {
            if (mOrganization == null)
                return;
            mOrganizationCardController.onLinkClicked();
        }

        @OnClick(R.id.organization_subscribe_button)
        public void onSubscribeClick(View v) {
            if (mOrganization == null)
                return;
            mOrganizationCardController.onSubscribed();
        }

        @OnClick(R.id.organization_more_button)
        public void onMoreClick(View v) {
            if (mOrganization == null)
                return;
            mMoreToggle.setChecked(!mMoreToggle.isChecked());
        }

        @OnClick(R.id.organization_users_description)
        public void onUsersClick(View v) {
            if (mOrganization == null)
                return;
            mOrganizationCardController.onUsersClicked();
        }

        public void expand(final View v) {
            v.measure(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            final int targetHeight = v.getMeasuredHeight();

            // Older versions of android (pre API 21) cancel animations for views with a height of 0.
            v.getLayoutParams().height = 1;
            v.setVisibility(View.VISIBLE);
            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    v.getLayoutParams().height = interpolatedTime == 1
                            ? RelativeLayout.LayoutParams.WRAP_CONTENT
                            : (int)(targetHeight * interpolatedTime);
                    v.requestLayout();
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // 1dp/ms
            a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
            v.startAnimation(a);
        }

        public void collapse(final View v) {
            final int initialHeight = v.getMeasuredHeight();

            Animation a = new Animation() {
                @Override
                protected void applyTransformation(float interpolatedTime, Transformation t) {
                    if (interpolatedTime == 1) {
                        v.setVisibility(View.GONE);
                    } else {
                        v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                        v.requestLayout();
                    }
                }

                @Override
                public boolean willChangeBounds() {
                    return true;
                }
            };

            // 1dp/ms
            a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
            v.startAnimation(a);
        }

    }

    public interface OrganizationCardController {
        void onSubscribed();

        void onUsersClicked();

        void onPlaceClicked();

        void onLinkClicked();
    }
}
