package ru.getlect.evendate.evendate.sync.dataTypes;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class FriendEntry extends DataEntry{
    public final int user_id;
    public final String last_name;
    public final String first_name;
    public final String middle_name;
    public final String avatar_url;
    public final String type;
    //TODO
    @SerializedName("none")
    public final long friend_uid;
    public final String link;

    public FriendEntry(int user_id, String last_name, String first_name, String middle_name,
                               String avatar_url, String type, int friend_uid, String link) {
        this.user_id = user_id;
        this.last_name = last_name;
        this.first_name = first_name;
        this.middle_name = middle_name;
        this.avatar_url = avatar_url;
        this.type = type;
        this.friend_uid = friend_uid;
        this.link = link;
    }

    @Override
    public int getEntryId() {
        return this.user_id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

    /* obj ссылается на null */
        if (obj == null) return false;

    /* Удостоверимся, что ссылки имеют тот же самый тип */
        if (!(getClass() == obj.getClass())) return false;
        FriendEntry tmp = (FriendEntry) obj;
        return (this.user_id == tmp.user_id);
    }

    @Override
    public ContentProviderOperation getUpdate(final Uri ContentUri) {
        return ContentProviderOperation.newUpdate(ContentUri)
                .withValue(EvendateContract.UserEntry.COLUMN_LAST_NAME, this.last_name)
                .withValue(EvendateContract.UserEntry.COLUMN_FIRST_NAME, this.first_name)
                .withValue(EvendateContract.UserEntry.COLUMN_MIDDLE_NAME, this.middle_name)
                .withValue(EvendateContract.UserEntry.COLUMN_AVATAR_URL, this.avatar_url)
                .withValue(EvendateContract.UserEntry.COLUMN_TYPE, this.type)
                .withValue(EvendateContract.UserEntry.COLUMN_FRIEND_UID, this.friend_uid)
                .withValue(EvendateContract.UserEntry.COLUMN_LINK, this.link)
                .build();
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return ContentProviderOperation.newInsert(ContentUri)
                .withValue(EvendateContract.UserEntry.COLUMN_USER_ID, this.user_id)
                .withValue(EvendateContract.UserEntry.COLUMN_LAST_NAME, this.last_name)
                .withValue(EvendateContract.UserEntry.COLUMN_FIRST_NAME, this.first_name)
                .withValue(EvendateContract.UserEntry.COLUMN_MIDDLE_NAME, this.middle_name)
                .withValue(EvendateContract.UserEntry.COLUMN_AVATAR_URL, this.avatar_url)
                .withValue(EvendateContract.UserEntry.COLUMN_TYPE, this.type)
                .withValue(EvendateContract.UserEntry.COLUMN_FRIEND_UID, this.friend_uid)
                .withValue(EvendateContract.UserEntry.COLUMN_LINK, this.link)
                .build();
    }
}