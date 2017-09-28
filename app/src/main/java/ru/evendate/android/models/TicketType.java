package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.Date;

import ru.evendate.android.ui.utils.DateUtils;

/**
 * Created by Aedirn on 09.03.17.
 */
@Parcel
public class TicketType extends DataModel {
    public static final String FIELDS_LIST = "comment,created_at,updated_at,price," +
            "sell_start_date,sell_end_date,start_after_ticket_type_uuid,amount," +
            "min_count_per_user,max_count_per_user,is_selling";

    @SerializedName("uuid")
    String uuid;
    @SerializedName("event_id")
    int eventId;
    @SerializedName("type_code")
    String typeCode;
    @SerializedName("name")
    String name;

    @SerializedName("comment")
    String comment;
    @SerializedName("created_at")
    int createdAt;
    @SerializedName("updated_at")
    int updatedAt;
    @SerializedName("price")
    float price;
    @SerializedName("sell_start_date")
    int sellStartDate;
    @SerializedName("sell_end_date")
    int sellEndDate;
    @SerializedName("start_after_ticket_type_uuid")
    int startAfterTicketTypeUuid;
    @SerializedName("amount")
    int amount;
    @SerializedName("min_count_per_user")
    int minCountPerUser;
    @SerializedName("max_count_per_user")
    int maxCountPerUser;
    @SerializedName("is_selling")
    boolean isSelling;

    @Override
    public int getEntryId() {
        throw new IllegalArgumentException("TicketType has not id cause api restrictions");
    }

    public String getUuid() {
        return uuid;
    }

    public int getEventId() {
        return eventId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedAt() {
        return DateUtils.date(createdAt);
    }

    public Date getUpdatedAt() {
        return DateUtils.date(updatedAt);
    }

    public float getPrice() {
        return price;
    }

    public Date getSellStartDate() {
        return DateUtils.date(sellStartDate);
    }

    public Date getSellEndDate() {
        return DateUtils.date(sellEndDate);
    }

    public int getStartAfterTicketTypeUuid() {
        return startAfterTicketTypeUuid;
    }

    public int getAmount() {
        return amount;
    }

    public int getMinCountPerUser() {
        return minCountPerUser;
    }

    public int getMaxCountPerUser() {
        return maxCountPerUser;
    }

    public boolean isSelling() {
        return isSelling;
    }
}
