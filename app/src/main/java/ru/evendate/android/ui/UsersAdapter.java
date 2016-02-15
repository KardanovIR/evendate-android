package ru.evendate.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.sync.models.UserModel;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserHolder>{

    Context mContext;
    private ArrayList<UserModel> mUserList;
    public static Uri mUri = EvendateContract.EventEntry.CONTENT_URI;

    public UsersAdapter(Context context){
        this.mContext = context;
    }

    public void setUserList(ArrayList<UserModel> userList){
        mUserList = userList;
        notifyDataSetChanged();
    }

    public ArrayList<UserModel> getUserList() {
        return mUserList;
    }
    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        if(mUserList == null)
            return;
        UserModel userEntry = mUserList.get(position);
        holder.id = userEntry.getEntryId();
        String name = userEntry.getLastName() + " " + userEntry.getFirstName();
        holder.mNameTextView.setText(name);
        Picasso.with(mContext)
                .load(userEntry.getAvatarUrl())
                .error(R.drawable.default_background)
                .into(holder.mUserImageView);
    }
    @Override
    public int getItemCount() {
        if(mUserList == null)
            return 0;
        return mUserList.size();
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
