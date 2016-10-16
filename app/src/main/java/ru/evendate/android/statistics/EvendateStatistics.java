package ru.evendate.android.statistics;

/**
 * Created by Aedirn on 15.10.16.
 */

public interface EvendateStatistics {

    // View actions
    void sendEventViewFromFeed(int eventId);


    //Notification events
    void sendEventOpenNotification(int eventId);

    //void sendOrgOpenNotification(int orgId);

    //void sendUserOpenNotification(int userId);

    //void sendRecommendOpenNotification(int id);

    void sendEventHideNotification(int eventId);


    // Open map events
    void sendEventOpenMap(int eventId);

    void sendOrgOpenMap(int orgId);


    // Open event site events
    void sendEventClickLinkAction(int eventId);

    void sendOrganizationClickLinkAction(int organizationId);
}
