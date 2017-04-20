package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Aedirn on 06.03.17.
 */
@SuppressWarnings("unused")
@Parcel
public class Order extends DataModel {
    public static final String FIELDS_LIST = "is_canceled,payed_at,canceled_at,status_name," +
            "status_id,created_at,updated_at";

    @SerializedName("uuid")
    String uuid;
    @SerializedName("event_id")
    int eventId;
    @SerializedName("user_id")
    int userId;
    @SerializedName("status_type_code")
    String statusTypeCode;
    @SerializedName("status_name")
    String statusName;
    @SerializedName("payed_at")
    int payedAt;

    @SerializedName("is_canceled")
    boolean isCanceled;
    @SerializedName("canceled_at")
    int canceledAt;
    @SerializedName("status_id")
    int statusId;
    @SerializedName("created_at")
    long createdAt;
    @SerializedName("updated_at")
    long updatedAt;
    @SerializedName("tickets")
    ArrayList<Ticket> tickets;
    @SerializedName("user")
    User user;

    @Override
    public int getEntryId() {
        throw new IllegalArgumentException("Ticket has not id cause api restrictions");
    }

    public String getUuid() {
        return uuid;
    }

    public int getEventId() {
        return eventId;
    }

    public int getUserId() {
        return userId;
    }

    public String getStatusTypeCode() {
        return statusTypeCode;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public Date getPayedAt() {
        return DateUtils.date(payedAt);
    }

    public Date getCanceledAt() {
        return DateUtils.date(canceledAt);
    }

    public String getStatusName() {
        return statusName;
    }

    public int getStatusId() {
        return statusId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public ArrayList<Ticket> getTickets() {
        return tickets;
    }

    public User getUser() {
        return user;
    }
}
