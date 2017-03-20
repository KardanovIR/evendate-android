package ru.evendate.android.ui.checkin;

import android.support.annotation.NonNull;
import android.util.Log;

import ru.evendate.android.data.DataRepository;
import rx.Subscription;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 17.03.17.
 */

public class CheckInConfirmPresenter implements CheckInContract.TicketConfirmPresenter {
    private static String LOG_TAG = CheckInConfirmPresenter.class.getSimpleName();
    private DataRepository mDataRepository;
    private Subscription mSubscription;
    private Subscription mConfirmSubscription;
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
        if (mSubscription != null)
            mSubscription.unsubscribe();
        if (mConfirmSubscription != null)
            mConfirmSubscription.unsubscribe();
    }

    @Override
    public void confirm(String ticketUuid, int eventId, boolean checkout) {
        mView.showConfirmLoading(true);
        mConfirmSubscription = mDataRepository.checkoutTicket(eventId, ticketUuid, checkout)
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
        mSubscription = mDataRepository.getTicket(eventId, ticketUuid)
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
