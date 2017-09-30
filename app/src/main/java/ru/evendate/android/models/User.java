package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by Dmitry on 11.09.2015.
 */
@Parcel
public class User extends DataModel {
    public static final String FIELDS_LIST = "is_friend,name,link";
    public static String SEARCH_ORDER_BY = "-search_score";

    @SerializedName("id")
    int userId;
    @SerializedName("last_name")
    String lastName;
    @SerializedName("first_name")
    String firstName;
    @SerializedName("middle_name")
    String middleName;
    String gender;
    @SerializedName("avatar_url")
    String avatarUrl;
    @SerializedName("is_editor")
    boolean isEditor;

    @SerializedName("is_friend")
    boolean isFriend;
    String type;
    String link;

    @Override
    public int getEntryId() {
        return this.userId;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public boolean isEditor() {
        return isEditor;
    }

    public String getLink() {
        return link;
    }

    public boolean isFriend() {
        return isFriend;
    }
}