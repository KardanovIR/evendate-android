package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 07.02.2016.
 */
public class EventDetail extends EventModel {
    public static final String FIELDS_LIST = "location,latitude,longitude,organization_name," +
            "organization_type_name,organization_type_name,organization_short_name,organization_logo_large_url," +
            "favored_users_count,description,detail_info_url,is_favorite," +
            "dates,tags,favored{fields:\'" + UserModel.FIELDS_LIST + "\'}";

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
    @SerializedName("favored_users_count")
    int likedUsersCount;
    String description;
    @SerializedName("detail_info_url")
    String detailInfoUrl;
    @SerializedName("is_favorite")
    boolean isFavorite;

    @SerializedName("tags")
    ArrayList<TagModel> tagList;
    @SerializedName("dates")
    ArrayList<Date> dateList;
    @SerializedName("favored")
    ArrayList<UserDetail> userList;


    public ArrayList<TagModel> getTagList() {
        return tagList;
    }

    public ArrayList<UserDetail> getUserList() {
        return userList;
    }

    public ArrayList<Date> getDataList() {
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

    public void favore(){
        isFavorite = !isFavorite;
        likedUsersCount += isFavorite ? 1 : -1;
    }
}
