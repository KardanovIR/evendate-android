package ru.getlect.evendate.evendate.sync;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Path;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntryWithEvents;
import ru.getlect.evendate.evendate.sync.dataTypes.TagResponse;

/**
 * Created by Dmitry on 18.10.2015.
 */
public interface EvendateService {

    @GET("/api/organizations?with_subscriptions=true")
    Call<EvendateServiceResponseArray<OrganizationEntry>> organizationData(@Header("Authorization") String authorization);

    @GET("/api/tags")
    Call<EvendateServiceResponseAttr<TagResponse>> tagData(@Header("Authorization") String authorization);

    @GET("/api/organizations/{id}?with_events=true")
    Call<EvendateServiceResponseAttr<OrganizationEntryWithEvents>> organizationWithEventsData(@Path("id") int organizationId, @Header("Authorization") String authorization);
    //@GET("/api/events/my")
    //Call<EvendateServiceResponse<TagEntry>> eventData(@Header("Authorization") String authorization);
//viceResponse<TagList>> tagData(@Header("Authorization") String authorization);

    //@GET("/api/users/friends")
    //Call<EvendateServiceResponse<TagEntry>> eventData(@Header("Authorization") String authorization);

}