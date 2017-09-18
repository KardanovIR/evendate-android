package ru.evendate.android.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;

import io.reactivex.Observable;
import ru.evendate.android.models.City;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;

/**
 * Created by Aedirn on 31.03.17.
 */

public interface DataSource {

    Observable<ResponseArray<Ticket>> getTickets(@NonNull String token, int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTickets(@NonNull String token, int eventId, boolean checkOut, int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTicketsByNumber(@NonNull String token, int eventId, String query, int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTicketsByName(@NonNull String token, int eventId, String query, int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTicket(@NonNull String token, int eventId, String ticketUuid);

    Observable<ResponseArray<Ticket>> checkoutTicket(@NonNull String token, int eventId, String ticketUuid, boolean checkout);

    Observable<ResponseArray<Event>> getRegisteredEvents(@NonNull String token, boolean future, int page, int pageLength);

    Observable<ResponseArray<Event>> getEventsAdmin(@NonNull String token, boolean future, int page, int pageLength);

    Observable<ResponseArray<OrganizationCategory>> getCatalog(@Nullable String token, int cityId);

    Observable<ResponseArray<City>> getCities(@Nullable String token);

    Observable<ResponseArray<City>> getNearestCities(@Nullable String token, double latitude, double longitude);

    Observable<ResponseArray<Event>> getRecommendations(@Nullable String token, int page, int pageLength);

    Observable<ResponseArray<Event>> getEvent(@Nullable String token, int eventId);

    Observable<Response> faveEvent(@NonNull String token, int eventId);

    Observable<Response> unfaveEvent(@NonNull String token, int eventId);

    Observable<Response> hideEvent(@NonNull String token, int eventId);

    Observable<Response> unhideEvent(@NonNull String token, int eventId);

    Observable<ResponseArray<OrganizationFull>> getOrg(@Nullable String token, int orgId);

    Observable<Response> subscribeOrg(@NonNull String token, int orgId);

    Observable<Response> unSubscribeOrg(@NonNull String token, int orgId);

    Observable<ResponseArray<UserDetail>> getUser(@Nullable String token, int userId);

    Observable<ResponseArray<UserDetail>> searchUser(@Nullable String token, String query, int page, int pageLength);

    Observable<ResponseArray<OrganizationFull>> getOrgRecommendations(@NonNull String token, int page, int pageLength);

    Observable<ResponseArray<OrganizationFull>> searchOrgs(@Nullable String token, String query, int page, int pageLength);

    Observable<ResponseArray<UserDetail>> getFriends(@NonNull String token, int page, int pageLength);

    Observable<ResponseArray<Event>> getFeed(@NonNull String token, int page, int pageLength);

    Observable<ResponseArray<Event>> getFeed(@Nullable String token, int cityId, int page, int pageLength);

    Observable<ResponseArray<Event>> getFavorite(@NonNull String token, int page, int pageLength);

    Observable<ResponseArray<Event>> getFavorite(@Nullable String token, int cityId, int page, int pageLength);

    Observable<ResponseArray<Event>> getOrgEvents(@Nullable String token, int orgId, int page, int pageLength);

    Observable<ResponseArray<Event>> getOrgPastEvents(@Nullable String token, int orgId, int page, int pageLength);

    Observable<ResponseArray<Event>> getCalendarEvents(@Nullable String token, Date date, int page, int pageLength);

    Observable<ResponseArray<Event>> searchEvents(@Nullable String token, String query, int page, int pageLength);

    Observable<ResponseArray<Event>> searchByTagEvents(@Nullable String token, String query, int page, int pageLength);
}
