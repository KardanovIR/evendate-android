package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Aedirn on 09.03.17.
 */
@Parcel
@SuppressWarnings("WeakerAccess")
public class TicketOrder extends Ticket {
    @SerializedName("count")
    int count;

    public TicketOrder() {}

    public TicketOrder(String uuid, int count) {
        this.uuid = uuid;
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
