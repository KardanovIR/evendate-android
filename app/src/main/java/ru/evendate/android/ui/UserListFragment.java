package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.adapters.NpaLinearLayoutManager;
import ru.evendate.android.adapters.UsersAdapter;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UserListFragment extends Fragment {
    private String LOG_TAG = UserListFragment.class.getSimpleName();

    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    public static final String TYPE = "type";
    public static final String EVENT_ID = "event_id";
    public static final String ORGANIZATION_ID = "organization_id";
    private int type = 0;
    private int organizationId;
    private int eventId;
    @Bind(R.id.progressBar) ProgressBar mProgressBar;
    @Bind(R.id.ll_feed_empty) View mFeedEmptyLayout;

    public enum TypeFormat {
        EVENT(0),
        ORGANIZATION(1),
        FRIENDS(2);

        final int type;
        TypeFormat(int nativeInt) {
            this.type = nativeInt;
        }

        static public TypeFormat getType(int pType) {
            for (TypeFormat type: TypeFormat.values()) {
                if (type.type() == pType) {
                    return type;
                }
            }
            throw new RuntimeException("unknown type");
        }
        public int type() {
            return type;
        }
    }

    public static UserListFragment newInstance(int type, int id) {
        UserListFragment userListFragment = new UserListFragment();
        userListFragment.type = type;
        if (type == TypeFormat.EVENT.type) {
            userListFragment.eventId = id;
        } else {
            userListFragment.organizationId = id;
        }
        return userListFragment;
    }

    public static UserListFragment newFriendsInstance(int type) {
        UserListFragment userListFragment = new UserListFragment();
        userListFragment.type = type;
        return userListFragment;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(TYPE, type);
        outState.putInt(EVENT_ID, eventId);
        outState.putInt(ORGANIZATION_ID, organizationId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_list, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null) {
            type = savedInstanceState.getInt(TYPE);
        }
        initTransitions();
        mAdapter = new UsersAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));

        mProgressBar.getProgressDrawable()
                .setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        return rootView;
    }

    private void initTransitions(){
        if(Build.VERSION.SDK_INT >= 21){
            getActivity().getWindow().setEnterTransition(new Explode());
            getActivity().getWindow().setExitTransition(new Explode());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        if (savedInstanceState == null)
            return;
        type = savedInstanceState.getInt(TYPE);
        eventId = savedInstanceState.getInt(EVENT_ID);
        organizationId = savedInstanceState.getInt(ORGANIZATION_ID);
    }

    @Override
    public void onStart() {
        super.onStart();

        hideCap();

        switch (UserListFragment.TypeFormat.getType(type)){
            case EVENT:
                loadEvent();
                break;
            case ORGANIZATION:
                loadOrganization();
                break;
            case FRIENDS:
                loadFriends();
                break;
        }
    }

    private void loadOrganization() {
        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<OrganizationFull>> eventObservable =
                apiService.getOrganization(EvendateAccountManager.peekToken(getActivity()),
                        organizationId, OrganizationDetail.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (!isAdded())
                        return;
                    if(!result.isOk()){
                        onError();
                        return;
                    }
                    OrganizationDetail organization = result.getData().get(0);
                    mAdapter.replace(organization.getSubscribedUsersList());
                    mProgressBar.setVisibility(View.GONE);
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "completed"));
    }

    private void loadEvent() {
        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<EventDetail>> eventObservable =
                apiService.getEvent(EvendateAccountManager.peekToken(getActivity()),
                        eventId, EventDetail.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (!isAdded())
                        return;
                    if(!result.isOk()){
                        onError();
                        return;
                    }
                    EventDetail event = result.getData().get(0);
                    mAdapter.replace(event.getUserList());
                    mProgressBar.setVisibility(View.GONE);
                    if (mAdapter.isEmpty()) {
                        displayCap();
                    }
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "completed"));
    }

    private void loadFriends(){
        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<UserDetail>> friendsObservable =
                apiService.getFriends(EvendateAccountManager.peekToken(getActivity()), UserDetail.FIELDS_LIST);

        friendsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (!isAdded())
                        return;
                    if(!result.isOk()){
                        onError();
                        return;
                    }
                    mAdapter.replace(result.getData());
                    mProgressBar.setVisibility(View.GONE);
                    if (mAdapter.isEmpty()) {
                        displayCap();
                    }
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "completed"));
    }

    private void onError(){
        if (!isAdded())
            return;
        AlertDialog alertDialog = ErrorAlertDialogBuilder.newInstance(getActivity(),
                (DialogInterface dialog, int which) -> {
                loadEvent();
                mProgressBar.setVisibility(View.VISIBLE);
                dialog.dismiss();
        });
        alertDialog.show();
    }

    private void displayCap(){
        mFeedEmptyLayout.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    private void hideCap(){
        mFeedEmptyLayout.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
}
