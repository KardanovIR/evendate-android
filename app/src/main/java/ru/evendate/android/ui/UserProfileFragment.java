package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.evendate.android.R;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.UserLoader;
import ru.evendate.android.sync.models.UserDetail;

/**
 * Created by Dmitry on 15.02.2015.
 */
public class UserProfileFragment extends Fragment implements LoaderListener<UserDetail>{
    private Uri mUri;
    private int userId;
    public static final String URI = "uri";
    private RecyclerView mRecyclerView;
    SubscriptionsAdapter mAdapter;
    UserLoader mLoader;
    UserAdapter mUserAdapter;

    ImageView mUserImageView;
    TextView mUserNameTextView;

    public static UserProfileFragment newInstance(int id){
        UserProfileFragment fragment = new UserProfileFragment();
        fragment.userId = id;
        return fragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        mUserImageView = (ImageView)rootView.findViewById(R.id.user_image);
        mUserNameTextView = (TextView)rootView.findViewById(R.id.user_name);
        mAdapter = new SubscriptionsAdapter(getActivity());
        mUserAdapter = new UserAdapter();
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLoader = new UserLoader(getActivity());
        mLoader.setLoaderListener(this);
        mLoader.getData(userId);
        return rootView;
    }
    @Override
    public void onLoaded(UserDetail user) {
        if(!isAdded())
            return;
        mAdapter.setSubscriptionList(user.getSubscriptions());
        mUserAdapter.setUser(user);
    }

    @Override
    public void onError() {
        if(!isAdded())
            return;
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLoader.getData(userId);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    private class UserAdapter{
        private UserDetail mUserDetail;

        public void setUser(UserDetail user) {
            mUserDetail = user;
            setUserInfo();
        }

        public UserDetail getUser() {
            return mUserDetail;
        }

        private void setUserInfo(){
            //prevent illegal state exception cause fragment not attached to
            if(!isAdded())
                return;
            String userName = mUserDetail.getLastName() + " " + mUserDetail.getFirstName();
            mUserNameTextView.setText(userName);

            Picasso.with(getContext())
                    .load(mUserDetail.getAvatarUrl())
                    .error(R.drawable.default_background)
                    .into(mUserImageView);
        }
    }
}
