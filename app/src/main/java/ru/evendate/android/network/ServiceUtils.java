package ru.evendate.android.network;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Dmitry on 13.11.2016.
 */

public class ServiceUtils {

    public static String formatDateForServer(Date d) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(d.getTime());
    }
}
