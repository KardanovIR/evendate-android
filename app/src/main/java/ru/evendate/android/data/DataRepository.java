package ru.evendate.android.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Calendar;
import java.util.Date;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.models.City;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.models.User;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.ui.checkin.CheckInContract;

/**
 * Created by Aedirn on 07.03.17.
 */

public class DataRepository implements DataSource {
    private ApiService mService;
    private Context mContext;

    public DataRepository(@NonNull Context context) {
        mContext = context;
        mService = ApiFactory.getService(mContext);
    }

    @Override
    public Observable<ResponseArray<Ticket>> getTickets(@NonNull String token, String type, int page, int pageLength) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTickets(token, type, Ticket.FIELDS_LIST,
                        Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Ticket>> getTickets(@NonNull String token, int eventId, boolean checkOut, String type, int page, int pageLength) {

        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTickets(token, eventId, checkOut, type,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Ticket>> getTicketsByNumber(@NonNull String token, int eventId, String query, String type, int page, int pageLength) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTicketsByNumber(token, eventId, query, type,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Ticket>> getTicketsByName(@NonNull String token, int eventId, String query, String type, int page, int pageLength) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTicketsByName(token, eventId, query, type,
                        CheckInContract.TicketAdmin.params, Ticket.ORDER_BY, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Ticket>> getTicket(@NonNull String token, int eventId, String ticketUuid) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.getTicket(token, eventId, ticketUuid,
                        CheckInContract.TicketAdmin.params);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Ticket>> checkoutTicket(@NonNull String token, int eventId, String ticketUuid, boolean checkout) {
        Observable<ResponseArray<Ticket>> eventsObservable =
                mService.checkoutTicket(token, eventId, ticketUuid, checkout);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getRegisteredEvents(@NonNull String token, boolean future, int page, int pageLength) {
        Observable<ResponseArray<Event>> eventsObservable =
                mService.getEvents(token, future, true,
                        EventRegistered.FIELDS_LIST, Event.ORDER_BY_LAST_DATE, pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getEventsAdmin(@NonNull String token, boolean future, int page, int pageLength) {
        Observable<ResponseArray<Event>> eventsObservable =
                mService.getEventsAdmin(token, future,
                        true, true, true, EventRegistered.FIELDS_LIST, "", pageLength, pageLength * page);

        return eventsObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<OrganizationCategory>> getCatalog(@Nullable String token, int cityId) {
        Observable<ResponseArray<OrganizationCategory>> observable =
                mService.getCatalog(token,
                        OrganizationCategory.FIELDS_LIST, cityId, true);

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<City>> getCities(@Nullable String token) {
        Observable<ResponseArray<City>> observable =
                mService.getCities(token, "");

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<City>> getNearestCities(@Nullable String token, double latitude, double longitude) {
        Observable<ResponseArray<City>> observable =
                mService.getCities(token, City.FIELDS_LIST,
                        latitude, longitude, City.ORDER_BY_DISTANCE);

        return observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getRecommendations(@Nullable String token, int page, int pageLength) {
        return mService.getRecommendations(token,
                true, Event.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getEvent(@Nullable String token, int eventId) {
        return mService.getEvent(token, eventId, Event.FIELDS_LIST)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Response> faveEvent(@NonNull String token, int eventId) {
        return mService.eventPostFavorite(token, eventId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Response> unfaveEvent(@NonNull String token, int eventId) {
        return mService.eventDeleteFavorite(token, eventId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Response> hideEvent(@NonNull String token, int eventId) {
        return mService.hideEvent(token, eventId, true)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Response> unhideEvent(@NonNull String token, int eventId) {
        return mService.hideEvent(token, eventId, false)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<OrganizationFull>> getOrg(@Nullable String token, int orgId) {
        return mService.getOrganization(token, orgId, OrganizationDetail.FIELDS_LIST)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Response> subscribeOrg(@NonNull String token, int orgId) {
        return mService.orgPostSubscription(orgId, token)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<Response> unSubscribeOrg(@NonNull String token, int orgId) {
        return mService.orgDeleteSubscription(orgId, token)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<UserDetail>> getUser(@Nullable String token, int userId) {
        return mService.getUser(token, userId, UserDetail.FIELDS_LIST)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<UserDetail>> searchUser(@Nullable String token, String query, int page, int pageLength) {
        return mService.findUsers(token, query, UserDetail.FIELDS_LIST, User.SEARCH_ORDER_BY, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<OrganizationFull>> getOrgRecommendations(@NonNull String token, int page, int pageLength) {
        return mService.getOrgRecommendations(token, OrganizationDetail.FIELDS_LIST, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<OrganizationFull>> searchOrgs(@Nullable String token, String query, int page, int pageLength) {
        return mService.findOrganizations(token, query,
                OrganizationSubscription.FIELDS_LIST, OrganizationSubscription.SEARCH_ORDER_BY);
    }

    @Override
    public Observable<ResponseArray<UserDetail>> getFriends(@NonNull String token, int page, int pageLength) {
        return mService.getFriends(token, UserDetail.FIELDS_LIST, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getFeed(@NonNull String token, int page, int pageLength) {
        return mService.getFeed(token, true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getFeed(@Nullable String token, int cityId, int page, int pageLength) {
        return mService.getFeed(token, true, cityId, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getFavorite(@NonNull String token, int page, int pageLength) {
        return mService.getFavorite(token, true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getFavorite(@Nullable String token, int cityId, int page, int pageLength) {
        return mService.getFavorite(token, true, cityId, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getOrgEvents(@Nullable String token, int organizationId, int page, int pageLength) {
        return mService.getEvents(token,
                organizationId, true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getOrgPastEvents(@Nullable String token, int organizationId, int page, int pageLength) {
        String date = ServiceUtils.formatDateForServer(Calendar.getInstance().getTime());
        return mService.getEvents(token,
                organizationId, date, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_LAST_DATE, pageLength, pageLength * page)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> getCalendarEvents(String token, Date date, int page, int pageLength) {
        return mService.getFeed(token, ServiceUtils.formatDateRequestNotUtc(date), true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_ACTUALITY, pageLength, pageLength * page).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> searchEvents(@Nullable String token, String query, int page, int pageLength) {
        return mService.findEvents(token, query, true, EventFeed.FIELDS_LIST, EventFeed.SEARCH_ORDER_BY, pageLength, pageLength * page).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    public Observable<ResponseArray<Event>> searchByTagEvents(@Nullable String token, String query, int page, int pageLength) {
        return mService.findEventsByTags(token, query, true, EventFeed.FIELDS_LIST, EventFeed.ORDER_BY_FAVORITE_AND_FIRST_TIME, pageLength, pageLength * page).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
