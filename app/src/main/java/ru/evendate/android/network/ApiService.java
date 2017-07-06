package ru.evendate.android.network;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import ru.evendate.android.models.Action;
import ru.evendate.android.models.City;
import ru.evendate.android.models.DateCalendar;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.Registration;
import ru.evendate.android.models.Settings;
import ru.evendate.android.models.StatisticsEvent;
import ru.evendate.android.models.Tag;
import ru.evendate.android.models.Ticket;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 18.10.2015.
 */
@SuppressWarnings("SameParameterValue")
public interface ApiService {
    String API_PATH = "/api/v1";

    @GET(API_PATH + "/events/dates")
    Observable<ResponseArray<DateCalendar>> getCalendarDates(
            @Header("Authorization") String authorization,
            @Query("unique") boolean unique,
            @Query("my") boolean my,
            @Query("future") boolean future,
            @Query("fields") String fields
    );

    /**
     * Get feed event list
     */
    @GET(API_PATH + "/events/my")
    Observable<ResponseArray<Event>> getFeed(
            @Header("Authorization") String authorization,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    /**
     * Get concrete event
     */
    @GET(API_PATH + "/events/{id}")
    Observable<ResponseArray<Event>> getEvent(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("fields") String fields
    );

    /**
     * Get favorite event list
     */
    @GET(API_PATH + "/events/favorites")
    Observable<ResponseArray<Event>> getFavorite(
            @Header("Authorization") String authorization,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    /**
     * Get feed events by date
     */
    @GET(API_PATH + "/events/my")
    Observable<ResponseArray<Event>> getFeed(
            @Header("Authorization") String authorization,
            @Query("date") String date,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    /**
     * Get recommended events
     */
    @GET(API_PATH + "/events/recommendations")
    Observable<ResponseArray<Event>> getRecommendations(
            @Header("Authorization") String authorization,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @POST(API_PATH + "/events/{id}/favorites")
    Observable<Response> eventPostFavorite(
            @Header("Authorization") String authorization,
            @Path("id") int eventId
    );

    @DELETE(API_PATH + "/events/{id}/favorites")
    Observable<Response> eventDeleteFavorite(
            @Header("Authorization") String authorization,
            @Path("id") int eventId
    );

    /**
     * Get events in organization
     */
    @GET(API_PATH + "/events")
    Observable<ResponseArray<Event>> getEvents(
            @Header("Authorization") String authorization,
            @Query("organization_id") int organizationId,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    /**
     * Get past events in organization
     */
    @GET(API_PATH + "/events")
    Observable<ResponseArray<Event>> getEvents(
            @Header("Authorization") String authorization,
            @Query("organization_id") int organizationId,
            @Query("till") String till,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/events")
    Observable<ResponseArray<Event>> getEvents(
            @Header("Authorization") String authorization,
            @Query("future") boolean future,
            @Query("registered") boolean registered,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/events")
    Observable<ResponseArray<Event>> getEventsAdmin(
            @Header("Authorization") String authorization,
            @Query("future") boolean future,
            @Query("can_edit") boolean canEdit,
            @Query("registration_locally") boolean registrationLocally,
            @Query("registration_required") boolean registrationRequired,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/events/tickets")
    Observable<ResponseArray<Ticket>> getTickets(
            @Header("Authorization") String authorization,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/statistics/events/{id}/tickets")
    Observable<ResponseArray<Ticket>> getTickets(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("checkout") boolean checkout,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/statistics/events/{id}/tickets")
    Observable<ResponseArray<Ticket>> getTicketsByNumber(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("number") String number,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/statistics/events/{id}/tickets")
    Observable<ResponseArray<Ticket>> getTicketsByName(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("user_name") String userName,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/statistics/events/{id}/tickets/{uuid}")
    Observable<ResponseArray<Ticket>> getTicket(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Path("uuid") String ticketUuid,
            @Query("fields") String fields
    );

    @PUT(API_PATH + "/statistics/events/{id}/tickets/{uuid}")
    Observable<ResponseArray<Ticket>> checkoutTicket(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Path("uuid") String ticketUuid,
            @Query("checkout") boolean checkout
    );

    @GET(API_PATH + "/users/{id}")
    Observable<ResponseArray<UserDetail>> getUser(
            @Header("Authorization") String authorization,
            @Path("id") int userId,
            @Query("fields") String fields
    );

    @GET(API_PATH + "/users/me")
    Observable<ResponseArray<UserDetail>> getMe(
            @Header("Authorization") String authorization,
            @Query("fields") String fields
    );

    @GET(API_PATH + "/organizations/{id}")
    Observable<ResponseArray<OrganizationFull>> getOrganization(
            @Header("Authorization") String authorization,
            @Path("id") int organizationId,
            @Query("fields") String fields
    );

    /**
     * Get recommended orgs
     */
    @GET(API_PATH + "/organizations/recommendations")
    Observable<ResponseArray<OrganizationFull>> getOrgRecommendations(
            @Header("Authorization") String authorization,
            @Query("fields") String fields,
            @Query("length") int length,
            @Query("offset") int offset
    );

    /**
     * Get subscriptions
     */
    @GET(API_PATH + "/organizations/subscriptions")
    Observable<ResponseArray<OrganizationFull>> getSubscriptions(
            @Header("Authorization") String authorization,
            @Query("fields") String fields
    );

    @POST(API_PATH + "/organizations/{id}/subscriptions")
    Observable<Response> orgPostSubscription(
            @Path("id") int organizationId,
            @Header("Authorization") String authorization
    );

    @DELETE(API_PATH + "/organizations/{id}/subscriptions")
    Observable<Response> orgDeleteSubscription(
            @Path("id") int organizationId, @Header("Authorization") String authorization
    );

    @GET(API_PATH + "/organizations/types")
    Observable<ResponseArray<OrganizationCategory>> getCatalog(
            @Header("Authorization") String authorization,
            @Query("fields") String fields,
            @Query("city_id") int cityId
    );

    @GET(API_PATH + "/organizations/cities")
    Observable<ResponseArray<City>> getCities(
            @Header("Authorization") String authorization,
            @Query("fields") String fields
    );

    @GET(API_PATH + "/organizations/cities")
    Observable<ResponseArray<City>> getCities(
            @Header("Authorization") String authorization,
            @Query("fields") String fields,
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("order_by") String orderBy
    );

    @PUT(API_PATH + "/users/me/devices")
    Observable<Response> putDeviceToken(
            @Header("Authorization") String authorization,
            @Query("device_token") String deviceToken,
            @Query("client_type") String clientType,
            @Query("model") String model,
            @Query("os_version") String OsVersion
    );

    @GET(API_PATH + "/users/{id}/actions")
    Observable<ResponseArray<Action>> getActions(
            @Header("Authorization") String authorization,
            @Path("id") int userId,
            @Query("fields") String fields,
            @Query("order_by") String orderBy
    );

    @PUT(API_PATH + "/events/{id}/status")
    Observable<Response> hideEvent(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("hidden") boolean hidden
    );

    @POST(API_PATH + "/events/{id}/orders")
    Observable<ResponseObject<Registration>> postRegistration(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Body Registration registration_fields
    );

    //statistics
    @POST(API_PATH + "/statistics/batch")
    Observable<Response> postStat(
            @Header("Authorization") String authorization,
            @Body List<StatisticsEvent> payload
    );

    /**
     * Get notifications for event
     */
    @GET(API_PATH + "/events/{id}/notifications")
    Observable<ResponseArray<EventNotification>> getNotifications(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("fields") String fields
    );

    @POST(API_PATH + "/events/{id}/notifications")
    Observable<Response> setNotificationByTime(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("notification_time") String notificationTime
    );

    @POST(API_PATH + "/events/{id}/notifications")
    Observable<Response> setNotificationByType(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("notification_type") String notificationType
    );

    @DELETE(API_PATH + "/events/{id}/notifications/{uuid}")
    Observable<Response> deleteNotification(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Path("uuid") String uuid
    );

    /**
     * Get friends
     */
    @GET(API_PATH + "/users/friends")
    Observable<ResponseArray<UserDetail>> getFriends(
            @Header("Authorization") String authorization,
            @Query("fields") String fields
    );

    @GET(API_PATH + "/users/settings")
    Observable<ResponseArray<Settings>> getSettings(
            @Header("Authorization") String authorization,
            @Query("as_array") boolean asArray
    );

    @PUT(API_PATH + "/users/settings")
    Observable<Response> setSettings(
            @Header("Authorization") String authorization,
            @Query("show_to_friends") boolean feedPrivacy
    );


    @GET(API_PATH + "/organizations")
    Observable<ResponseArray<OrganizationFull>> findOrganizations(
            @Header("Authorization") String authorization,
            @Query("q") String query,
            @Query("fields") String fields,
            @Query("order_by") String orderBy
    );

    @GET(API_PATH + "/users")
    Observable<ResponseArray<UserDetail>> findUsers(
            @Header("Authorization") String authorization,
            @Query("name") String name,
            @Query("fields") String fields
    );

    @GET(API_PATH + "/events")
    Observable<ResponseArray<Event>> findEvents(
            @Header("Authorization") String authorization,
            @Query("q") String query,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/events")
    Observable<ResponseArray<Event>> findEventsByTags(
            @Header("Authorization") String authorization,
            @Query("tags") String queryTags,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @GET(API_PATH + "/tags")
    Observable<ResponseArray<Tag>> getTopTags(
            @Header("Authorization") String authorization,
            @Query("used_since") String usedSince,
            @Query("length") int length
    );
}