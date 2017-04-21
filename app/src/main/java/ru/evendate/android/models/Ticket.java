package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

import ru.evendate.android.ui.checkin.CheckInContract;

import static ru.evendate.android.models.Ticket.TicketParams.CHECKOUT;

/**
 * Created by Aedirn on 06.03.17.
 */

@Parcel
public class Ticket extends DataModel implements CheckInContract.TicketAdmin {
    public static final String FIELDS_LIST = Params.get(new String[]{
            TicketParams.TICKET_ORDER_UUID,
            CHECKOUT,
            TicketParams.PRICE,
            TicketParams.CREATED_AT,
            TicketParams.UPDATED_AT,
            TicketParams.TICKET_TYPE,
            TicketParams.ORDER,
            TicketParams.USER
    });
    public static final String ORDER_BY = "-created_at";
    @SerializedName("uuid")
    String uuid;
    @SerializedName("user_id")
    int userId;
    @SerializedName("type_code")
    String typeCode;
    @SerializedName("number")
    String number;
    @SerializedName("ticket_type_uuid")
    String ticketTypeUuid;
    @SerializedName("ticket_order_uuid")
    String ticketOrderUuid;
    @SerializedName(CHECKOUT)
    boolean checkout;
    @SerializedName("price")
    float price;
    @SerializedName("created_at")
    int createdAt;
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

    public String getNumber() {
        return number;
    }

    public String getTicketTypeUuid() {
        return ticketTypeUuid;
    }

    public String getTicketOrderUuid() {
        return ticketOrderUuid;
    }

    public boolean isCheckout() {
        return checkout;
    }

    public float getPrice() {
        return price;
    }

    public Date getCreatedAt() {
        return DateUtils.date(createdAt);
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

    public static class TicketParams extends Params {
        public static final String TICKET_ORDER_UUID = "ticket_order_uuid";
        public static final String CHECKOUT = "checkout";
        public static final String PRICE = "price";
        public static final String CREATED_AT = "created_at";
        public static final String UPDATED_AT = "updated_at";
        public static final String TICKET_TYPE = "ticket_type";
        public static final String ORDER = "order";
        public static final String USER = "user";
    }
}
