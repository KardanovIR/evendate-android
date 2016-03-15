package ru.evendate.android.sync;

import ru.evendate.android.models.DataModel;

/**
 * Created by Дмитрий on 02.11.2015.
 */
public class EvendateServiceResponseAttr<DataType extends DataModel> extends EvendateServiceResponse {
    private DataType data;

    public DataType getData() {
        return data;
    }
}
