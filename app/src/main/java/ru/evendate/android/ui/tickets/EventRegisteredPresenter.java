package ru.evendate.android.ui.tickets;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.disposables.Disposable;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.EventRegistered;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 14.03.17.
 */
//TODO DRY
class EventRegisteredPresenter implements EventRegisteredContract.Presenter {
    private static final int LENGTH = 10;
    private static String LOG_TAG = EventRegisteredPresenter.class.getSimpleName();
    private DataRepository mDataRepository;
    private Disposable mDisposable;
    private EventRegisteredContract.View mView;
    private boolean isFuture = true;

    EventRegisteredPresenter(@NonNull DataRepository dataRepository,
                             @NonNull EventRegisteredContract.View view, boolean future) {
        mDataRepository = checkNotNull(dataRepository);
        mView = checkNotNull(view);
        mView.setPresenter(this);
        isFuture = future;
    }

    @Override
    public void start() {
        reloadEvents();
    }

    @Override
    public void stop() {
        if (mDisposable != null)
            mDisposable.dispose();
    }

    public void reloadEvents() {
        loadEvents(true, 0);
    }

    //todo token
    public void loadEvents(boolean forceLoad, int page) {
        mView.setLoadingIndicator(forceLoad);
        String token = EvendateAccountManager.peekToken(mView.getContext());
        mDisposable = mDataRepository.getRegisteredEvents(token, isFuture, page, LENGTH)
                .subscribe(result -> {
                            List<EventRegistered> list = new ArrayList<>(result.getData());
                            boolean isLast = list.size() < LENGTH;
                            if (result.isOk()) {
                                if (list.isEmpty() && mView.isEmpty()) {
                                    mView.showEmptyState();
                                } else if (forceLoad) {
                                    mView.reshowEvents(list, isLast);
                                } else {
                                    mView.showEvents(list, isLast);
                                }
                            } else {
                                mView.showError();
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
