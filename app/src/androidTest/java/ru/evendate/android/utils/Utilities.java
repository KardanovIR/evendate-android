package ru.evendate.android.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.test.AndroidTestCase;

import java.util.Locale;

/**
 * Created by Dmitry on 04.09.2015.
 */
public class Utilities extends AndroidTestCase {
    private Context mContext;

    public static Utilities newInstance(Context context) {
        Utilities utilities = new Utilities();
        utilities.mContext = context;
        return utilities;
    }

    public void setLocale(String language, String country) {
        Locale locale = new Locale(language, country);
        Locale.setDefault(locale);
        Resources res = mContext.getResources();
        Configuration config = res.getConfiguration();
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    public void setEnLocale() {
        setLocale("en", "GB");
    }

    public void setRuLocale() {
        setLocale("ru", "RU");
    }
}
