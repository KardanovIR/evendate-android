package ru.evendate.android.network;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import ru.evendate.android.models.Action;
import ru.evendate.android.models.DateCalendar;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.models.OrganizationCategory;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.Settings;
import ru.evendate.android.models.StatisticsEvent;
import ru.evendate.android.models.UserDetail;
import rx.Observable;

/**
 * Created by Dmitry on 18.10.2015.
 */
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
    Observable<ResponseArray<EventDetail>> getFeed(
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
    Observable<ResponseArray<EventDetail>> getEvent(
            @Header("Authorization") String authorization,
            @Path("id") int eventId,
            @Query("fields") String fields
    );

    /**
     * Get favorite event list
     */
    @GET(API_PATH + "/events/favorites")
    Observable<ResponseArray<EventDetail>> getFavorite(
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
    Observable<ResponseArray<EventDetail>> getFeed(
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
    Observable<ResponseArray<EventDetail>> getRecommendations(
            @Header("Authorization") String authorization,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
    );

    @POST(API_PATH + "/events/{id}/favorites")
    Observable<Response> eventPostFavorite(
            @Path("id") int eventId, @Header("Authorization") String authorization
    );

    @DELETE(API_PATH + "/events/{id}/favorites")
    Observable<Response> eventDeleteFavorite(
            @Path("id") int eventId, @Header("Authorization") String authorization
    );
    @POST(API_PATH + "/events/{id}/favorites")
    Observable<Response> likeEvent(
            @Path("id") int eventId, @Header("Authorization") String authorization
    );

    @DELETE(API_PATH + "/events/{id}/favorites")
    Observable<Response> dislikeEvent(
            @Path("id") int eventId, @Header("Authorization") String authorization
    );

    /**
     * Get events in organization
     */
    @GET(API_PATH + "/events")
    Observable<ResponseArray<EventDetail>> getEvents(
            @Header("Authorization") String authorization,
            @Query("organization_id") int organizationId,
            @Query("future") boolean future,
            @Query("fields") String fields,
            @Query("order_by") String orderBy,
            @Query("length") int length,
            @Query("offset") int offset
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

    /**
     * Get feed event list
     */
    @GET(API_PATH + "/organizations/types")
    Observable<ResponseArray<OrganizationCategory>> getCatalog(
            @Header("Authorization") String authorization,
            @Query("fields") String fields
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
            @Path("id") int organizationId,
            @Query("hidden") boolean hidden
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
}