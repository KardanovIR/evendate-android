package ru.evendate.android.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.adapters.NpaLinearLayoutManager;
import ru.evendate.android.adapters.SubscriptionsAdapter;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 21.02.2016.
 */
public class UserSubscriptionsFragment extends Fragment {

    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    private SubscriptionsAdapter mAdapter;
    private UserDetail mUser;

    public static UserSubscriptionsFragment newInstance(UserDetail user) {
        UserSubscriptionsFragment fragment = new UserSubscriptionsFragment();
        fragment.mUser = user;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_subs, container, false);
        ButterKnife.bind(this, rootView);
        mAdapter = new SubscriptionsAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
        mAdapter.setSubscriptionList(mUser.getSubscriptions());
        return rootView;
    }
}
