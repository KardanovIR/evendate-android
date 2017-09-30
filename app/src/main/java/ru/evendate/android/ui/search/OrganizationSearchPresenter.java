package ru.evendate.android.ui.search;

import android.support.annotation.NonNull;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.ui.EndlessContract;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by dmitry on 18.09.17.
 */

class OrganizationSearchPresenter implements EndlessContract.EndlessPresenter {
    private static final String LOG_TAG = OrganizationSearchPresenter.class.getSimpleName();

    private static final int LENGTH = 10;
    private DataSource mDataSource;
    private Disposable mDisposable;
    private EndlessContract.EndlessView<OrganizationSearchPresenter, OrganizationFull> mView;
    private String mQuery;

    OrganizationSearchPresenter(@NonNull DataSource dataSource,
                                @NonNull EndlessContract.EndlessView<OrganizationSearchPresenter, OrganizationFull> view) {
        mDataSource = checkNotNull(dataSource);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {
        if (mDisposable != null)
            mDisposable.dispose();
    }

    @Override
    public void reload() {
        load(true, 0);
    }

    @Override
    public void load(boolean forceLoad, int page) {
        mView.setLoadingIndicator(forceLoad);
        String token = EvendateAccountManager.peekToken(mView.getContext());
        mDisposable = mDataSource.searchOrgs(token, mQuery, page, LENGTH)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> {
                            if (!result.isOk())
                                mView.showError();
                            else {
                                if (result.getData().isEmpty())
                                    mView.showEmptyState();
                                else
                                    mView.showList(result.getData(), true);
                            }
                        },
                        this::onError,
                        () -> mView.setLoadingIndicator(false)
                );
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }
}
