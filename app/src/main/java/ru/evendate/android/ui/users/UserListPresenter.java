package ru.evendate.android.ui.users;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.EndlessContract;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by dmitry on 11.09.17.
 */

public class UserListPresenter implements EndlessContract.EndlessPresenter {
    private static String LOG_TAG = UserListPresenter.class.getSimpleName();

    private static final int LENGTH = 10;
    private DataSource mDataSource;
    private Disposable mDisposable;
    private UserListContract.UserListView mView;

    UserListPresenter(@NonNull DataSource dataSource,
                      @NonNull UserListContract.UserListView view) {
        mDataSource = checkNotNull(dataSource);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        reload();
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
        switch (mView.getType()) {
            case EVENT:
                loadEvent(token, mView.getEventId());
                break;
            case ORGANIZATION:
                loadOrganization(token, mView.getOrgId());
                break;
            case FRIENDS:
                if (token == null) {
                    mView.requestAuth().subscribe(
                            (String newToken) -> loadFriends(newToken, forceLoad, page));
                } else {
                    loadFriends(token, forceLoad, page);
                }
                break;
        }
    }

    private void loadEvent(String token, int eventId) {
        mDisposable = mDataSource.getEvent(token, eventId)
                .subscribe(result -> {
                            if (!result.isOk())
                                mView.showError();
                            else {
                                Event event = result.getData().get(0);
                                List<UserDetail> users = event.getUserList();
                                if (users.isEmpty() && mView.isEmpty())
                                    mView.showEmptyState();
                                else
                                    mView.showList(users, true);
                            }
                        }, this::onError,
                        () -> mView.setLoadingIndicator(false));
    }

    private void loadOrganization(String token, int organizationId) {
        mDisposable = mDataSource.getOrg(token, organizationId)
                .subscribe(result -> {
                            if (!result.isOk())
                                mView.showError();
                            else {
                                OrganizationFull org = result.getData().get(0);
                                List<UserDetail> users = org.getSubscribedUsersList();
                                if (users.isEmpty() && mView.isEmpty())
                                    mView.showEmptyState();
                                else
                                    mView.showList(users, true);
                            }
                        }, this::onError,
                        () -> mView.setLoadingIndicator(false));
    }

    private void loadFriends(String token, boolean forceLoad, int page) {
        mDisposable = mDataSource.getFriends(token, page, page * 20)
                .subscribe(result -> {
                            if (!result.isOk())
                                mView.showError();
                            else {
                                List<UserDetail> list = result.getData();
                                boolean isLast = list.size() < LENGTH;
                                boolean isEmpty = list.size() == 0;
                                if (isEmpty && mView.isEmpty()) {
                                    mView.showEmptyState();
                                } else if (forceLoad) {
                                    mView.reshowList(list, isLast);
                                } else {
                                    mView.showList(list, isLast);
                                }
                            }
                        }, this::onError,
                        () -> mView.setLoadingIndicator(false));
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }
}
