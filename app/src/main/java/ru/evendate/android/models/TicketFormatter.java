package ru.evendate.android.models;

import android.content.Context;

import java.text.ChoiceFormat;
import java.util.Locale;

import ru.evendate.android.R;
import ru.evendate.android.ui.FormatUtils;

/**
 * Created by Aedirn on 16.03.17.
 */

public class TicketFormatter {
    public static String formatNumber(Context context, String number) {
        return context.getString(R.string.ticket_number_word) + " " +
                getNumberSymbol(FormatUtils.getCurrentLocale(context)) +
                number.substring(0, 3) + " " + number.substring(3, 6) + " " + number.substring(6, 9);
    }

    public static String formatTicketCount(Locale locale, int ticketCount, String formatString) {
        if (!locale.getLanguage().equals("ru"))
            return ticketCount + " " + formatString;
        int count = ticketCount;
        if (ticketCount > 20)
            count = ticketCount % 20;
        double[] limits = {0, 1, 2, 5};
        String[] formats = {"ов", "", "а", "ов"};

        ChoiceFormat fmt = new ChoiceFormat(limits, formats);
        return ticketCount + " " + formatString + fmt.format(count);
    }

    private static String getNumberSymbol(Locale locale) {
        if (locale.getLanguage().equals("ru"))
            return "№";
        return "#";
    }
}
