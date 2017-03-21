package ru.evendate.android.data;

import android.content.Context;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.models.City;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.ui.checkin.CheckInContract;

/**
 * Created by Aedirn on 07.03.17.
 */

public class DataRepository {
    private ApiService mService;
    private Context mContext;

    public DataRepository(@NonNull Context context) {
        mContext = context;
        mService = ApiFactory.getService(mContext);
    }

    public Observable<ResponseArray<Ticket>> getTickets(int page, int pageLength) {

        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTickets(EvendateAccountManager.peekToken(mContext),
                        Ticket.FIELDS_LIST, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTickets(int eventId, boolean checkOut, int page, int pageLength) {

        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTickets(EvendateAccountManager.peekToken(mContext), eventId, checkOut,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTicketsByNumber(int eventId, String query, int page, int pageLength) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTicketsByNumber(EvendateAccountManager.peekToken(mContext), eventId, query,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTicketsByName(int eventId, String query, int page, int pageLength) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTicketsByName(EvendateAccountManager.peekToken(mContext), eventId, query,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTicket(int eventId, String ticketUuid) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTicket(EvendateAccountManager.peekToken(mContext), eventId, ticketUuid,
                        CheckInContract.TicketAdmin.params);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> checkoutTicket(int eventId, String ticketUuid, boolean checkout) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.checkoutTicket(EvendateAccountManager.peekToken(mContext), eventId, ticketUuid,
                        checkout);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Event>> getRegisteredEvents(boolean future, int page, int pageLength) {
        Observable<ResponseArray<Event>> eventsObservable =
                mService.getEvents(EvendateAccountManager.peekToken(mContext), future, true,
                        EventRegistered.FIELDS_LIST, "", pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Event>> getEventsAdmin(boolean future, int page, int pageLength) {
        Observable<ResponseArray<Event>> eventsObservable =
                mService.getEventsAdmin(EvendateAccountManager.peekToken(mContext), future,
                        true, true, true, EventRegistered.FIELDS_LIST, "", pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<OrganizationCategory>> getCatalog(int cityId) {
        Observable<ResponseArray<OrganizationCategory>> observable =
                mService.getCatalog(EvendateAccountManager.peekToken(mContext),
                        OrganizationCategory.FIELDS_LIST, cityId);

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<City>> getCities() {
        Observable<ResponseArray<City>> observable =
                mService.getCities(EvendateAccountManager.peekToken(mContext), "");

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<City>> getNearestCities(double latitude, double longitude) {
        Observable<ResponseArray<City>> observable =
                mService.getCities(EvendateAccountManager.peekToken(mContext), City.FIELDS_LIST,
                        latitude, longitude, City.ORDER_BY_DISTANCE);

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
