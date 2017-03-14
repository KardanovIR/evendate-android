package ru.evendate.android.ui.tickets;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.EventRegistered;
import rx.Subscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 14.03.17.
 */

class EventRegisteredPresenter implements EventRegisteredContract.Presenter {
    private static final int LENGTH = 10;
    private static String LOG_TAG = EventRegisteredPresenter.class.getSimpleName();
    private DataRepository mDataRepository;
    private Subscription mSubscription;
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
        loadEvents(false, 0);
    }

    @Override
    public void stop() {
        if (mSubscription != null)
            mSubscription.unsubscribe();
    }

    public void loadEvents(boolean forceLoad, int page) {
        mView.setLoadingIndicator(true);
        mSubscription = mDataRepository.getRegisteredEvents(isFuture, page, LENGTH)
                .subscribe(result -> {
                            List<EventRegistered> list = new ArrayList<>(result.getData());
                            boolean isLast = list.size() < LENGTH;
                            boolean isEmpty = list.size() == 0;
                            if (result.isOk()) {
                                if (isEmpty) {
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

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }
}
