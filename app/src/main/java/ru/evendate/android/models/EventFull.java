package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 07.02.2016.
 */
@SuppressWarnings("unused")
public class EventFull extends Event implements EventFeed {
    public static final String FIELDS_LIST = "location,latitude,longitude,organization_name," +
            "organization_type_name,organization_short_name," +
            "organization_logo_large_url,organization_logo_medium_url,organization_logo_small_url," +
            "favored_users_count,description,detail_info_url,is_favorite,link," +

            "registration_required,registration_approvement_required,registration_limit_count," +
            "registration_locally,registration_till,registration_approved,registration_available," +
            "registered_count,registered" +

            "is_free,min_price,is_same_time,created_at," +

            "dates{fields:'" + DateFull.FIELDS_LIST + "'},tags,favored{fields:\'" + User.FIELDS_LIST + "\'}";

    private String location;
    private double latitude;
    private double longitude;
    @SerializedName("organization_name")
    private String organizationName;
    @SerializedName("organization_type_name")
    private String organizationTypeName;
    @SerializedName("organization_short_name")
    private String organizationShortName;
    @SerializedName("organization_logo_large_url")
    private String organizationLogoUrl;
    @SerializedName("organization_logo_medium_url")
    private String organizationLogoMediumUrl;
    @SerializedName("organization_logo_small_url")
    private String organizationLogoSmallUrl;
    @SerializedName("favored_users_count")
    private int likedUsersCount;
    private String description;
    @SerializedName("detail_info_url")
    private String detailInfoUrl;
    @SerializedName("is_favorite")
    private boolean isFavorite;
    @SerializedName("link")
    private String link;

    @SerializedName("registration_required")
    private boolean registrationRequired;
    @SerializedName("registration_approvement_required")
    private boolean registrationApprovementRequired;
    @SerializedName("registration_limit_count")
    private int registrationLimitCount;
    @SerializedName("registration_locally")
    private boolean registrationLocally;
    @SerializedName("registration_till")
    private long registrationTill;
    @SerializedName("registration_approved")
    private boolean registrationApproved;
    @SerializedName("registration_available")
    private boolean registrationAvailable;
    @SerializedName("registered_count")
    private int registeredCount;
    @SerializedName("registered")
    private boolean registered;

    @SerializedName("is_free")
    private boolean isFree;
    @SerializedName("min_price")
    private int minPrice;
    @SerializedName("is_same_time")
    private boolean isSameTime;

    @SerializedName("tags")
    private ArrayList<Tag> tagList;
    @SerializedName("dates")
    private ArrayList<DateFull> dateList;
    @SerializedName("favored")
    private ArrayList<UserDetail> userList;


    public ArrayList<Tag> getTagList() {
        return tagList;
    }

    public ArrayList<UserDetail> getUserList() {
        return userList;
    }

    public ArrayList<DateFull> getDateList() {
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

    public String getDetailInfoUrl() {
        return detailInfoUrl;
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

    public long getRegistrationTill() {
        return registrationTill;
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
