package ru.evendate.android.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 29.02.2016.
 * users card for event detail
 */
public class UserFavoritedCard extends CardView {
    @BindView(R.id.user_container) UsersView mUsersView;
    @BindView(R.id.all_users) TextView mAllTextView;
    @BindView(R.id.title) TextView mTitleTextView;

    public UserFavoritedCard(Context context) {
        this(context, null);
    }

    public UserFavoritedCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater)context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.view_user_favorited, this, true);
        ButterKnife.bind(this, rootView);
        mAllTextView.setText(context.getString(R.string.event_users_all));
    }

    public void setOnAllButtonListener(View.OnClickListener clickListener) {
        mAllTextView.setOnClickListener(clickListener);
    }

    public ArrayList<UserDetail> getUsers() {
        return mUsersView.getUsers();
    }

    public void setUsers(ArrayList<UserDetail> users) {
        mUsersView.setUsers(users);
    }

    public void setTitle(String title) {
        mTitleTextView.setText(title);
    }

}
