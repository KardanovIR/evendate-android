package ru.evendate.android.ui.utils;

import android.content.Context;
import android.os.Build;

import java.util.Locale;

/**
 * Created by Aedirn on 09.03.17.
 */

public class FormatUtils {
    @SuppressWarnings("deprecation")
    public static Locale getCurrentLocale(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().getLocales().get(0);
        } else {
            return context.getResources().getConfiguration().locale;
        }
    }
}
