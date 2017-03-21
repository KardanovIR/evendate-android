package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Aedirn on 09.03.17.
 */
@Parcel
public class TicketType extends DataModel {

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
    long createdAt;
    @SerializedName("updated_at")
    long updatedAt;
    @SerializedName("price")
    float price;
    @SerializedName("sell_start_date")
    long sellStartDate;
    @SerializedName("sell_end_date")
    long sellEndDate;
    @SerializedName("start_after_ticket_type_uuid")
    int startAfterTicketTypeUuid;
    @SerializedName("amount")
    int amount;
    @SerializedName("min_count_per_user")
    int minCountPerUser;
    @SerializedName("max_count_per_user")
    int maxCountPerUser;
    @SerializedName("promocode")
    int promocode;
    @SerializedName("promocode_effort")
    int promocodeEffort;

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

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public float getPrice() {
        return price;
    }

    public long getSellStartDate() {
        return sellStartDate;
    }

    public long getSellEndDate() {
        return sellEndDate;
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

    public int getPromocode() {
        return promocode;
    }

    public int getPromocodeEffort() {
        return promocodeEffort;
    }
}
