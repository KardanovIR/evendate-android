package ru.evendate.android.ui.checkin;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.data.DataSource;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Aedirn on 17.03.17.
 */

public class CheckInConfirmPresenter implements CheckInContract.TicketConfirmPresenter {
    private static String LOG_TAG = CheckInConfirmPresenter.class.getSimpleName();
    private DataSource mDataRepository;
    private Disposable mDisposable;
    private Disposable mConfirmDisposable;
    private CheckInContract.TicketConfirmView mView;
    private AppCompatActivity mContext;

    CheckInConfirmPresenter(AppCompatActivity context, @NonNull DataSource dataRepository,
                            @NonNull CheckInContract.TicketConfirmView view) {
        mDataRepository = checkNotNull(dataRepository);
        mView = checkNotNull(view);
        mView.setPresenter(this);
        mContext = context;
    }

    @Override
    public void start() {}

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
        String token = EvendateAccountManager.peekToken(mContext);
        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(newToken ->
                    startConfirm(newToken, ticketUuid, eventId, checkout));
        } else {
            startConfirm(token, ticketUuid, eventId, checkout);
        }
    }

    private void startConfirm(String token, String ticketUuid, int eventId, boolean checkout) {
        mConfirmDisposable = mDataRepository.checkoutTicket(token, eventId, ticketUuid, checkout)
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
        String token = EvendateAccountManager.peekToken(mContext);
        if (token == null) {
            mView.requestAuth().subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe(newToken ->
                    startLoadTicket(newToken, ticketUuid, eventId));
        } else {
            startLoadTicket(token, ticketUuid, eventId);
        }
    }

    private void startLoadTicket(String token, String ticketUuid, int eventId) {
        mDisposable = mDataRepository.getTicket(token, eventId, ticketUuid)
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

    private void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mView.showTicketLoadingError();
    }

}
