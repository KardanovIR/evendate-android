package ru.evendate.android.ui.tinder;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.models.Event;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 31.03.17.
 */

public class RecommenderPresenter implements RecommenderContract.Presenter {
    private static final int LENGTH = 10;
    private static String LOG_TAG = RecommenderPresenter.class.getSimpleName();
    private RecommenderContract.View mView;
    private DataSource mDataRepository;


    RecommenderPresenter(@NonNull DataRepository dataRepository,
                         @NonNull RecommenderContract.View view) {
        mDataRepository = checkNotNull(dataRepository);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        loadRecommends(true, 0);
    }

    @Override
    public void stop() {

    }

    @Override
    public void loadRecommends(boolean forceLoad, int page) {
        mView.setLoadingIndicator(true);
        mDataRepository.getRecommendations(page, LENGTH)
                .subscribe(result -> {
                            List<Event> list = result.getData();
                            boolean isLast = list.size() < LENGTH;
                            boolean isEmpty = list.size() == 0;
                            if (result.isOk()) {
                                if (isEmpty) {
                                    mView.showEmptyState();
                                } else if (forceLoad) {
                                    mView.reshowRecommends(list, isLast);
                                } else {
                                    mView.showRecommends(list, isLast);
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

    @Override
    public void faveEvent(Event event) {
        /*mDataRepository.faveEvent(event.getEntryId()).subscribe(result -> {
                    if (result.isOk()) {
                        Log.i(LOG_TAG, "performed like");
                        event.setIsFavorite(true);
                    } else
                        Log.e(LOG_TAG, "Error with response with like");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage())
        );*/
    }

    @Override
    public void unfaveEvent(Event event) {
        /*mDataRepository.unfaveEvent(event.getEntryId()).subscribe(result -> {
                    if (result.isOk()) {
                        Log.i(LOG_TAG, "performed unlike");
                        event.setIsFavorite(false);
                    } else
                        Log.e(LOG_TAG, "Error with response with unlike");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage())
        );*/
    }

    @Override
    public void hideEvent(Event event) {
        /*mDataRepository.hideEvent(event.getEntryId()).subscribe(result -> {
                    if (result.isOk()) {
                        Log.i(LOG_TAG, "performed hide");
                    } else
                        Log.e(LOG_TAG, "Error with response with hide");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage())
        );*/
    }

    @Override
    public void unhideEvent(Event event) {
        /*mDataRepository.unhideEvent(event.getEntryId()).subscribe(result -> {
                    if (result.isOk()) {
                        Log.i(LOG_TAG, "performed unhide");
                    } else
                        Log.e(LOG_TAG, "Error with response with unhide");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage())
        );*/
    }
}
