package ru.evendate.android.ui.userdetail;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import ru.evendate.android.R;
import ru.evendate.android.models.ActionType;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.WrapLinearLayoutManager;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
class ActionTypesAdapter extends AbstractAdapter<ActionType, ActionTypesAdapter.ActionHolder> {
    private RecyclerView.RecycledViewPool actionItemsPool;
    private Context mContext;

    ActionTypesAdapter(@NonNull Context context, RecyclerView.RecycledViewPool actionItemsPool) {
        mContext = context;
        this.actionItemsPool = actionItemsPool;
    }

    @Override
    public ActionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActionHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_action, parent, false));
    }

    @Override
    public void onBindViewHolder(ActionHolder holder, int position) {
        ActionType type = getItem(position);
        holder.mActionTextView.setText(type.getTypeName(mContext));
        String name = type.getUser().getFirstName() + " " + type.getUser().getLastName();
        holder.mUserNameTextView.setText(name);
        Picasso.with(mContext)
                .load(type.getUser().getAvatarUrl())
                .error(R.mipmap.ic_launcher)
                .into(holder.mAvatarView);
        holder.mActionTargetsAdapter.set(type.getTargetList());
    }

    class ActionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View holderView;
        @BindView(R.id.action_description) TextView mActionTextView;
        @BindView(R.id.user_name) TextView mUserNameTextView;
        @BindView(R.id.user_avatar) CircleImageView mAvatarView;
        @BindView(R.id.recycler_view) RecyclerView recyclerView;
        ActionTargetsAdapter mActionTargetsAdapter;

        ActionHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);

            mActionTargetsAdapter = new ActionTargetsAdapter(mContext);
            WrapLinearLayoutManager manager =
                    new WrapLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            manager.setAutoMeasureEnabled(false);
            recyclerView.setLayoutManager(manager);
            recyclerView.setRecycledViewPool(actionItemsPool);
            recyclerView.setAdapter(mActionTargetsAdapter);
            recyclerView.setNestedScrollingEnabled(false);
        }

        @Override
        public void onClick(View v) {
            //if(v == holderView){
            //    Intent intent = new Intent(mContext, UserProfileActivity.class);
            //    intent.setData(EvendateContract.UserEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build());
            //    mContext.startActivity(intent);
            //}
        }

    }
}
