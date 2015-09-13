package ru.getlect.evendate.evendate.sync.dataTypes;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class EventFriendEntry extends FriendEntry {
    public final int event_id;

    public EventFriendEntry(int event_id, int user_id, String last_name, String first_name, String middle_name,
                            String avatar_url, String type, int friend_uid, String link) {
        super(user_id, last_name, first_name, middle_name,
                avatar_url, type, friend_uid, link);
        this.event_id = event_id;
    }

    @Override
    public int getEntryId() {
        return user_id;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;

    /* obj ссылается на null */
        if (obj == null) return false;

    /* Удостоверимся, что ссылки имеют тот же самый тип */
        if (!(getClass() == obj.getClass())) return false;
        EventFriendEntry tmp = (EventFriendEntry) obj;
        return (this.event_id == tmp.event_id &&
                super.equals(obj));
    }
}