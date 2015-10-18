package ru.getlect.evendate.evendate.sync;

import java.util.ArrayList;

import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;

/**
 * Created by Dmitry on 18.10.2015.
 */
public class EvendateServiceResponse<DataType extends DataEntry> {
    boolean status;
    String text;
    private ArrayList<DataType> data;

    public ArrayList<DataType> getData() {
        return data;
    }
}

