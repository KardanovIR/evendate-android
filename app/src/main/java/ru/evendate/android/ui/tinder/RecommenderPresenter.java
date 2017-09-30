package ru.evendate.android.ui.tinder;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.models.Event;
import ru.evendate.android.network.Response;

import static com.google.common.base.Preconditions.checkNotNull;
import static ru.evendate.android.ui.tinder.RecommenderContract.PAGE_LENGTH;

/**
 * Created by Aedirn on 31.03.17.
 */
public class RecommenderPresenter implements RecommenderContract.Presenter {
    private static String LOG_TAG = RecommenderPresenter.class.getSimpleName();
    private RecommenderContract.View mView;
    private DataSource mDataRepository;
    private Disposable mDisposable;

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
        mDisposable.dispose();
    }

    @Override
    public void loadRecommends(boolean forceLoad, int page) {
        mView.setLoadingIndicator(forceLoad);
        String token = EvendateAccountManager.peekToken(mView.getContext());
        mDisposable = mDataRepository.getRecommendations(token, page, PAGE_LENGTH)
                .subscribe(result -> {
                            List<Event> list = result.getData();
                            boolean isLast = list.size() < PAGE_LENGTH;
                            if (result.isOk()) {
                                if (list.isEmpty() && mView.isEmpty()) {
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

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }

    //todo DRY event actions
    @Override
    public void faveEvent(Event event) {
        String token = EvendateAccountManager.peekToken(mView.getContext());
        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe((String newToken) ->
                    mDataRepository.faveEvent(newToken, event.getEntryId()).subscribe(getFaveObserver(event)));
        } else {
            mDataRepository.faveEvent(token, event.getEntryId()).subscribe(getFaveObserver(event));
        }
    }

    @Override
    public void unfaveEvent(Event event) {
        String token = EvendateAccountManager.peekToken(mView.getContext());
        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe((String newToken) ->
                    mDataRepository.unfaveEvent(newToken, event.getEntryId()).subscribe(getFaveObserver(event)));
        } else {
            mDataRepository.unfaveEvent(token, event.getEntryId()).subscribe(getFaveObserver(event));
        }
    }

    private Observer<Response> getFaveObserver(Event event) {
        return new DefaultObserver<Response>() {
            @Override
            public void onNext(Response value) {
                if (value.isOk()) {
                    Log.i(LOG_TAG, "performed like/dislike");
                    event.setIsFavorite(event.isFavorite());
                } else
                    Log.e(LOG_TAG, "Error with response with like/dislike");
            }

            @Override
            public void onError(Throwable error) {
                Log.e(LOG_TAG, "" + error.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }

    @Override
    public void hideEvent(Event event) {
        String token = EvendateAccountManager.peekToken(mView.getContext());

        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe((String newToken) ->
                    mDataRepository.hideEvent(newToken, event.getEntryId()).subscribe(getHideObserver()));
        } else {
            mDataRepository.hideEvent(token, event.getEntryId()).subscribe(getHideObserver());
        }
    }

    @Override
    public void unhideEvent(Event event) {
        String token = EvendateAccountManager.peekToken(mView.getContext());

        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe((String newToken) ->
                    mDataRepository.unhideEvent(newToken, event.getEntryId()).subscribe(getHideObserver()));
        } else {
            mDataRepository.unhideEvent(token, event.getEntryId()).subscribe(getHideObserver());
        }
    }

    private Observer<Response> getHideObserver() {
        return new DefaultObserver<Response>() {
            @Override
            public void onNext(Response value) {
                if (value.isOk()) {
                    Log.i(LOG_TAG, "performed hide/unhide");
                } else
                    Log.e(LOG_TAG, "Error with response with hide/unhide");
            }

            @Override
            public void onError(Throwable error) {
                Log.e(LOG_TAG, "" + error.getMessage());
            }

            @Override
            public void onComplete() {

            }
        };
    }
}
