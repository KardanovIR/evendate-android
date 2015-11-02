package ru.getlect.evendate.evendate.sync;

import ru.getlect.evendate.evendate.sync.dataTypes.ResponseData;

/**
 * Created by Дмитрий on 02.11.2015.
 */
public class EvendateServiceResponseAttr<DataType extends ResponseData> extends EvendateServiceResponse {
    private DataType data;

    public DataType getData() {
        return data;
    }
}
