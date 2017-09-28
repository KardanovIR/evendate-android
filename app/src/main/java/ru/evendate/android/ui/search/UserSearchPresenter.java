package ru.evendate.android.ui.search;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.EndlessContract;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by dmitry on 18.09.17.
 */
class UserSearchPresenter implements EndlessContract.EndlessPresenter {
    private static final String LOG_TAG = UserSearchPresenter.class.getSimpleName();

    private static final int LENGTH = 10;
    private DataSource mDataSource;
    private Disposable mDisposable;
    private EndlessContract.EndlessView<UserSearchPresenter, UserDetail> mView;
    private String mQuery;

    UserSearchPresenter(@NonNull DataSource dataSource,
                        @NonNull EndlessContract.EndlessView<UserSearchPresenter, UserDetail> view) {
        mDataSource = checkNotNull(dataSource);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    public void setQuery(String query) {
        mQuery = query;
    }

    @Override
    public void start() {
    }

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
        mDisposable = mDataSource.searchUser(token, mQuery, page, LENGTH).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (!result.isOk())
                                mView.showError();
                            else {
                                List<UserDetail> list = result.getData();
                                boolean isLast = list.size() < LENGTH;
                                if (list.isEmpty() && mView.isEmpty())
                                    mView.showEmptyState();
                                else
                                    mView.showList(list, isLast);
                            }
                        }, this::onError,
                        () -> mView.setLoadingIndicator(false));
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }
}
