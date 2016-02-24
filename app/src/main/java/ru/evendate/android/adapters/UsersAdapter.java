package ru.evendate.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.models.UserModel;
import ru.evendate.android.ui.UserProfileActivity;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UsersAdapter extends AbstractAdapter<UserDetail, UsersAdapter.UserHolder> {

    public UsersAdapter(Context context) {
        super(context);
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_list, parent, false));
    }
    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        if(getList() == null)
            return;
        UserModel userEntry = getList().get(position);
        holder.id = userEntry.getEntryId();
        String name = userEntry.getLastName() + " " + userEntry.getFirstName();
        holder.mNameTextView.setText(name);
        Picasso.with(mContext)
                .load(userEntry.getAvatarUrl())
                .error(R.drawable.default_background)
                .into(holder.mUserImageView);
    }
    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        public ImageView mUserImageView;
        public TextView mNameTextView;
        public int id;

        public UserHolder(View itemView){
            super(itemView);
            holderView = itemView;
            mUserImageView = (ImageView)itemView.findViewById(R.id.user_item_image);
            mNameTextView = (TextView)itemView.findViewById(R.id.user_item_name);
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v == holderView){
                Intent intent = new Intent(mContext, UserProfileActivity.class);
                intent.setData(EvendateContract.UserEntry.CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build());
                mContext.startActivity(intent);
            }
        }

    }
}