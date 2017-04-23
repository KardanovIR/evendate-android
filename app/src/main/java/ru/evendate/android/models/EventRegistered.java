package ru.evendate.android.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.evendate.android.network.ServiceUtils;

/**
 * Created by Aedirn on 07.03.17.
 */

public interface EventRegistered {
    String FIELDS_LIST = "dates" + ServiceUtils.encloseFields(EventDate.FIELDS_LIST) + "," +
            "location,my_tickets_count,tickets" + ServiceUtils.encloseFields(Ticket.FIELDS_LIST, Ticket.ORDER_BY);

    int getEntryId();

    String getTitle();

    String getLocation();

    Date getNearestDateTime();

    List<Ticket> getTickets();

    int getMyTicketsCount();

    ArrayList<EventDate> getDateList();
}
