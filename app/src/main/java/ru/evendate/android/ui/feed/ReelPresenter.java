package ru.evendate.android.ui.feed;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Date;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DefaultObserver;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.ui.EndlessContract;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by dmitry on 12.09.17.
 */
public class ReelPresenter implements EndlessContract.EndlessPresenter {
    private static String LOG_TAG = ReelPresenter.class.getSimpleName();

    private static final int LENGTH = 10;
    private DataSource mDataSource;
    private Disposable mDisposable;
    private EndlessContract.EndlessView<ReelPresenter, Event> mView;
    private int type;
    private int organizationId;
    private Date mDate;
    private String mQuery;

    private ReelPresenter(@NonNull DataSource dataRepository,
                          @NonNull EndlessContract.EndlessView<ReelPresenter, Event> view) {
        mDataSource = checkNotNull(dataRepository);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    public static ReelPresenter newInstance(@NonNull DataSource dataRepository,
                                            @NonNull EndlessContract.EndlessView<ReelPresenter, Event> view,
                                            ReelFragment.ReelType type) {
        ReelPresenter presenter = new ReelPresenter(dataRepository, view);
        presenter.type = type.type();
        return presenter;
    }

    public static ReelPresenter newInstance(@NonNull DataSource dataRepository,
                                            @NonNull EndlessContract.EndlessView<ReelPresenter, Event> view,
                                            ReelFragment.ReelType type, int organizationId) {
        ReelPresenter presenter = new ReelPresenter(dataRepository, view);
        presenter.type = type.type();
        presenter.organizationId = organizationId;
        return presenter;
    }

    public static ReelPresenter newInstance(@NonNull DataSource dataRepository,
                                            @NonNull EndlessContract.EndlessView<ReelPresenter, Event> view,
                                            ReelFragment.ReelType type, Date date) {
        ReelPresenter presenter = new ReelPresenter(dataRepository, view);
        presenter.type = type.type();
        presenter.mDate = date;
        return presenter;
    }

    public void setQuery(String query) {
        mQuery = query;
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
        mDisposable = getDataObservable(page).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                            if (!result.isOk())
                                mView.showError();
                            else {
                                boolean isLast = result.getData().size() < LENGTH;
                                List<Event> list = result.getData();
                                if (list.isEmpty() && mView.isEmpty())
                                    mView.showEmptyState();
                                else if (forceLoad) {
                                    mView.reshowList(list, isLast);
                                } else {
                                    mView.showList(list, isLast);
                                }
                            }
                        }, this::onError,
                        () -> mView.setLoadingIndicator(false));

    }

    private Observable<ResponseArray<Event>> getDataObservable(int page) {
        Observable<ResponseArray<Event>> observable;
        String token = EvendateAccountManager.peekToken(mView.getContext());
        int cityId = EvendatePreferences.newInstance(mView.getContext()).getUserCity().getEntryId();
        switch (ReelFragment.ReelType.getType(type)) {
            case FEED:
                if (token != null) {
                    observable = mDataSource.getFeed(token, page, LENGTH);
                } else {
                    observable = mDataSource.getFeed(null, cityId, page, LENGTH);
                }
                break;
            case FAVORITES:
                if (token != null) {
                    observable = mDataSource.getFavorite(token, page, LENGTH);
                } else {
                    observable = mDataSource.getFavorite(null, cityId, page, LENGTH);
                }
                break;
            case ORGANIZATION:
                observable = mDataSource.getOrgEvents(token, organizationId, page, LENGTH);
                break;
            case ORGANIZATION_PAST:
                observable = mDataSource.getOrgPastEvents(token, organizationId, page, LENGTH);
                break;
            case CALENDAR:
                observable = mDataSource.getCalendarEvents(token, mDate, page, LENGTH);
                break;
            case RECOMMENDATION:
                observable = mDataSource.getRecommendations(token, page, LENGTH);
                break;
            case SEARCH:
                observable = mDataSource.searchEvents(token, mQuery, page, LENGTH);
                break;
            case SEARCH_BY_TAG:
                observable = mDataSource.searchByTagEvents(token, mQuery, page, LENGTH);
                break;
            default:
                throw new RuntimeException("unknown type");
        }
        return observable;
    }

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }

    //todo DRY event actions
    public void hideEvent(EventFeed event) {
        String token = EvendateAccountManager.peekToken(mView.getContext());

        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe((String newToken) -> {
                if (!event.isHidden()) {
                    mDataSource.hideEvent(newToken, event.getEntryId()).subscribe(getHideObserver());
                } else {
                    mDataSource.unhideEvent(newToken, event.getEntryId()).subscribe(getHideObserver());
                }
                event.setHidden(!event.isHidden());
            });
        } else {
            if (!event.isHidden()) {
                mDataSource.hideEvent(token, event.getEntryId()).subscribe(getHideObserver());
            } else {
                mDataSource.unhideEvent(token, event.getEntryId()).subscribe(getHideObserver());
            }
            event.setHidden(!event.isHidden());
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

    //todo DRY event actions
    public void likeEvent(EventFeed event) {
        String token = EvendateAccountManager.peekToken(mView.getContext());
        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe((String newToken) -> like(newToken, event));
        } else {
            like(token, event);
        }
    }

    private void like(String token, EventFeed event) {
        Observable<Response> likeEventObservable;
        if (event.isFavorite()) {
            likeEventObservable = mDataSource.unfaveEvent(token, event.getEntryId());
        } else {
            likeEventObservable = mDataSource.faveEvent(token, event.getEntryId());
        }
        likeEventObservable.subscribe(getFaveObserver(event));
        event.setIsFavorite(!event.isFavorite());
    }

    private Observer<Response> getFaveObserver(EventFeed event) {
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

}