package ru.evendate.android.ui.users;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.User;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.AbstractAdapter;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UsersAdapter extends AbstractAdapter<UserDetail, UsersAdapter.UserHolder> {

    private Context mContext;
    private UsersInteractionListener mListener;

    public UsersAdapter(@NonNull Context context, @NonNull UsersInteractionListener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_list, parent, false));
    }

    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
        User userEntry = getItem(position);
        holder.mUser = userEntry;
        String name = userEntry.getLastName() + " " + userEntry.getFirstName();
        holder.mNameTextView.setText(name);
        Picasso.with(mContext)
                .load(userEntry.getAvatarUrl())
                .error(R.drawable.default_background)
                .into(holder.mUserImageView);
    }

    public class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View holderView;
        @BindView(R.id.user_item_image) ImageView mUserImageView;
        @BindView(R.id.user_item_name) TextView mNameTextView;
        User mUser;

        UserHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                mListener.openUser(mUser);
            }
        }

    }

    public interface UsersInteractionListener {
        void openUser(User user);
    }
}
