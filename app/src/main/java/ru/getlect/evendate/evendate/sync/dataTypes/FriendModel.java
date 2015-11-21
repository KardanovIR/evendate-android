package ru.getlect.evendate.evendate.sync.dataTypes;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class FriendModel extends DataModel {

    @SerializedName("id")
    int userId;
    @SerializedName("last_name")
    String lastName;
    @SerializedName("first_name")
    String firstName;
    @SerializedName("middle_name")
    String middleName;
    @SerializedName("avatar_url")
    String avatarUrl;
    String type;
    @SerializedName("friend_uid")
    String friendUid;
    String link;

    @Override
    public int getEntryId() {
        return this.userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(getClass() == obj.getClass())) return false;
        
        FriendModel tmp = (FriendModel) obj;
        return (this.lastName.equals(tmp.lastName) &&
            this.firstName.equals(tmp.firstName) &&
            this.middleName.equals(tmp.middleName) &&
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