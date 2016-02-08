package ru.evendate.android.sync;

import retrofit.Call;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import ru.evendate.android.sync.models.DateCalendar;
import ru.evendate.android.sync.models.EventDetail;
import ru.evendate.android.sync.models.OrganizationDetail;
import ru.evendate.android.sync.models.OrganizationModel;
import ru.evendate.android.sync.models.OrganizationType;

/**
 * Created by Dmitry on 18.10.2015.
 */
public interface EvendateService {
    String API_PATH = "/api/v1";

    //@GET("/api/tags")
    //Call<EvendateServiceResponseAttr<TagResponse>> tagData(
    //        @Header("Authorization") String authorization,
    //        @Query("page") int page,
    //        @Query("length") int length
    //);

    @GET(API_PATH + "/events/dates")
    Call<EvendateServiceResponseArray<DateCalendar>> getCalendarDates(
            @Header("Authorization") String authorization,
            @Query("unique") boolean unique,
            @Query("since") String since,
            @Query("fields") String fields
    );

    /**
     * Get feed event list
     */
    @GET(API_PATH + "/events/my")
    Call<EvendateServiceResponseArray<EventDetail>> getFeed(
            @Header("Authorization") String authorization,
            @Query("future") boolean future
    );

    /**
     * Get concrete event
     */
    @GET(API_PATH + "/events/{id}")
    Call<EvendateServiceResponseArray<EventDetail>> eventData(
            @Path("id") int eventId,
            @Header("Authorization") String authorization,
            @Query("fields") String fields
    );

    /**
     * Get favorite event list
     */
    @GET(API_PATH + "/events/favorites")
    Call<EvendateServiceResponseArray<EventDetail>> getFavorite(
            @Header("Authorization") String authorization
    );

    @POST(API_PATH + "/events/{id}/favorites")
    Call<EvendateServiceResponse> eventPostFavorite(
            @Query("id") int eventId, @Header("Authorization") String authorization
    );

    @DELETE(API_PATH + "/events/{id}/favorites")
    Call<EvendateServiceResponse> eventDeleteFavorite(
            @Path("id") int eventId, @Header("Authorization") String authorization
    );

    /**
     * Get events in organization
     */
    @GET(API_PATH + "/events")
    Call<EvendateServiceResponseArray<EventDetail>> getEvents(
            @Header("Authorization") String authorization,
            @Query("organization_id") int organizationId,
            @Query("future") boolean future,
            @Query("fields") String fields
    );

    /**
     * Get events by date
     */
    @GET(API_PATH + "/events")
    Call<EvendateServiceResponseArray<EventDetail>> getEvents(
            @Header("Authorization") String authorization,
            @Query("date") String date,
            @Query("future") boolean future,
            @Query("fields") String fields
    );
    //@GET("/api/users/friends")
    //Call<EvendateServiceResponseArray<UserModel>> friendsData(
    //        @Header("Authorization") String authorization,
    //        @Query("page") int page,
    //        @Query("length") int length
    //);
//
    //@GET("/api/users/me")
    //Call<EvendateServiceResponseAttr<UserModel>> meData(
    //        @Header("Authorization") String authorization
    //);

    @GET(API_PATH + "/organizations/{id}")
    Call<EvendateServiceResponseArray<OrganizationDetail>> getOrganization(
            @Header("Authorization") String authorization,
            @Path("id") int organizationId,
            @Query("fields") String fields
    );

    /**
     * Get subscriptions
     */
    @GET(API_PATH + "/organizations/subscriptions")
    Call<EvendateServiceResponseArray<OrganizationModel>> getSubscriptions(
            @Header("Authorization") String authorization
    );
    @POST(API_PATH + "/organizations/{id}/subscriptions")
    Call<EvendateServiceResponse> organizationPostSubscription(
            @Path("id") int organizationId,
            @Header("Authorization") String authorization
    );

    @DELETE(API_PATH + "/organizations/{id}/subscriptions")
    Call<EvendateServiceResponse> organizationDeleteSubscription(
            @Path("id") int organizationId, @Header("Authorization") String authorization
    );

    /**
     * Get feed event list
     */
    @GET(API_PATH + "/organizations/types")
    Call<EvendateServiceResponseArray<OrganizationType>> getCatalog(
            @Header("Authorization") String authorization
    );

    @PUT(API_PATH + "/users/me/devices")
    Call<EvendateServiceResponse> putDeviceToken(
            @Query("device_token") String deviceToken,
            @Query("client_type") String clientType,
            @Header("Authorization") String authorization
    );
}