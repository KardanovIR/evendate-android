package ru.evendate.android.data;

import android.content.Context;
import android.support.annotation.NonNull;

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
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Aedirn on 07.03.17.
 */

public class DataRepository {

    Context mContext;

    public DataRepository(@NonNull Context context) {
        mContext = context;
    }

    public Observable<ResponseArray<Ticket>> getTickets(int page, int pageLength) {

        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Ticket>> eventsObservable =
                evendateService.getTickets(EvendateAccountManager.peekToken(mContext),
                        Ticket.FIELDS_LIST, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTickets(int eventId, boolean checkOut, int page, int pageLength) {

        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Ticket>> eventsObservable =
                evendateService.getTickets(EvendateAccountManager.peekToken(mContext), eventId, checkOut,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTicketsByNumber(int eventId, String query, int page, int pageLength) {
        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Ticket>> eventsObservable =
                evendateService.getTicketsByNumber(EvendateAccountManager.peekToken(mContext), eventId, query,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTicketsByName(int eventId, String query, int page, int pageLength) {
        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Ticket>> eventsObservable =
                evendateService.getTicketsByName(EvendateAccountManager.peekToken(mContext), eventId, query,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> getTicket(int eventId, String ticketUuid) {

        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Ticket>> eventsObservable =
                evendateService.getTicket(EvendateAccountManager.peekToken(mContext), eventId, ticketUuid,
                        CheckInContract.TicketAdmin.params);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Ticket>> checkoutTicket(int eventId, String ticketUuid, boolean checkout) {

        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Ticket>> eventsObservable =
                evendateService.checkoutTicket(EvendateAccountManager.peekToken(mContext), eventId, ticketUuid,
                        checkout);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Event>> getRegisteredEvents(boolean future, int page, int pageLength) {

        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Event>> eventsObservable =
                evendateService.getEvents(EvendateAccountManager.peekToken(mContext), future, true,
                        EventRegistered.FIELDS_LIST, "", pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<Event>> getEventsAdmin(boolean future, int page, int pageLength) {

        ApiService evendateService = ApiFactory.getService(mContext);
        Observable<ResponseArray<Event>> eventsObservable =
                evendateService.getEventsAdmin(EvendateAccountManager.peekToken(mContext), future,
                        true, true, true, EventRegistered.FIELDS_LIST, "", pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<OrganizationCategory>> getCatalog(int cityId) {

        ApiService apiService = ApiFactory.getService(mContext);
        Observable<ResponseArray<OrganizationCategory>> observable =
                apiService.getCatalog(EvendateAccountManager.peekToken(mContext),
                        OrganizationCategory.FIELDS_LIST, cityId);

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<City>> getCities() {

        ApiService apiService = ApiFactory.getService(mContext);
        Observable<ResponseArray<City>> observable =
                apiService.getCities(EvendateAccountManager.peekToken(mContext), "");

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<ResponseArray<City>> getNearestCities(double latitude, double longitude) {

        ApiService apiService = ApiFactory.getService(mContext);
        Observable<ResponseArray<City>> observable =
                apiService.getCities(EvendateAccountManager.peekToken(mContext), City.FIELDS_LIST,
                        latitude, longitude, City.ORDER_BY_DISTANCE);

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
