package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dmitry on 21.02.2016.
 */
public class UserActionsFragment extends Fragment {
    private static String LOG_TAG = UserActionsFragment.class.getSimpleName();

    @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
    private DatesAdapter mAdapter;
    private int userId;
    @Bind(R.id.progress_bar) ProgressBar mProgressBar;
    AlertDialog dialog;

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

        initRecyclerView();
        initProgressBar();
        return rootView;
    }

    private void initRecyclerView() {
        mAdapter = new DatesAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(new LandingAnimator());
        mRecyclerView.setLayoutManager(new NpaLinearLayoutManager(getActivity()));
    }

    private void initProgressBar() {
        mProgressBar.getProgressDrawable()
                .setColorFilter(ContextCompat.getColor(getActivity(), R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadActions();
    }

    public void loadActions() {
        ApiService evendateService = ApiFactory.getService(getActivity());
        Observable<ResponseArray<Action>> actionObservable =
                evendateService.getActions(EvendateAccountManager.peekToken(getActivity()),
                        userId, Action.FIELDS_LIST, Action.ORDER_BY);

        actionObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    Log.i(LOG_TAG, "loaded");
                    if (result.isOk())
                        onLoaded(result.getData());
                    else
                        onError();
                }, error -> {
                    onError();
                    Log.e(LOG_TAG, error.getMessage());
                }, () -> Log.i(LOG_TAG, "completed"));
    }

    public void onLoaded(ArrayList<Action> list) {
        ArrayList<AggregateDate<ActionType>> convertedList = ActionConverter.convertActions(list);
        mProgressBar.setVisibility(View.GONE);
        mAdapter.replace(convertedList);
    }

    public void onError() {
        if (!isAdded())
            return;
        mProgressBar.setVisibility(View.GONE);
        dialog = ErrorAlertDialogBuilder.newInstance(getActivity(), (DialogInterface dialog, int which) -> {
            loadActions();
            dialog.dismiss();
        });
        dialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (dialog != null)
            dialog.dismiss();
    }
}