package ru.getlect.evendate.evendate.sync.dataTypes;

/**
 * Created by Dmitry on 13.09.2015.
 */
public class EventTagEntry extends TagEntry {
    public final int event_id;

    public EventTagEntry(int event_id, int tag_id, String name) {
        super(tag_id, name);
        this.event_id = event_id;
    }

    @Override
    public int getEntryId() {
        return tag_id;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;

    /* obj ссылается на null */
        if (obj == null) return false;

    /* Удостоверимся, что ссылки имеют тот же самый тип */
        if (!(getClass() == obj.getClass())) return false;
        EventTagEntry tmp = (EventTagEntry) obj;
        return (this.event_id == tmp.event_id &&
                super.equals(obj));
    }
}