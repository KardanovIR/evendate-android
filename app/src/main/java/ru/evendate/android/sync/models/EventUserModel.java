package ru.evendate.android.sync.models;

import org.chalup.microorm.annotations.Column;

import ru.evendate.android.data.EvendateContract.UserEventEntry;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class EventUserModel extends UserModel {
    @Column(UserEventEntry.COLUMN_EVENT_ID)
    int event_id;

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(getClass() == obj.getClass())) return false;

        EventUserModel tmp = (EventUserModel) obj;
        return (this.event_id == tmp.event_id &&
                super.equals(obj));
    }
}