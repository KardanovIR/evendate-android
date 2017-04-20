package ru.evendate.android.models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Dmitry on 07.02.2016.
 */
@SuppressWarnings("unused")
@Parcel
public class Event extends DataModel implements EventFeed, EventRegistered {
    public static final String FIELDS_LIST = "location,latitude,longitude," +
            "image_horizontal_small_url,image_horizontal_medium_url,organization_name," +
            "organization_type_name,organization_short_name," +
            "organization_logo_large_url,organization_logo_medium_url,organization_logo_small_url," +
            "favored_users_count,description,detail_info_url,is_favorite,link," +

            "registration_required,registration_approvement_required,registration_limit_count," +
            "registration_locally,registration_till,registration_approved,registration_available," +
            "registered_count,registration_fields,is_registered,orders,tickets,my_tickets_count," +

            "is_free,min_price,is_same_time,created_at," +

            "dates" + DataUtil.encloseFields(EventDate.FIELDS_LIST) + ",tags," +
            "favored{fields:\'" + User.FIELDS_LIST + "\'}";

    @SerializedName("id")
    int eventId;
    String title;
    @SerializedName("first_event_date")
    int firstDateTime;
    @SerializedName("last_event_date")
    int lastDateTime;
    @SerializedName("nearest_event_date")
    @Nullable
    Integer nearestDateTime;
    @SerializedName("image_horizontal_url")
    String imageHorizontalUrl;
    @SerializedName("image_vertical_url")
    String imageVerticalUrl;
    @SerializedName("organization_id")
    int organizationId;

    String location;
    double latitude;
    double longitude;
    @SerializedName("image_horizontal_small_url")
    String imageHorizontalSmallUrl;
    @SerializedName("image_horizontal_medium_url")
    String imageHorizontalMediumUrl;
    @SerializedName("organization_name")
    String organizationName;
    @SerializedName("organization_type_name")
    String organizationTypeName;
    @SerializedName("organization_short_name")
    String organizationShortName;
    @SerializedName("organization_logo_large_url")
    String organizationLogoUrl;
    @SerializedName("organization_logo_medium_url")
    String organizationLogoMediumUrl;
    @SerializedName("organization_logo_small_url")
    String organizationLogoSmallUrl;
    @SerializedName("favored_users_count")
    int likedUsersCount;
    String description;
    @SerializedName("detail_info_url")
    @Nullable
    String detailInfoUrl;
    @SerializedName("is_favorite")
    boolean isFavorite;
    @SerializedName("link")
    String link;

    @SerializedName("registration_required")
    boolean registrationRequired;
    @SerializedName("registration_approvement_required")
    boolean registrationApprovementRequired;
    @SerializedName("registration_limit_count")
    int registrationLimitCount;
    @SerializedName("registration_locally")
    boolean registrationLocally;
    @SerializedName("registration_till")
    int registrationTill;
    @SerializedName("registration_approved")
    boolean registrationApproved;
    @SerializedName("registration_available")
    boolean registrationAvailable;
    @SerializedName("registered_count")
    int registeredCount;
    @SerializedName("is_registered")
    boolean registered;
    @SerializedName("orders")
    ArrayList<Order> orders;
    @SerializedName("my_tickets_count")
    int myTicketsCount;
    @SerializedName("tickets_count")
    int ticketsCount;
    @SerializedName("tickets")
    ArrayList<Ticket> tickets;

    @SerializedName("is_free")
    boolean isFree;
    @SerializedName("min_price")
    int minPrice;
    @SerializedName("is_same_time")
    boolean isSameTime;

    @SerializedName("registration_fields")
    ArrayList<RegistrationField> registrationFields;

    @SerializedName("tags")
    ArrayList<Tag> tagList;
    @SerializedName("dates")
    ArrayList<EventDate> dateList;
    @SerializedName("favored")
    ArrayList<UserDetail> userList;


    @Override
    public int getEntryId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public Date getFirstDateTime() {
        return DateUtils.date(firstDateTime);
    }

    public Date getLastDateTime() {
        return DateUtils.date(lastDateTime);
    }

    @Nullable
    public Date getNearestDateTime() {
        return nearestDateTime == null ? null : DateUtils.date(nearestDateTime);
    }

    public String getImageHorizontalUrl() {
        return imageHorizontalUrl;
    }

    public String getImageVerticalUrl() {
        return imageVerticalUrl;
    }

    public int getOrganizationId() {
        return organizationId;
    }

    public ArrayList<Tag> getTagList() {
        return tagList;
    }

    public ArrayList<UserDetail> getUserList() {
        return userList;
    }

    public ArrayList<EventDate> getDateList() {
        return dateList;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public String getDescription() {
        return description;
    }

    public String getImageHorizontalSmallUrl() {
        return imageHorizontalSmallUrl;
    }

    public String getImageHorizontalMediumUrl() {
        return imageHorizontalMediumUrl;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public String getOrganizationShortName() {
        return organizationShortName;
    }

    public String getOrganizationTypeName() {
        return organizationTypeName;
    }

    public String getOrganizationLogoUrl() {
        return organizationLogoUrl;
    }

    public String getOrganizationLogoMediumUrl() {
        return organizationLogoMediumUrl;
    }

    public String getOrganizationLogoSmallUrl() {
        return organizationLogoSmallUrl;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Nullable
    public String getDetailInfoUrl() {
        return detailInfoUrl != null && detailInfoUrl.equals("") ? null : detailInfoUrl;
    }

    public int getLikedUsersCount() {
        return likedUsersCount;
    }

    public void setLikedUsersCount(int likedUsersCount) {
        this.likedUsersCount = likedUsersCount;
    }

    public void favore() {
        isFavorite = !isFavorite;
        likedUsersCount += isFavorite ? 1 : -1;
    }

    public String getLink() {
        return link;
    }

    public boolean isRegistrationRequired() {
        return registrationRequired;
    }

    public boolean isRegistrationApprovementRequired() {
        return registrationApprovementRequired;
    }

    public int getRegistrationLimitCount() {
        return registrationLimitCount;
    }

    public boolean isRegistrationLocally() {
        return registrationLocally;
    }

    public Date getRegistrationTill() {
        return DateUtils.date(registrationTill);
    }

    public boolean isRegistrationApproved() {
        return registrationApproved;
    }

    public boolean isRegistrationAvailable() {
        return registrationAvailable;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public boolean isRegistered() {
        return registered;
    }

    public ArrayList<RegistrationField> getRegistrationFields() {
        return registrationFields;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public int getMyTicketsCount() {
        return myTicketsCount;
    }

    public int getTicketsCount() {
        return ticketsCount;
    }

    public ArrayList<Ticket> getTickets() {
        return tickets;
    }

    public boolean isFree() {
        return isFree;
    }

    @Override
    public int getMinPrice() {
        return minPrice;
    }

    public boolean isSameTime() {
        return isSameTime;
    }
}
