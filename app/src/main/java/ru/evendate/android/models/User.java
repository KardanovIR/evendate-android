package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class User extends DataModel {
    public static final String FIELDS_LIST = "is_friend,name,link";

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

    boolean is_friend;
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

    public String getLink() {
        return link;
    }

    public boolean is_friend() {
        return is_friend;
    }
}