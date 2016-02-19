package ru.evendate.android.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import ru.evendate.android.R;
import ru.evendate.android.adapters.AbstractAdapter;
import ru.evendate.android.sync.models.Action;

/**
 * Created by ds_gordeev on 17.02.2016.
 */
public class ActionsAdapter extends AbstractAdapter<Action, ActionsAdapter.UserHolder> {


    public ActionsAdapter(Context context) {
        super(context);
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false));
    }
    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        //if(getList() == null)
        //    return;
        //Action entry = getList().get(position);
        //String name = entry.getLastName() + " " + userEntry.getFirstName();
        //holder.mNameTextView.setText(name);
        //Picasso.with(mContext)
        //        .load(userEntry.getAvatarUrl())
        //        .error(R.drawable.default_background)
        //        .into(holder.mUserImageView);
    }
    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        public ImageView mUserImageView;
        public TextView mNameTextView;

        public UserHolder(View itemView){
            super(itemView);
            holderView = itemView;
            mUserImageView = (ImageView)itemView.findViewById(R.id.user_item_image);
            mNameTextView = (TextView)itemView.findViewById(R.id.user_item_name);
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
