package ru.evendate.android.ui.checkin;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.EventRegistered;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 15.03.17.
 */

class EventAdminListPresenter implements CheckInContract.EventAdminPresenter {
    private static final int LENGTH = 10;
    private static String LOG_TAG = EventAdminListPresenter.class.getSimpleName();
    private DataRepository mDataRepository;
    private Disposable mDisposable;
    private CheckInContract.EventAdminView mView;

    EventAdminListPresenter(@NonNull DataRepository dataRepository,
                            @NonNull CheckInContract.EventAdminView view) {
        mDataRepository = checkNotNull(dataRepository);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        loadList(true, 0);
    }

    @Override
    public void stop() {
        if (mDisposable != null)
            mDisposable.dispose();
    }

    public void loadList(boolean forceLoad, int page) {
        mView.setLoadingIndicator(forceLoad);
        mDisposable = mDataRepository.getEventsAdmin(true, page, LENGTH)
                .subscribe(result -> {
                            List<EventRegistered> list = new ArrayList<>(result.getData());
                            boolean isLast = list.size() < LENGTH;
                            boolean isEmpty = list.size() == 0;
                            if (result.isOk()) {
                                if (isEmpty && mView.isEmpty()) {
                                    mView.showEmptyState();
                                } else if (forceLoad) {
                                    mView.reshowList(list, isLast);
                                } else {
                                    mView.showList(list, isLast);
                                }
                            } else {
                                mView.showError();
                            }
                        },
                        this::onError,
                        () -> mView.setLoadingIndicator(false)
                );
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }
}
