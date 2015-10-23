package ru.getlect.evendate.evendate.utils;

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
}
