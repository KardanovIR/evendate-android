package ru.evendate.android.ui.userdetail;

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

import ru.evendate.android.R;
import ru.evendate.android.models.ActionTarget;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.eventdetail.EventDetailActivity;
import ru.evendate.android.ui.orgdetail.OrganizationDetailActivity;

/**
 * Created by Dmitry on 20.02.2016.
 */
class ActionTargetsAdapter extends AbstractAdapter<ActionTarget, ActionTargetsAdapter.ActionTargetHolder> {

    ActionTargetsAdapter(Context context) {
        super(context);
    }

    @Override
    public ActionTargetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActionTargetHolder(LayoutInflater.from(parent.getContext())
                .inflate(viewType, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        int type = getItem(position).getTargetType();
        if (type == ActionTarget.TYPE_EVENT)
            return R.layout.item_action_target_event;
        else
            return R.layout.item_action_target_org;
    }

    @Override
    public void onBindViewHolder(ActionTargetHolder holder, int position) {
        ActionTarget action = getItem(position);
        holder.mNameTextView.setText(action.getTargetName());
        holder.mUri = action.getTargetUri();
        holder.type = action.getTargetType();
        Picasso.with(mContext)
                .load(action.getTargetImageLink())
                .error(R.mipmap.ic_launcher)
                .into(holder.mImageView);
    }

    class ActionTargetHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View holderView;
        TextView mNameTextView;
        ImageView mImageView;
        Uri mUri;
        public int type;

        ActionTargetHolder(View itemView) {
            super(itemView);
            holderView = itemView;
            mNameTextView = (TextView)itemView.findViewById(R.id.action_target_text_view);
            mImageView = (ImageView)itemView.findViewById(R.id.action_target_image);
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                Intent intent;
                if (type == ActionTarget.TYPE_EVENT)
                    intent = new Intent(mContext, EventDetailActivity.class);
                else
                    intent = new Intent(mContext, OrganizationDetailActivity.class);
                intent.setData(mUri);
                if (Build.VERSION.SDK_INT > 21)
                    mContext.startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation((Activity)mContext).toBundle());
                else
                    mContext.startActivity(intent);
            }
        }

    }
}