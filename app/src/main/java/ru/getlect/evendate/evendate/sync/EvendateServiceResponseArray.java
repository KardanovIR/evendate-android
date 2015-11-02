package ru.getlect.evendate.evendate.sync;

import java.util.ArrayList;

import ru.getlect.evendate.evendate.sync.dataTypes.ResponseData;

/**
 * Created by Дмитрий on 02.11.2015.
 */
public class EvendateServiceResponseArray<DataType extends ResponseData> extends EvendateServiceResponse{

    private ArrayList<DataType> data;
    public ArrayList<DataType> getData() {
        return data;
    }
}
