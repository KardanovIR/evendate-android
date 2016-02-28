package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.adapters.DatesAdapter;
import ru.evendate.android.loaders.ActionLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.Action;
import ru.evendate.android.models.ActionConverter;

/**
 * Created by Dmitry on 21.02.2016.
 */
public class UserActionsFragment extends Fragment implements LoaderListener<ArrayList<Action>> {

    private RecyclerView mRecyclerView;
    DatesAdapter mAdapter;
    ActionLoader mLoader;
    private int userId;

    public static UserActionsFragment newInstance(int userId){
        UserActionsFragment userListFragment = new UserActionsFragment();
        userListFragment.userId = userId;
        return userListFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_actions, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        mAdapter = new DatesAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLoader = new ActionLoader(getActivity());
        mLoader.setLoaderListener(this);
        mLoader.getData(userId);
        return rootView;
    }
    @Override
    public void onLoaded(ArrayList<Action> list) {
        mAdapter.setList(ActionConverter.convertActions(list));
    }
    @Override
    public void onError() {
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mLoader.getData(userId);
                dialog.dismiss();
            }
        });
        dialog.show();
    }
}