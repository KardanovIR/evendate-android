package ru.evendate.android.models;

/**
 * Created by Aedirn on 07.03.17.
 */

class DataUtil {
    static String encloseFields(String fields) {
        return "{fields:'" + fields + "'}";
    }

    static String encloseFields(String fields, String orderBy) {
        return "{fields:'" + fields + "',order_by:'" + orderBy + "'}";
    }
}
