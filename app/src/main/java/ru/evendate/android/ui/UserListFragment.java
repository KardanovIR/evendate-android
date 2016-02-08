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

import ru.evendate.android.R;
import ru.evendate.android.loaders.EventLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.OrganizationLoader;
import ru.evendate.android.sync.models.EventDetail;
import ru.evendate.android.sync.models.OrganizationDetail;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UserListFragment extends Fragment{
    private String LOG_TAG = UserListFragment.class.getSimpleName();

    private android.support.v7.widget.RecyclerView mRecyclerView;
    OrganizationLoader mOrganizationLoader;
    EventLoader mEventLoader;
    private UsersAdapter mAdapter;
    static final String TYPE = "type";
    static final String EVENT_ID = "event_id";
    static final String ORGANIZATION_ID = "organization_id";
    private int type = 0;
    private int organizationId;
    private int eventId;

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
        mOrganizationLoader = new OrganizationLoader(getActivity());
        mEventLoader = new EventLoader(getActivity());
        if (type == TypeFormat.event.nativeInt)
            loadEvent();
        else
            loadOrganization();
        return rootView;
    }

    private void loadOrganization(){
        mOrganizationLoader.setLoaderListener(new LoaderListener<OrganizationDetail>() {
            @Override
            public void onLoaded(OrganizationDetail subList) {
                mAdapter.setUserList(subList.getSubscribedUsersList());
            }
            @Override
            public void onError() {
                buildAlertDialog().show();
            }
        });
        mOrganizationLoader.getOrganization(organizationId);
    }

    private void loadEvent(){
        mEventLoader.setLoaderListener(new LoaderListener<EventDetail>() {
            @Override
            public void onLoaded(EventDetail subList) {
                mAdapter.setUserList(subList.getUserList());
            }
            @Override
            public void onError() {
                buildAlertDialog().show();
            }
        });
        mEventLoader.getData(eventId);
    }

    public AlertDialog buildAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.loading_error));
        builder.setMessage(getString(R.string.loading_error_description));

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mOrganizationLoader.getOrganization(organizationId);
                dialog.dismiss();
            }
        });
        return builder.create();
    }

}
