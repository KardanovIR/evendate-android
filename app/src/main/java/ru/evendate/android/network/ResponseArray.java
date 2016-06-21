package ru.evendate.android.network;

import java.util.ArrayList;

import ru.evendate.android.models.DataModel;

/**
 * Created by Дмитрий on 02.11.2015.
 */
public class ResponseArray<DataType extends DataModel> extends Response {

    private ArrayList<DataType> data;

    public ArrayList<DataType> getData() {
        return data;
    }
}
