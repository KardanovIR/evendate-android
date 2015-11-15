package ru.getlect.evendate.evendate.sync;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Response;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntryWithEvents;
import ru.getlect.evendate.evendate.sync.dataTypes.TagEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.TagResponse;

/**
 * Отвечает за обработку получанных данных от retrofit
 * Created by Dmitry on 18.10.2015.
 */
public class ServerDataFetcher{
    //TODO нз как сделать без дублирования кода, потому что в интерфейсе явно указывается класс,
    // в который парсится json
    public static ArrayList<DataEntry> getOrganizationData(EvendateService evendateService, String basicAuth) {
        Call<EvendateServiceResponseArray<OrganizationEntry>> call = evendateService.organizationData(basicAuth);

        try{
            Response<EvendateServiceResponseArray<OrganizationEntry>> response = call.execute();
            if(response.isSuccess()){
                ArrayList<DataEntry> dataList = new ArrayList<>();
                dataList.addAll(response.body().getData());
                return dataList;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static ArrayList<DataEntry> getTagData(EvendateService evendateService, String basicAuth){
        Call<EvendateServiceResponseAttr<TagResponse>> call = evendateService.tagData(basicAuth);
        try{
            Response<EvendateServiceResponseAttr<TagResponse>> response = call.execute();
            if(response.isSuccess()){
                ArrayList<DataEntry> dataList = new ArrayList<>();
                TagResponse tagResponse = response.body().getData();
                dataList.addAll(tagResponse.getTags());
                return dataList;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
    public static DataEntry getOrganizationWithEventsData(EvendateService evendateService, String basicAuth, int organizationId) {
        Call<EvendateServiceResponseAttr<OrganizationEntryWithEvents>> call =
                evendateService.organizationWithEventsData(organizationId, basicAuth);
        try{
            Response<EvendateServiceResponseAttr<OrganizationEntryWithEvents>> response = call.execute();
            if(response.isSuccess()){
                return response.body().getData();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
