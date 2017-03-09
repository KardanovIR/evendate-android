package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Aedirn on 06.03.17.
 */

@Parcel
public class Ticket extends DataModel {
    public static final String FIELDS_LIST = "ticket_order_uuid,status,checked_out,price," +
            "created_at,updated_at,ticket_type,order";
    public static final String ORDER_BY = "-created_at";


    @SerializedName("uuid")
    String uuid;
    @SerializedName("user_id")
    int userId;
    @SerializedName("type_code")
    String typeCode;
    @SerializedName("ticket_type_uuid")
    String ticketTypeUuid;

    @SerializedName("ticket_order_uuid")
    String ticketOrderUuid;
    @SerializedName("status")
    boolean status;
    @SerializedName("checked_out")
    boolean checkedOut;
    @SerializedName("price")
    float price;
    @SerializedName("created_at")
    long createdAt;
    @SerializedName("updated_at")
    long updatedAt;

    @SerializedName("ticket_type")
    TicketType ticketType;
    @SerializedName("order")
    Order order;
    @SerializedName("user")
    User user;

    @Override
    public int getEntryId() {
        throw new IllegalArgumentException("Ticket has not id cause api restrictions");
    }

    public String getUuid() {
        return uuid;
    }

    public int getUserId() {
        return userId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getTicketTypeUuid() {
        return ticketTypeUuid;
    }

    public String getTicketOrderUuid() {
        return ticketOrderUuid;
    }

    public boolean isStatus() {
        return status;
    }

    public boolean isCheckedOut() {
        return checkedOut;
    }

    public float getPrice() {
        return price;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public TicketType getTicketType() {
        return ticketType;
    }

    public Order getOrder() {
        return order;
    }

    public User getUser() {
        return user;
    }
}
