package ru.evendate.android.ui.users;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.User;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.userdetail.UserProfileActivity;

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
        User userEntry = getItem(position);
        holder.id = userEntry.getEntryId();
        String name = userEntry.getLastName() + " " + userEntry.getFirstName();
        holder.mNameTextView.setText(name);
        Picasso.with(mContext)
                .load(userEntry.getAvatarUrl())
                .error(R.drawable.default_background)
                .into(holder.mUserImageView);
    }

    class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View holderView;
        @Bind(R.id.user_item_image) ImageView mUserImageView;
        @Bind(R.id.user_item_name) TextView mNameTextView;
        public int id;

        UserHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                Intent intent = new Intent(mContext, UserProfileActivity.class);
                intent.setData(EvendateContract.UserEntry.getContentUri(id));
                if (Build.VERSION.SDK_INT >= 21) {
                    mContext.startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation((Activity)mContext).toBundle());
                } else
                    mContext.startActivity(intent);
            }
        }

    }
}
