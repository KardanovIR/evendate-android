package ru.evendate.android.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 29.02.2016.
 */
public class UserFavoritedCard extends CardView {
    private UsersView mUsersView;
    private TextView mAllTextView;
    private TextView mTitleTextView;

    public UserFavoritedCard(Context context) {
        this(context, null);
    }

    public UserFavoritedCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rootView = inflater.inflate(R.layout.view_user_favorited, this, true);
        mUsersView = (UsersView)rootView.findViewById(R.id.user_container);
        mAllTextView = (TextView)rootView.findViewById(R.id.all_users);
        mTitleTextView = (TextView)rootView.findViewById(R.id.title);
        mAllTextView.setText(context.getString(R.string.event_users_all));
    }

    public void setOnAllButtonListener(View.OnClickListener clickListener){
        mAllTextView.setOnClickListener(clickListener);
    }

    public ArrayList<UserDetail> getUsers() {
        return mUsersView.getUsers();
    }

    public void setUsers(ArrayList<UserDetail> users) {
        mUsersView.setUsers(users);
    }

    public void setTitle(String title){
        mTitleTextView.setText(title);
    }

}
