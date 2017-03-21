package ru.evendate.android.network;

import ru.evendate.android.models.DataModel;

/**
 * Created by Aedirn on 07.03.17.
 */

public class ResponseObject<DataType extends DataModel> extends Response {

    private DataType data;

    public DataType getData() {
        return data;
    }
}
