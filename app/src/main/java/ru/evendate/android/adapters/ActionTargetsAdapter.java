package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 20.02.2016.
 */
public class ActionTargetsAdapter extends AbstractAdapter<Long, ActionTargetsAdapter.ActionTargetHolder> {

    public ActionTargetsAdapter(Context context) {
        super(context);
    }

    @Override
    public ActionTargetHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ActionTargetHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_action_target, parent, false));
    }

    @Override
    public void onBindViewHolder(ActionTargetHolder holder, int position) {
        if (getList() == null)
            return;
        Long id = getList().get(position);
        holder.id = id;
        holder.mNameTextView.setText(String.valueOf(id));
    }

    public class ActionTargetHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        public TextView mNameTextView;
        public long id;

        public ActionTargetHolder(View itemView) {
            super(itemView);
            holderView = itemView;
            mNameTextView = (TextView) itemView.findViewById(R.id.action_target_text_view);
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                Toast.makeText(mContext, String.valueOf(id), Toast.LENGTH_SHORT).show();
            //    Intent intent = new Intent(mContext, UserProfileActivity.class);
            //    intent.setData(EvendateContract.UserEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build());
            //    mContext.startActivity(intent);
            }
        }

    }
}