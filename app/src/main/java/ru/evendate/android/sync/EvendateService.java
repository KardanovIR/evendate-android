package ru.evendate.android.sync;

import retrofit.Call;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;
import ru.evendate.android.sync.models.EventModel;
import ru.evendate.android.sync.models.FriendModel;
import ru.evendate.android.sync.models.OrganizationModel;
import ru.evendate.android.sync.models.OrganizationModelWithEvents;
import ru.evendate.android.sync.models.TagResponse;

/**
 * Created by Dmitry on 18.10.2015.
 */
public interface EvendateService {

    @GET("/api/organizations?with_subscriptions=true")
    Call<EvendateServiceResponseArray<OrganizationModel>> organizationData(
            @Header("Authorization") String authorization
    );

    @GET("/api/tags")
    Call<EvendateServiceResponseAttr<TagResponse>> tagData(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("length") int length
    );

    @GET("/api/organizations/{id}?with_events=true")
    Call<EvendateServiceResponseAttr<OrganizationModelWithEvents>> organizationWithEventsData(
            @Path("id") int organizationId,
            @Header("Authorization") String authorization
    );

    @GET("/api/events/my")
    Call<EvendateServiceResponseArray<EventModel>> eventsData(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("length") int length
    );
    @GET("/api/events/my")
    Call<EvendateServiceResponseArray<EventModel>> eventsData(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("length") int length,
            @Query("date") String date
    );

    @GET("/api/events")
    Call<EvendateServiceResponseArray<EventModel>> eventsData(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("length") int length,
            @Query("organization_id") int organizationId,
            @Query("type") String type
    );

    @GET("/api/users/friends")
    Call<EvendateServiceResponseArray<FriendModel>> friendsData(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("length") int length
    );

    @GET("/api/users/me")
    Call<EvendateServiceResponseAttr<FriendModel>> meData(
            @Header("Authorization") String authorization
    );

    @GET("/api/events/{id}")
    Call<EvendateServiceResponseAttr<EventModel>> eventData(
            @Path("id") int eventId,
            @Header("Authorization") String authorization);

    @GET("/api/subscriptions/my")
    Call<EvendateServiceResponseArray<OrganizationModel>> subscriptionData(
            @Header("Authorization") String authorization);

    @POST("/api/subscriptions")
    Call<EvendateServiceResponseAttr<OrganizationModel>> organizationPostSubscription(
            @Query("organization_id") int organizationId,
            @Header("Authorization") String authorization
    );

    @DELETE("/api/subscriptions/{id}")
    Call<EvendateServiceResponse> organizationDeleteSubscription(
            @Path("id") int subscriptionId, @Header("Authorization") String authorization
    );

    @GET("/api/events/favorites")
    Call<EvendateServiceResponseArray<EventModel>> favoritesEventData(
            @Header("Authorization") String authorization,
            @Query("page") int page,
            @Query("length") int length
    );
    @POST("/api/events/favorites")
    Call<EvendateServiceResponse> eventPostFavorite(
            @Query("event_id") int eventId, @Header("Authorization") String authorization
    );

    @DELETE("/api/events/favorites/{id}")
    Call<EvendateServiceResponse> eventDeleteFavorite(
            @Path("id") int eventId, @Header("Authorization") String authorization
    );

    @PUT("/api/users/device")
    Call<EvendateServiceResponse> putDeviceToken(
            @Query("device_token") String deviceToken,
            @Query("client_type") String clientType,
            @Header("Authorization") String authorization
    );
}