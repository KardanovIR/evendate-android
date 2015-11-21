package ru.getlect.evendate.evendate.sync;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import ru.getlect.evendate.evendate.sync.models.EventModel;
import ru.getlect.evendate.evendate.sync.models.FriendModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModelWithEvents;
import ru.getlect.evendate.evendate.sync.models.TagResponse;

/**
 * Created by Dmitry on 18.10.2015.
 */
public interface EvendateService {

    @GET("/api/organizations?with_subscriptions=true")
    Call<EvendateServiceResponseArray<OrganizationModel>> organizationData(@Header("Authorization") String authorization);

    @GET("/api/tags")
    Call<EvendateServiceResponseAttr<TagResponse>> tagData(@Header("Authorization") String authorization);

    @GET("/api/organizations/{id}?with_events=true")
    Call<EvendateServiceResponseAttr<OrganizationModelWithEvents>> organizationWithEventsData(@Path("id") int organizationId, @Header("Authorization") String authorization);

    @GET("/api/events/my")
    Call<EvendateServiceResponseArray<EventModel>> eventsData(@Header("Authorization") String authorization);

    @GET("/api/users/friends")
    Call<EvendateServiceResponseArray<FriendModel>> friendsData(@Header("Authorization") String authorization);

    @GET("/api/events/{id}")
    Call<EvendateServiceResponseAttr<EventModel>> eventData(@Path("id") int eventId, @Header("Authorization") String authorization);
}