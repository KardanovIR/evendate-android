package ru.evendate.android.models;

/**
 * Created by Aedirn on 07.03.17.
 */

public class DataUtil {
    public static String encloseFields(String fields) {
        return "{fields:'" + fields + "'}";
    }

    public static String encloseFields(String fields, String orderBy) {
        return "{fields:'" + fields + "',order_by:'" + orderBy + "'}";
    }
}
