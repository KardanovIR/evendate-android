package ru.evendate.android.models;

import java.util.List;

/**
 * Created by Aedirn on 07.03.17.
 */

public interface EventRegistered {
    String FIELDS_LIST = "location,tickets_count,tickets" + DataUtil.encloseFields(Ticket.FIELDS_LIST, Ticket.ORDER_BY);

    int getEntryId();

    String getTitle();

    String getLocation();

    long getNearestDate();

    List<Ticket> getTickets();

    int getTicketsCount();
}
