package ru.evendate.android.network;

import retrofit.Call;
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
import ru.evendate.android.models.Organization;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationType;
import ru.evendate.android.models.UserDetail;
import rx.Observable;

/**
 * Created by Dmitry on 18.10.2015.
 */
public interface ApiService {
    String API_PATH = "/api/v1";

    @GET(API_PATH + "/events/dates")
    Call<ResponseArray<DateCalendar>> getCalendarDates(
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
    Call<Response> eventPostFavorite(
            @Path("id") int eventId, @Header("Authorization") String authorization
    );

    @DELETE(API_PATH + "/events/{id}/favorites")
    Call<Response> eventDeleteFavorite(
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
    Call<ResponseArray<UserDetail>> getUser(
            @Header("Authorization") String authorization,
            @Path("id") int userId,
            @Query("fields") String fields
    );

    @GET(API_PATH + "/users/me")
    Call<ResponseArray<UserDetail>> getMe(
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
    Call<ResponseArray<Organization>> getSubscriptions(
            @Header("Authorization") String authorization
    );

    @POST(API_PATH + "/organizations/{id}/subscriptions")
    Call<Response> organizationPostSubscription(
            @Path("id") int organizationId,
            @Header("Authorization") String authorization
    );

    @DELETE(API_PATH + "/organizations/{id}/subscriptions")
    Call<Response> organizationDeleteSubscription(
            @Path("id") int organizationId, @Header("Authorization") String authorization
    );

    /**
     * Get feed event list
     */
    @GET(API_PATH + "/organizations/types")
    Call<ResponseArray<OrganizationType>> getCatalog(
            @Header("Authorization") String authorization,
            @Query("fields") String fields
    );

    @PUT(API_PATH + "/users/me/devices")
    Call<Response> putDeviceToken(
            @Query("device_token") String deviceToken,
            @Query("client_type") String clientType,
            @Query("model") String model,
            @Query("os_version") String OsVersion,
            @Header("Authorization") String authorization
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
}