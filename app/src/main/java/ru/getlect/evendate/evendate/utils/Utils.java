package ru.getlect.evendate.evendate.utils;


import android.util.Log;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Dmitry on 21.10.2015.
 *
 * В классе собраны различные нужные функции, которых нет в фреймворке, а добавлять ради этого библиотеки нет смысла
 */
public class Utils {

    public static String getFileNameWithoutExtension(String filename){
        int pos = filename.lastIndexOf(".");
        if (pos > 0) {
            filename = filename.substring(0, pos);
        }
        return filename;
    }
    public static String getFileExtension(String filename){
        int pos = filename.lastIndexOf(".");
        if (pos > 0) {
            return filename.substring(pos + 1, filename.length());
        }
        return null;
    }
    public static String normalizeBitmapFormat(String format){
        format = format.toUpperCase();
        if(format.equals("JPG") || format.equals("JPE"))
            format = "JPEG";
        return format;
    }

    /** Return the value mapped by the given key, or {@code null} if not present or null.
     *  http://stackoverflow.com/questions/18226288/json-jsonobject-optstring-returns-string-null
     */
    public static String optString(JSONObject json, String key)
    {
        // http://code.google.com/p/android/issues/detail?id=13830
        return json.isNull(key) ? null : json.optString(key, null);
    }
    public static Integer optInt(JSONObject json, String key)
    {
        // http://code.google.com/p/android/issues/detail?id=13830
        return json.isNull(key) ? null : json.optInt(key, 0);
    }

    /**
     * convert string date cause api return days in such ways
     * @param str_date
     * @param formatter
     * @return
     */
    //"2015-10-26 00:00:00"
    public static Date formatDate(String str_date, SimpleDateFormat formatter){
        if(formatter == null)
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date;
        try {
            date = formatter.parse(str_date);
            return date;
        }catch (ParseException e){
            Log.e("format data", "error");
            e.printStackTrace();
        }
        return null;
    }
}
