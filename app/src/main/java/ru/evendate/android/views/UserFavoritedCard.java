package ru.evendate.android.views;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.evendate.android.R;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 29.02.2016.
 */
public class UserFavoritedCard extends CardView {
    private ArrayList<UserDetail> mUsers;
    private int userLimit = 6;
    LinearLayout mLinearLayout;

    public UserFavoritedCard(Context context) {
        this(context, null);
    }

    public UserFavoritedCard(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_user_favorited, this, true);
        mLinearLayout.findViewById(R.id.user_container);
    }


    public ArrayList<UserDetail> getUsers() {
        return mUsers;
    }

    public void setUsers(ArrayList<UserDetail> users) {
        this.mUsers = users;
        setupUsers();
    }
    private void setupUsers(){
        if(mLinearLayout.getChildCount() != 0)
            mLinearLayout.removeViewsInLayout(0, mLinearLayout.getChildCount());
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(mLinearLayout.getLayoutParams());
        lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        for(int i = 0; i < userLimit; i++){
            CircleImageView circleImageView = new CircleImageView(getContext());
            Picasso.with(getContext()).load(mUsers.get(i).getAvatarUrl()).into(circleImageView);
            circleImageView.setLayoutParams(lp);
            mLinearLayout.addView(circleImageView);
        }
    }
}
