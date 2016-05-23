package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.evendate.android.R;
import ru.evendate.android.models.ActionType;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
public class ActionTypesAdapter extends AbstractAdapter<ActionType, ActionTypesAdapter.ActionHolder> {

    public ActionTypesAdapter(Context context) {
        super(context);
    }

    @Override
    public ActionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActionHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_action, parent, false));
    }

    @Override
    public void onBindViewHolder(ActionHolder holder, int position) {
        if (getList() == null)
            return;
        ActionType type = getList().get(position);
        holder.mActionTextView.setText(type.getTypeName(mContext));
        String name = type.getUser().getFirstName() + " " + type.getUser().getLastName();
        holder.mUserNameTextView.setText(name);
        Picasso.with(mContext)
                .load(type.getUser().getAvatarUrl())
                .error(R.mipmap.ic_launcher)
                .into(holder.mAvatarView);

        holder.mActionTargetsAdapter = new ActionTargetsAdapter(mContext);
        WrapLinearLayoutManager manager =
                new WrapLinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        manager.setAutoMeasureEnabled(false);
        holder.recyclerView.setLayoutManager(manager);
        holder.recyclerView.setAdapter(holder.mActionTargetsAdapter);
        holder.mActionTargetsAdapter.setList(type.getTargetList());
        holder.recyclerView.setNestedScrollingEnabled(false);
    }

    public class ActionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        public TextView mActionTextView;
        public TextView mUserNameTextView;
        public CircleImageView mAvatarView;
        public RecyclerView recyclerView;
        public ActionTargetsAdapter mActionTargetsAdapter;

        public ActionHolder(View itemView) {
            super(itemView);
            holderView = itemView;
            recyclerView = (RecyclerView)itemView.findViewById(R.id.recycler_view);
            mActionTextView = (TextView)itemView.findViewById(R.id.action_description);
            mUserNameTextView = (TextView)itemView.findViewById(R.id.user_name);
            mAvatarView = (CircleImageView)itemView.findViewById(R.id.user_avatar);
            holderView.setOnClickListener(this);
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
