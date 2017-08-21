package ru.evendate.android.ui.userdetail;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.evendate.android.R;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.NpaLinearLayoutManager;

/**
 * Created by Dmitry on 21.02.2016.
 */
public class UserSubscriptionsFragment extends Fragment {

    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;
    private SubscriptionsAdapter mAdapter;
    private UserDetail mUser;
    private static final String USER_OBJ_KEY = "user";
    private Unbinder unbinder;

    public static UserSubscriptionsFragment newInstance(UserDetail user) {
        UserSubscriptionsFragment fragment = new UserSubscriptionsFragment();
        fragment.mUser = user;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_subs, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        if (savedInstanceState != null)
            mUser = new Gson().fromJson(savedInstanceState.getString(USER_OBJ_KEY), UserDetail.class);

        mAdapter = new SubscriptionsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
        mAdapter.setSubscriptionList(mUser.getSubscriptions());
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(USER_OBJ_KEY, new Gson().toJson(mUser));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
