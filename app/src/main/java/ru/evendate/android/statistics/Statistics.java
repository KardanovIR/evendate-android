package ru.evendate.android.statistics;

import android.content.Context;

/**
 * Created by Aedirn on 15.10.16.
 */

public class Statistics implements GoogleStatistics, EvendateStatistics {

    private GoogleStatistics googleStatistics;
    private EvendateStatistics evendateStatistics;


    public Statistics(Context context) {
        googleStatistics = new GoogleStatisticsImpl(context);
        evendateStatistics = new EvendateStatisticsImpl(context);
    }

    /*
        View actions
    */
    @Override
    public void sendEventView(int eventId) {
        googleStatistics.sendEventView(eventId);
    }

    @Override
    public void sendOrganizationView(int orgId) {
        googleStatistics.sendOrganizationView(orgId);
    }

    @Override
    public void sendUserView(int userId) {
        googleStatistics.sendUserView(userId);
    }

    @Override
    public void sendEventViewFromFeed(int eventId) {
        evendateStatistics.sendEventViewFromFeed(eventId);
    }

    /*
        Notification events
     */
    @Override
    public void sendEventOpenNotification(int eventId) {
        googleStatistics.sendEventOpenNotification(eventId);
        evendateStatistics.sendEventOpenNotification(eventId);
    }

    @Override
    public void sendOrgOpenNotification(int orgId) {
        googleStatistics.sendOrgOpenNotification(orgId);
    }

    @Override
    public void sendUserOpenNotification(int userId) {
        googleStatistics.sendUserOpenNotification(userId);
    }

    @Override
    public void sendRecommendOpenNotification() {
        googleStatistics.sendRecommendOpenNotification();
    }

    @Override
    public void sendEventHideNotification(int eventId) {
        googleStatistics.sendEventHideNotification(eventId);
        evendateStatistics.sendEventHideNotification(eventId);
    }

    @Override
    public void sendOrgHideNotification(int orgId) {
        googleStatistics.sendOrgHideNotification(orgId);
    }

    @Override
    public void sendUserHideNotification(int userId) {
        googleStatistics.sendUserHideNotification(userId);
    }


    /*
        Event actions
     */
    @Override
    public void sendEventFavoreAction(int eventId) {
        googleStatistics.sendEventFavoreAction(eventId);
    }

    @Override
    public void sendEventUnfavoreAction(int eventId) {
        googleStatistics.sendEventUnfavoreAction(eventId);
    }

    @Override
    public void sendEventClickLinkAction(int eventId) {
        googleStatistics.sendEventClickLinkAction(eventId);
        evendateStatistics.sendEventClickLinkAction(eventId);
    }

    @Override
    public void sendEventOpenMap(int eventId) {
        googleStatistics.sendEventOpenMap(eventId);
        evendateStatistics.sendEventOpenMap(eventId);
    }

    /*
        Org actions
     */
    @Override
    public void sendOrganizationSubAction(int orgId) {
        googleStatistics.sendOrganizationSubAction(orgId);
    }

    @Override
    public void sendOrganizationUnsubAction(int orgId) {
        googleStatistics.sendOrganizationUnsubAction(orgId);
    }

    @Override
    public void sendOrganizationClickLinkAction(int orgId) {
        googleStatistics.sendOrganizationClickLinkAction(orgId);
        evendateStatistics.sendOrganizationClickLinkAction(orgId);
    }

    @Override
    public void sendOrgOpenMap(int orgId) {
        googleStatistics.sendOrgOpenMap(orgId);
        evendateStatistics.sendOrgOpenMap(orgId);
    }

    /*
        User actions
     */
    @Override
    public void sendUserClickLinkAction(int userId) {
        googleStatistics.sendUserClickLinkAction(userId);
    }

    @Override
    public void sendCurrentScreenName(String screenName) {
        googleStatistics.sendCurrentScreenName(screenName);
    }
}
