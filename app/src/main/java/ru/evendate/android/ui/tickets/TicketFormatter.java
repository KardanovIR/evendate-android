package ru.evendate.android.ui.tickets;

import java.text.ChoiceFormat;
import java.util.Locale;

/**
 * Created by Aedirn on 09.03.17.
 */

public class TicketFormatter {
    public static String formatTicketCount(Locale locale, int ticketCount, String formatString) {
        if (!locale.getLanguage().equals("ru"))
            return ticketCount + formatString;
        int count = ticketCount;
        if (ticketCount > 20)
            count = ticketCount % 20;
        double[] limits = {0, 1, 2, 5};
        String[] formats = {"ов", "", "а", "ов"};

        ChoiceFormat fmt = new ChoiceFormat(limits, formats);
        return ticketCount + " " + formatString + fmt.format(count);
    }
}
