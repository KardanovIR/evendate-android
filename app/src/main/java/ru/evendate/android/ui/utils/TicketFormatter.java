package ru.evendate.android.ui.utils;

import android.content.Context;

import java.text.ChoiceFormat;
import java.text.NumberFormat;
import java.util.Locale;

import ru.evendate.android.R;

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

    public static String formatCost(Context context, float cost) {
        String formattedCost = NumberFormat.getNumberInstance(FormatUtils.getCurrentLocale(context))
                .format(cost);
        if (FormatUtils.getCurrentLocale(context).getLanguage().equals("ru"))
            return formattedCost + " \u20BD";
        return formattedCost;
    }

    public static String formatTotalCost(Context context, float cost) {
        return context.getString(R.string.ticketing_form_total_cost)
                + " " + formatCost(context, cost);
    }
}
