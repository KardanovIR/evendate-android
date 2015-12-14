package ru.evendate.android.sync;

import java.util.ArrayList;

import ru.evendate.android.sync.models.ResponseData;

/**
 * Created by Дмитрий on 02.11.2015.
 */
public class EvendateServiceResponseArray<DataType extends ResponseData> extends EvendateServiceResponse{

    private ArrayList<DataType> data;
    public ArrayList<DataType> getData() {
        return data;
    }
}
