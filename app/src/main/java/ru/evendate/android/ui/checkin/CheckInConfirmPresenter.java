package ru.evendate.android.ui.checkin;

import android.support.annotation.NonNull;
import android.util.Log;

import io.reactivex.disposables.Disposable;
import ru.evendate.android.data.DataRepository;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 17.03.17.
 */

public class CheckInConfirmPresenter implements CheckInContract.TicketConfirmPresenter {
    private static String LOG_TAG = CheckInConfirmPresenter.class.getSimpleName();
    private DataRepository mDataRepository;
    private Disposable mDisposable;
    private Disposable mConfirmDisposable;
    private CheckInContract.TicketConfirmView mView;

    CheckInConfirmPresenter(@NonNull DataRepository dataRepository,
                            @NonNull CheckInContract.TicketConfirmView view) {
        mDataRepository = checkNotNull(dataRepository);
        mView = checkNotNull(view);
        mView.setPresenter(this);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        if (mDisposable != null)
            mDisposable.dispose();
        if (mConfirmDisposable != null)
            mConfirmDisposable.dispose();
    }

    @Override
    public void confirm(String ticketUuid, int eventId, boolean checkout) {
        mView.showConfirmLoading(true);
        mConfirmDisposable = mDataRepository.checkoutTicket(eventId, ticketUuid, checkout)
                .subscribe(result -> {
                            if (result.isOk()) {
                                if (checkout) {
                                    mView.showConfirm();
                                } else {
                                    mView.showConfirmRevert();
                                }
                            } else {
                                mView.showConfirmError();
                            }
                        },
                        this::onConfirmError,
                        () -> mView.showConfirmLoading(false)
                );
    }

    private void onConfirmError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showConfirmError();
    }

    @Override
    public void loadTicket(String ticketUuid, int eventId) {
        mView.showTicketLoading(true);
        mDisposable = mDataRepository.getTicket(eventId, ticketUuid)
                .subscribe(result -> {
                            CheckInContract.TicketAdmin ticket = result.getData().get(0);
                            if (result.isOk()) {
                                mView.showTicket(ticket);
                            } else {
                                mView.showTicketLoadingError();
                            }
                        },
                        this::onError,
                        () -> mView.showTicketLoading(false)
                );
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showTicketLoadingError();
    }

}
