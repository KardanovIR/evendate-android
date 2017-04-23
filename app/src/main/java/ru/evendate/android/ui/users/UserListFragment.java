package ru.evendate.android.ui.users;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.transition.Explode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.ui.NpaLinearLayoutManager;
import ru.evendate.android.views.LoadStateView;

public class UserListFragment extends Fragment implements LoadStateView.OnReloadListener {
    private String LOG_TAG = UserListFragment.class.getSimpleName();

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    private UsersAdapter mAdapter;
    public static final String TYPE = "type";
    public static final String EVENT_ID = "event_id";
    public static final String ORGANIZATION_ID = "organization_id";
    private int type = 0;
    private int organizationId;
    private int eventId;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;

    public enum TypeFormat {
        EVENT(0),
        ORGANIZATION(1),
        FRIENDS(2);

        final int type;

        TypeFormat(int nativeInt) {
            this.type = nativeInt;
        }

        static public TypeFormat getType(int pType) {
            for (TypeFormat type : TypeFormat.values()) {
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
        mLoadStateView.setOnReloadListener(this);
        setEmptyCap();

        return rootView;
    }

    private void initTransitions() {
        if (Build.VERSION.SDK_INT >= 21) {
            getActivity().getWindow().setEnterTransition(new Explode());
            getActivity().getWindow().setExitTransition(new Explode());
        }
    }

    private void setEmptyCap() {
        mLoadStateView.setEmptyHeader(getString(R.string.list_users_empty_text));
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
        mLoadStateView.showProgress();
    }

    private void loadData() {
        switch (UserListFragment.TypeFormat.getType(type)) {
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
        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<ResponseArray<OrganizationFull>> eventObservable =
                apiService.getOrganization(EvendateAccountManager.peekToken(getActivity()),
                        organizationId, OrganizationDetail.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (!result.isOk())
                                mLoadStateView.showErrorHint();
                            else
                                onLoadedOrgs(result.getData());
                        }, this::onError,
                        mLoadStateView::hideProgress);
    }

    private void loadEvent() {
        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<ResponseArray<Event>> eventObservable =
                apiService.getEvent(EvendateAccountManager.peekToken(getActivity()),
                        eventId, Event.FIELDS_LIST);

        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (!result.isOk())
                                mLoadStateView.showErrorHint();
                            else
                                onLoadedEvents(result.getData());
                        }, this::onError,
                        mLoadStateView::hideProgress);
    }

    private void loadFriends() {
        ApiService apiService = ApiFactory.getService(getActivity());
        Observable<ResponseArray<UserDetail>> friendsObservable =
                apiService.getFriends(EvendateAccountManager.peekToken(getActivity()), UserDetail.FIELDS_LIST);

        friendsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (!result.isOk())
                                mLoadStateView.showErrorHint();
                            else
                                onLoaded(result.getData());
                        }, this::onError,
                        mLoadStateView::hideProgress);
    }


    public void onLoadedEvents(ArrayList<Event> events) {
        if (!isAdded())
            return;
        Event event = events.get(0);
        onLoaded(event.getUserList());
    }

    public void onLoadedOrgs(ArrayList<OrganizationFull> orgs) {
        if (!isAdded())
            return;
        OrganizationFull organization = orgs.get(0);
        onLoaded(organization.getSubscribedUsersList());
    }

    public void onLoaded(ArrayList<UserDetail> friends) {
        if (!isAdded())
            return;
        mAdapter.replace(friends);
        checkListAndShowHint();
    }

    private void onError(Throwable error) {
        if (!isAdded())
            return;
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    public void onReload() {
        loadData();
    }

    protected void checkListAndShowHint() {
        if (mAdapter.isEmpty())
            mLoadStateView.showEmptryHint();
    }
}
