package ru.evendate.android;

import android.test.AndroidTestCase;

import ru.evendate.android.ui.utils.TicketFormatter;
import ru.evendate.android.utils.Utilities;

/**
 * Created by Aedirn on 17.03.17.
 */

public class TicketFormatterTest extends AndroidTestCase {

    public void testTicketNumberFormat() {
        Utilities utilities = Utilities.newInstance(getContext());
        utilities.setEnLocale();
        assertEquals("Ticket #111 222 333", TicketFormatter.formatNumber(getContext(), "111222333"));
        utilities.setRuLocale();
        assertEquals("Билет №111 222 333", TicketFormatter.formatNumber(getContext(), "111222333"));
    }

    public void testFormatCost() {
        Utilities utilities = Utilities.newInstance(getContext());
        utilities.setEnLocale();
        assertEquals("11,203", TicketFormatter.formatCost(getContext(), 11203f));
        utilities.setRuLocale();
        assertEquals("11 223 \u20BD", TicketFormatter.formatCost(getContext(), 11223f));
    }
}
