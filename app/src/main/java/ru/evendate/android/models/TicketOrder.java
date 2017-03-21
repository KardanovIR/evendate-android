package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Aedirn on 09.03.17.
 */

public class TicketOrder extends Ticket {
    @SerializedName("count")
    int count;

    public TicketOrder(String uuid, int count) {
        this.uuid = uuid;
        this.count = count;
    }
}
