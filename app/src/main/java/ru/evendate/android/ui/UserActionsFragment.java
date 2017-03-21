package ru.evendate.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
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
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.adapters.AggregateDate;
import ru.evendate.android.adapters.DatesAdapter;
import ru.evendate.android.adapters.NpaLinearLayoutManager;
import ru.evendate.android.models.Action;
import ru.evendate.android.models.ActionConverter;
import ru.evendate.android.models.ActionType;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.views.LoadStateView;

/**
 * Created by Dmitry on 21.02.2016.
 */
public class UserActionsFragment extends Fragment implements LoadStateView.OnReloadListener {
    private static String LOG_TAG = UserActionsFragment.class.getSimpleName();

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    private DatesAdapter mAdapter;
    private static final String USER_ID_KEY = "user_id";
    private int userId;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;

    public static UserActionsFragment newInstance(int userId) {
        UserActionsFragment fragment = new UserActionsFragment();
        fragment.userId = userId;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_actions, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState != null)
            userId = savedInstanceState.getInt(USER_ID_KEY);

        initRecyclerView();
        mLoadStateView.setOnReloadListener(this);
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(USER_ID_KEY, userId);
    }

    private void initRecyclerView() {
        mAdapter = new DatesAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new LandingAnimator());
        mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLoadStateView.showProgress();
        loadActions();
    }

    public void loadActions() {
        ApiService service = ApiFactory.getService(getActivity());
        Observable<ResponseArray<Action>> actionObservable =
                service.getActions(EvendateAccountManager.peekToken(getActivity()),
                        userId, Action.FIELDS_LIST, Action.ORDER_BY);

        actionObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            Log.i(LOG_TAG, "loaded");
                            if (result.isOk())
                                onLoaded(result.getData());
                            else
                                mLoadStateView.showErrorHint();
                        },
                        this::onError,
                        mLoadStateView::hideProgress);
    }

    @Override
    public void onReload() {
        loadActions();
    }

    public void onLoaded(ArrayList<Action> list) {
        ArrayList<AggregateDate<ActionType>> convertedList = ActionConverter.convertActions(list);
        mAdapter.replace(convertedList);
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        if (!isAdded())
            mLoadStateView.showErrorHint();
    }
}