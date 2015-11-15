package ru.getlect.evendate.evendate.sync.dataTypes;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 15.11.2015.
 */
public class OrganizationEntryWithEvents extends OrganizationEntry{
    private ArrayList<EventEntry> events;
    @SerializedName("subscribed_friends")
    private ArrayList<FriendEntry> subscribedFriends;

    public OrganizationEntryWithEvents(int organizationId, String name, String img_url,
                                       String short_name, String description, String typeName,
                                       int subscribedCount, Integer subscriptionId, boolean isSubscribed,
                                       String backgroundLargeUrl, int updated_at) {
        super(organizationId, name, img_url, short_name, description, typeName, subscribedCount, subscriptionId, isSubscribed, backgroundLargeUrl, updated_at);
    }

    public ArrayList<EventEntry> getEvents() {
        return events;
    }

    public ArrayList<FriendEntry> getSubscribedFriends() {
        return subscribedFriends;
    }
}
