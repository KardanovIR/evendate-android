package ru.evendate.android.ui.checkin;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.network.ResponseArray;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 16.03.17.
 */

public class TicketsAdminPresenter implements CheckInContract.TicketAdminPresenter {
    private static final int LENGTH = 10;
    private static String LOG_TAG = TicketsAdminPresenter.class.getSimpleName();
    private DataRepository mDataRepository;
    private Disposable mDisposable;
    private CheckInContract.TicketsAdminView mView;

    TicketsAdminPresenter(@NonNull DataRepository dataRepository,
                          @NonNull CheckInContract.TicketsAdminView view) {
        mDataRepository = checkNotNull(dataRepository);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        //load(false, 0);
    }

    @Override
    public void stop() {
        if (mDisposable != null)
            mDisposable.dispose();
    }

    @Override
    public void reload(int eventId, boolean isCheckOut) {
        load(eventId, isCheckOut, true, 0);
    }

    @Override
    public void load(int eventId, boolean isCheckOut, boolean forceLoad, int page) {
        mView.setLoadingIndicator(forceLoad);
        String token = EvendateAccountManager.peekToken(mView.getContext());
        if (token == null) {
            mView.requestAuth().subscribe((String newToken) -> {
                if (newToken != null) {
                    startLoadList(newToken, eventId, isCheckOut, forceLoad, page);
                }
            });
        } else {
            startLoadList(token, eventId, isCheckOut, forceLoad, page);
        }
    }

    private void startLoadList(String token, int eventId, boolean isCheckOut, boolean forceLoad, int page) {
        mDisposable = mDataRepository.getTickets(token, eventId, isCheckOut, Ticket.GREEN_TYPE, page, LENGTH)
                .subscribe(result -> {
                            List<CheckInContract.TicketAdmin> list = new ArrayList<>(result.getData());
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

    @Override
    public void reload(int eventId, String query) {
        load(eventId, query, true, 0);
    }

    @Override
    public void load(int eventId, String query, boolean forceLoad, int page) {
        if (query.isEmpty()) {
            mView.showEmptyState();
            return;
        }
        //todo token
        Observable<ResponseArray<Ticket>> observable;
        String token = EvendateAccountManager.peekToken(mView.getContext());
        if (Pattern.matches("[\\d\\s]*", query)) {
            query = query.replaceAll("[\\s]", "");
            observable = mDataRepository.getTicketsByNumber(token, eventId, query, Ticket.GREEN_TYPE, page, LENGTH);
        } else {
            observable = mDataRepository.getTicketsByName(token, eventId, query, Ticket.GREEN_TYPE, page, LENGTH);
        }
        mView.setLoadingIndicator(forceLoad);
        mDisposable = observable.subscribe(result -> {
                    List<CheckInContract.TicketAdmin> list = new ArrayList<>(result.getData());
                    boolean isLast = list.size() < LENGTH;
                    boolean isEmpty = list.size() == 0;
                    if (result.isOk()) {
                        if (isEmpty) {
                            mView.showSearchEmptyState();
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

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showError();
    }
}
