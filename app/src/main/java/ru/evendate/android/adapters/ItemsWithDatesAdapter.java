package ru.evendate.android.adapters;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import ru.evendate.android.models.EventFeed;

/**
 * Created by ds_gordeev on 15.04.2016.
 */
public class ItemsWithDatesAdapter {
    private ArrayList<EventFeed> events;
    private ArrayList<Object> list;

    public ItemsWithDatesAdapter(ArrayList<EventFeed> events) {
        this.events = events;
        convert();
    }

    private void convert() {
        HashSet<Long> set = new HashSet<>();
        for (EventFeed event : events) {
            if (!set.contains(event.getNearestDate())) {
                set.add(event.getNearestDate());
                list.add(new Date(event.getNearestDate() * 1000));
            }
            list.add(event);
        }
    }

    public ArrayList<EventFeed> getEvents() {
        return events;
    }

    public void addEvents(ArrayList<EventFeed> events) {
        this.events.addAll(events);
        convert();
    }

    public ArrayList<Object> getList() {
        return list;
    }
}
