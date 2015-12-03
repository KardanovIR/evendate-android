package ru.getlect.evendate.evendate.sync.models;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import org.chalup.microorm.annotations.Column;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class FriendModel extends DataModel {

    @Column(EvendateContract.UserEntry.COLUMN_USER_ID)
    @SerializedName("id")
    int userId;
    @Column(EvendateContract.UserEntry.COLUMN_LAST_NAME)
    @SerializedName("last_name")
    String lastName;
    @Column(EvendateContract.UserEntry.COLUMN_FIRST_NAME)
    @SerializedName("first_name")
    String firstName;
    @Column(EvendateContract.UserEntry.COLUMN_MIDDLE_NAME)
    @SerializedName("middle_name")
    String middleName;
    @Column(EvendateContract.UserEntry.COLUMN_AVATAR_URL)
    @SerializedName("avatar_url")
    String avatarUrl;
    @Column(EvendateContract.UserEntry.COLUMN_TYPE)
    String type;
    @Column(EvendateContract.UserEntry.COLUMN_FRIEND_UID)
    @SerializedName("friend_uid")
    String friendUid;
    @Column(EvendateContract.UserEntry.COLUMN_LINK)
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

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(getClass() == obj.getClass())) return false;
        
        FriendModel tmp = (FriendModel) obj;
        return (this.lastName.equals(tmp.lastName) &&
                this.firstName.equals(tmp.firstName) &&
                (this.middleName != null ? this.middleName.equals(tmp.middleName) : tmp.middleName == null) &&
                this.avatarUrl.equals(tmp.avatarUrl) &&
                this.type.equals(tmp.type) &&
                this.friendUid.equals(tmp.friendUid) &&
                this.link.equals(tmp.link)
        );
    }

    @Override
    public ContentProviderOperation getUpdate(final Uri ContentUri) {
        return fillWithData(ContentProviderOperation.newUpdate(ContentUri))
                .build();
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return fillWithData(ContentProviderOperation.newInsert(ContentUri))
                .withValue(EvendateContract.UserEntry.COLUMN_USER_ID, this.userId)
                .build();
    }
    
    protected ContentProviderOperation.Builder fillWithData(ContentProviderOperation.Builder operation){
        return operation
                .withValue(EvendateContract.UserEntry.COLUMN_LAST_NAME, this.lastName)
                .withValue(EvendateContract.UserEntry.COLUMN_FIRST_NAME, this.firstName)
                .withValue(EvendateContract.UserEntry.COLUMN_MIDDLE_NAME, this.middleName)
                .withValue(EvendateContract.UserEntry.COLUMN_AVATAR_URL, this.avatarUrl)
                .withValue(EvendateContract.UserEntry.COLUMN_TYPE, this.type)
                .withValue(EvendateContract.UserEntry.COLUMN_FRIEND_UID, this.friendUid)
                .withValue(EvendateContract.UserEntry.COLUMN_LINK, this.link);
    }
}