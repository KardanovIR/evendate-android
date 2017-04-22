package ru.evendate.android.data;

import io.reactivex.Observable;
import ru.evendate.android.models.City;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ResponseArray;

/**
 * Created by Aedirn on 31.03.17.
 */

public interface DataSource {

    Observable<ResponseArray<Ticket>> getTickets(int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTickets(int eventId, boolean checkOut, int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTicketsByNumber(int eventId, String query, int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTicketsByName(int eventId, String query, int page, int pageLength);

    Observable<ResponseArray<Ticket>> getTicket(int eventId, String ticketUuid);

    Observable<ResponseArray<Ticket>> checkoutTicket(int eventId, String ticketUuid, boolean checkout);

    Observable<ResponseArray<Event>> getRegisteredEvents(boolean future, int page, int pageLength);

    Observable<ResponseArray<Event>> getEventsAdmin(boolean future, int page, int pageLength);

    Observable<ResponseArray<OrganizationCategory>> getCatalog(int cityId);

    Observable<ResponseArray<City>> getCities();

    Observable<ResponseArray<City>> getNearestCities(double latitude, double longitude);

    Observable<ResponseArray<Event>> getRecommendations(int page, int pageLength);

    Observable<Response> faveEvent(int eventId);

    Observable<Response> unfaveEvent(int eventId);

    Observable<Response> hideEvent(int eventId);

    Observable<Response> unhideEvent(int eventId);
}
