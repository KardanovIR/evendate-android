package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Dmitry on 07.09.2016.
 */
public class Settings extends DataModel {

    @SerializedName("show_to_friends")
    boolean showToFriend;

    public boolean isFeedShowedToFriend(){
        return showToFriend;
    };

    @Override
    public int getEntryId() {
        return 0;
    }
}
