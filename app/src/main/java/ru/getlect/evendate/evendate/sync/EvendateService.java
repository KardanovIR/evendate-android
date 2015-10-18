package ru.getlect.evendate.evendate.sync;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntry;

/**
 * Created by Dmitry on 18.10.2015.
 */
public interface EvendateService {

    @GET("/api/organizations?with_subscriptions=true")
    Call<EvendateServiceResponse<OrganizationEntry>> organizationData(@Header("Authorization") String authorization);

    @GET("/api/tags")
    Call<EvendateServiceResponse<TagList>> tagData(@Header("Authorization") String authorization);

    //@GET("/api/tags")
    //Call<EvendateServiceResponse<TagEntry>> eventData(@Header("Authorization") String authorization);

}