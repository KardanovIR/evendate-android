package ru.getlect.evendate.evendate.sync.dataTypes;

import org.chalup.microorm.annotations.Column;

import ru.getlect.evendate.evendate.data.EvendateContract.UserEventEntry;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class EventFriendModel extends FriendModel {
    @Column(UserEventEntry.COLUMN_EVENT_ID)
    int event_id;

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(getClass() == obj.getClass())) return false;

        EventFriendModel tmp = (EventFriendModel) obj;
        return (this.event_id == tmp.event_id &&
                super.equals(obj));
    }
}