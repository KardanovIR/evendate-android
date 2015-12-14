package ru.evendate.android.sync.models;

import com.google.gson.annotations.SerializedName;

import org.chalup.microorm.annotations.Column;

import ru.evendate.android.data.EvendateContract.EventTagEntry;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class EventTagModel extends TagModel {
    @Column(EventTagEntry.COLUMN_EVENT_ID)
    @SerializedName("event_id")
    int eventId;

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(getClass() == obj.getClass())) return false;

        EventTagModel tmp = (EventTagModel) obj;
        return (this.eventId == tmp.eventId &&
                super.equals(obj));
    }
}