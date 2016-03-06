package ru.evendate.android.views;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private LinearLayout mLinearLayout;
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
        mLinearLayout = (LinearLayout)rootView.findViewById(R.id.user_container);
        mAllTextView = (TextView)rootView.findViewById(R.id.all_users);
        mTitleTextView = (TextView)rootView.findViewById(R.id.title);
        if(isInEditMode()){
            mockup();
            return;
        }
        mAllTextView.setText(context.getString(R.string.event_users_all));
    }

    public void setOnAllButtonListener(View.OnClickListener clickListener){
        mAllTextView.setOnClickListener(clickListener);
    }

    public ArrayList<UserDetail> getUsers() {
        return mUsers;
    }

    public void setUsers(ArrayList<UserDetail> users) {
        this.mUsers = users;
        setupUsers();
    }

    public void setTitle(String title){
        mTitleTextView.setText(title);
    }

    private void setupUsers(){
        if(mLinearLayout.getChildCount() != 0)
            mLinearLayout.removeViewsInLayout(0, mLinearLayout.getChildCount());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.MATCH_PARENT);
        Resources r = getContext().getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4,
                r.getDisplayMetrics()
        );
        lp.setMargins(0, 0, px, 0);
        for(int i = 0; i < mUsers.size(); i++){
            if(i > userLimit - 1)
                break;
            CircleImageView circleImageView = new CircleImageView(getContext());
            circleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(getContext()).load(mUsers.get(i).getAvatarUrl()).into(circleImageView);
            circleImageView.setMinimumWidth(mLinearLayout.getHeight());
            mLinearLayout.addView(circleImageView, lp);
        }
    }
    private void mockup(){
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.MATCH_PARENT);
        Resources r = getContext().getResources();
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4,
                r.getDisplayMetrics()
        );
        lp.setMargins(0, 0, px, 0);
        for(int i = 0; i < userLimit; i++){
            CircleImageView circleImageView = new CircleImageView(getContext());
            circleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            circleImageView.setImageResource(R.drawable.butterfly);
            circleImageView.setMinimumWidth(mLinearLayout.getHeight());
            mLinearLayout.addView(circleImageView, lp);
        }
    }
}
