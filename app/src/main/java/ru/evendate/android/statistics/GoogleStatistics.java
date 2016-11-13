package ru.evendate.android.statistics;

/**
 * Created by Aedirn on 15.10.16.
 */

interface GoogleStatistics {

    // View actions
    void sendEventView(int eventId);

    void sendOrganizationView(int orgId);

    void sendUserView(int userId);


    //Notification events
    void sendEventOpenNotification(int eventId);

    void sendOrgOpenNotification(int orgId);

    void sendUserOpenNotification(int userId);

    void sendRecommendOpenNotification();

    void sendEventHideNotification(int eventId);

    void sendOrgHideNotification(int orgId);

    void sendUserHideNotification(int userId);


    // Event actions
    void sendEventFavoreAction(int eventId);

    void sendEventUnfavoreAction(int eventId);

    void sendEventClickLinkAction(int eventId);

    void sendEventOpenMap(int eventId);


    // Org actions
    void sendOrganizationSubAction(int orgId);

    void sendOrganizationUnsubAction(int orgId);

    void sendOrganizationClickLinkAction(int orgId);

    void sendOrgOpenMap(int orgId);


    // User actions
    void sendUserClickLinkAction(int userId);


    // Google screen page tracking
    void sendCurrentScreenName(String screenName);
}
