package ru.getlect.evendate.evendate.sync;

import java.io.IOException;
import java.util.ArrayList;

import retrofit.Call;
import retrofit.Response;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.TagEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.TagResponse;

/**
 * Отвечает за обработку получанных данных от retrofit
 * Created by Dmitry on 18.10.2015.
 */
public class ServerDataFetcher{
    private static final String basicAuth = "0ddb916680f9eb4e53138e2e276321c116a3b48f705570ebe23e3efc5a2ba803c6c65be4c582360688bc9f920c56a0b3447de7ea67sOyZlty3ruNhH4muJMqDq8IvsKAegwsRycTnb49eRiU1elPPk5b6EUm546lhW";

    //TODO нз как сделать без дублирования кода, потому что в интерфейсе явно указывается класс,
    // в который парсится json
    public static ArrayList<DataEntry> getOrganizationData(EvendateService evendateService) {
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
    public static ArrayList<DataEntry> getTagData(EvendateService evendateService){
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
}