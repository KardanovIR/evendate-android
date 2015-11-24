package ru.getlect.evendate.evendate.sync;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Response;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;
import ru.getlect.evendate.evendate.sync.models.FriendModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModelWithEvents;
import ru.getlect.evendate.evendate.sync.models.TagResponse;

/**
 * Отвечает за обработку полученных данных от retrofit
 * Created by Dmitry on 18.10.2015.
 */
public class ServerDataFetcher{
    //TODO нз как сделать без дублирования кода, потому что в интерфейсе явно указывается класс,
    // в который парсится json
    // ПАРАМЕТРИЗОВАТЬ
    public static ArrayList<DataModel> getOrganizationData(EvendateService evendateService, String basicAuth) {
        Call<EvendateServiceResponseArray<OrganizationModel>> call = evendateService.organizationData(basicAuth);

        try{
            Response<EvendateServiceResponseArray<OrganizationModel>> response = call.execute();
            if(response.isSuccess()){
                ArrayList<DataModel> dataList = new ArrayList<>();
                dataList.addAll(response.body().getData());
                return dataList;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static ArrayList<DataModel> getTagData(EvendateService evendateService, String basicAuth){
        Call<EvendateServiceResponseAttr<TagResponse>> call = evendateService.tagData(basicAuth, EvendateSyncAdapter.PAGE, EvendateSyncAdapter.ENTRY_LIMIT);
        try{
            Response<EvendateServiceResponseAttr<TagResponse>> response = call.execute();
            if(response.isSuccess()){
                ArrayList<DataModel> dataList = new ArrayList<>();
                TagResponse tagResponse = response.body().getData();
                dataList.addAll(tagResponse.getTags());
                return dataList;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static DataModel getOrganizationWithEventsData(EvendateService evendateService, String basicAuth, int organizationId) {
        Call<EvendateServiceResponseAttr<OrganizationModelWithEvents>> call =
                evendateService.organizationWithEventsData(organizationId, basicAuth);
        try{
            Response<EvendateServiceResponseAttr<OrganizationModelWithEvents>> response = call.execute();
            if(response.isSuccess()){
                return response.body().getData();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static DataModel getEventData(EvendateService evendateService, String basicAuth, int eventId){
        Call<EvendateServiceResponseAttr<EventModel>> call =
                evendateService.eventData(eventId, basicAuth);
        try{
            Response<EvendateServiceResponseAttr<EventModel>> response = call.execute();
            if(response.isSuccess()){
                return response.body().getData();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static ArrayList<DataModel> getEventsData(EvendateService evendateService, String basicAuth){
        Call<EvendateServiceResponseArray<EventModel>> call =
                evendateService.eventsData(basicAuth, EvendateSyncAdapter.PAGE, EvendateSyncAdapter.ENTRY_LIMIT);
        try{
            Response<EvendateServiceResponseArray<EventModel>> response = call.execute();
            if(response.isSuccess()){
                ArrayList<DataModel> dataList = new ArrayList<>();
                dataList.addAll(response.body().getData());
                return dataList;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static ArrayList<DataModel> getFriendsData(EvendateService evendateService, String basicAuth){
        Call<EvendateServiceResponseArray<FriendModel>> call =
                evendateService.friendsData(basicAuth, EvendateSyncAdapter.PAGE, EvendateSyncAdapter.ENTRY_LIMIT);
        try{
            Response<EvendateServiceResponseArray<FriendModel>> response = call.execute();
            if(response.isSuccess()){
                ArrayList<DataModel> dataList = new ArrayList<>();
                dataList.addAll(response.body().getData());
                return dataList;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static OrganizationModel organizationPostSubscription(EvendateService evendateService, String basicAuth, int organizationId){
        Call<EvendateServiceResponseAttr<OrganizationModel>> call =
                evendateService.organizationPostSubscription(organizationId, basicAuth);
        try{
            Response<EvendateServiceResponseAttr<OrganizationModel>> response = call.execute();
            if(response.isSuccess()){
                return response.body().getData();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static boolean organizationDeleteSubscription(EvendateService evendateService, String basicAuth, int subscriptionId){
        Call<EvendateServiceResponse> call =
                evendateService.organizationDeleteSubscription(subscriptionId, basicAuth);
        try{
            Response<EvendateServiceResponse> response = call.execute();
            if(response.isSuccess()){
                return true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }

    public static boolean eventPostFavorite(EvendateService evendateService, String basicAuth, int eventId){
        Call<EvendateServiceResponse> call =
                evendateService.eventPostFavorite(eventId, basicAuth);
        try{
            Response<EvendateServiceResponse> response = call.execute();
            if(response.isSuccess()){
                return true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
    public static boolean eventDeleteFavorite(EvendateService evendateService, String basicAuth, int eventId){
        Call<EvendateServiceResponse> call =
                evendateService.eventDeleteFavorite(eventId, basicAuth);
        try{
            Response<EvendateServiceResponse> response = call.execute();
            if(response.isSuccess()){
                return true;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return false;
    }
}
