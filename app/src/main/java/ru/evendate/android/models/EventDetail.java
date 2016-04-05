package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 07.02.2016.
 */
public class EventDetail extends Event implements EventFeed {
    public static final String FIELDS_LIST = "location,latitude,longitude,organization_name," +
            "organization_type_name,organization_short_name," +
            "organization_logo_large_url,organization_logo_medium_url,organization_logo_small_url," +
            "favored_users_count,description,detail_info_url,is_favorite,link," +
            "registration_required,registration_till,is_free,min_price," +
            "dates{fields:'" + DateFull.FIELDS_LIST + "'},tags,favored{fields:\'" + User.FIELDS_LIST + "\'}";

    String location;
    double latitude;
    double longitude;
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
    String detailInfoUrl;
    @SerializedName("is_favorite")
    boolean isFavorite;
    @SerializedName("link")
    String link;
    @SerializedName("registration_required")
    boolean registrationRequired;
    @SerializedName("registration_till")
    String registrationTill;
    @SerializedName("is_free")
    boolean isFree;
    @SerializedName("min_price")
    int minPrice;

    @SerializedName("tags")
    ArrayList<Tag> tagList;
    @SerializedName("dates")
    ArrayList<DateFull> dateList;
    @SerializedName("favored")
    ArrayList<UserDetail> userList;


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

    public String getRegistrationTill() {
        return registrationTill;
    }

    public boolean isFree() {
        return isFree;
    }

    @Override
    public int getMinPrice() {
        return minPrice;
    }
}
