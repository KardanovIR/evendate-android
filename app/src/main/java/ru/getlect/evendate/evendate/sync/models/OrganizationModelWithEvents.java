package ru.getlect.evendate.evendate.sync.models;

import java.util.ArrayList;

/**
 * Created by Dmitry on 15.11.2015.
 */
public class OrganizationModelWithEvents extends OrganizationModel {
    private ArrayList<EventModel> events;

    public ArrayList<EventModel> getEvents() {
        return events;
    }

}
