package ru.evendate.android.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import ru.evendate.android.R;
import ru.evendate.android.models.UserDetail;

/**
 * Created by ds_gordeev on 21.03.2016.
 */
public class UsersView extends LinearLayout {
    private ArrayList<UserDetail> mUsers;
    private int userLimit = 6;
    private int type = 0;

    public UsersView(Context context) {
        this(context, null);
    }

    public UsersView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.UsersView,
                0, 0);

        try {
            userLimit = a.getInteger(R.styleable.UsersView_usersLimit, userLimit);
            type = a.getInteger(R.styleable.UsersView_type, type);
        } finally {
            a.recycle();
        }
        if (isInEditMode()) {
            mockup();
        }
    }

    public ArrayList<UserDetail> getUsers() {
        return mUsers;
    }

    public void setUsers(ArrayList<UserDetail> users) {
        this.mUsers = users;
        setupUsers();
    }

    private void setupUsers() {
        if (getChildCount() != 0)
            removeViewsInLayout(0, getChildCount());
        int size = getResources().getDimensionPixelSize(R.dimen.organization_card_user_container_height);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        Resources r = getContext().getResources();
        int px = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4,
                r.getDisplayMetrics()
        );
        if(type == 1)
            px = -px;
        lp.setMargins(0, 0, px, 0);
        for (int i = 0; i < mUsers.size(); i++) {
            if (i > userLimit - 1)
                break;
            CircleImageView circleImageView = new CircleImageView(getContext());
            circleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Picasso.with(getContext()).load(mUsers.get(i).getAvatarUrl()).into(circleImageView);
            addView(circleImageView, lp);
        }
    }

    private void mockup() {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MarginLayoutParams.WRAP_CONTENT,
                MarginLayoutParams.MATCH_PARENT);
        Resources r = getContext().getResources();
        int px = (int)TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                4,
                r.getDisplayMetrics()
        );
        lp.setMargins(0, 0, px, 0);
        for (int i = 0; i < userLimit; i++) {
            CircleImageView circleImageView = new CircleImageView(getContext());
            circleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            circleImageView.setImageResource(R.drawable.butterfly);
            circleImageView.setMinimumWidth(getHeight());
            addView(circleImageView, i, lp);
        }
    }
}
