package ru.evendate.android.ui.networking;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import ru.evendate.android.models.DataModel;

/**
 * Created by dmitry on 03.12.2017.
 */
@Parcel
public class NetworkingRequest extends DataModel {

    @SerializedName("event_id")
    int eventId;
    @SerializedName("sender_user_id")
    int senderUserId;
    @SerializedName("recipient_user_id")
    int recipientUserId;
    @SerializedName("message")
    String message;
    boolean status;
    @Nullable
    Boolean accept_status;
    String accepted_at;
    String created_at;
    String updated_at;


    @Override
    public int getEntryId() {
        return 0;
    }

    public int getEventId() {
        return eventId;
    }

    public int getSenderUserId() {
        return senderUserId;
    }

    public int getRecipientUserId() {
        return recipientUserId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isStatus() {
        return status;
    }

    public boolean isAccept_status() {
        return accept_status;
    }

    public String getAccepted_at() {
        return accepted_at;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

}