package ru.getlect.evendate.evendate.utils;

import org.json.JSONObject;

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
        if(format.equals("JPG"))
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
}
