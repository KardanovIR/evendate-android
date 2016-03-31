package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import ru.evendate.android.R;
import ru.evendate.android.adapters.UsersAdapter;
import ru.evendate.android.loaders.EventLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.OrganizationLoader;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.OrganizationDetail;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UserListFragment extends Fragment{
    private String LOG_TAG = UserListFragment.class.getSimpleName();

    private android.support.v7.widget.RecyclerView mRecyclerView;
    private OrganizationLoader mOrganizationLoader;
    private EventLoader mEventLoader;
    private UsersAdapter mAdapter;
    public static final String TYPE = "type";
    public static final String EVENT_ID = "event_id";
    public static final String ORGANIZATION_ID = "organization_id";
    private int type = 0;
    private int organizationId;
    private int eventId;
    private ProgressBar mProgressBar;

    public enum TypeFormat {
        event                (0),
        organization        (1);

        TypeFormat(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }

    public static UserListFragment newInstance(int type, int id){
        UserListFragment userListFragment = new UserListFragment();
        userListFragment.type = type;
        if(type == TypeFormat.event.nativeInt){
            userListFragment.eventId = id;
        }
        else{
            userListFragment.organizationId = id;
        }
        return userListFragment;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                          Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);

        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        if (savedInstanceState != null){
            type = savedInstanceState.getInt(TYPE);
        }

        mAdapter = new UsersAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (type == TypeFormat.event.nativeInt)
            mEventLoader = new EventLoader(getActivity());
        else
            mOrganizationLoader = new OrganizationLoader(getActivity());

        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable()
                .setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        if (type == TypeFormat.event.nativeInt)
            loadEvent();
        else
            loadOrganization();
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE, type);
        outState.putInt(EVENT_ID, eventId);
        outState.putInt(ORGANIZATION_ID, organizationId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState == null)
            return;
        type = savedInstanceState.getInt(TYPE);
        eventId = savedInstanceState.getInt(EVENT_ID);
        organizationId = savedInstanceState.getInt(ORGANIZATION_ID);
    }

    private void loadOrganization(){
        mOrganizationLoader.setLoaderListener(new LoaderListener<OrganizationDetail>() {
            @Override
            public void onLoaded(OrganizationDetail subList) {
                if(!isAdded())
                    return;
                mAdapter.setList(subList.getSubscribedUsersList());
                mProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError() {
                if(!isAdded())
                    return;
                AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mOrganizationLoader.getOrganization(organizationId);
                        mProgressBar.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                mProgressBar.setVisibility(View.GONE);
            }
        });
        mOrganizationLoader.getOrganization(organizationId);
    }

    private void loadEvent(){
        mEventLoader.setLoaderListener(new LoaderListener<EventDetail>() {
            @Override
            public void onLoaded(EventDetail subList) {
                if(!isAdded())
                    return;
                mAdapter.setList(subList.getUserList());
                mProgressBar.setVisibility(View.GONE);
            }
            @Override
            public void onError() {
                if(!isAdded())
                    return;
                AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mEventLoader.getData(eventId);
                        mProgressBar.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                mProgressBar.setVisibility(View.GONE);
            }
        });
        mEventLoader.getData(eventId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (type == TypeFormat.event.nativeInt)
            mEventLoader.cancel();
        else
            mOrganizationLoader.cancel();
    }
}
