package ru.evendate.android.statistics;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;

/**
 * Created by Aedirn on 21.06.16.
 */
class GoogleStatisticsImpl implements GoogleStatistics {
    private static final String LOG_TAG = "GoogleStatisticsImpl";
    private Context mContext;

    GoogleStatisticsImpl(Context context) {
        mContext = context;
    }


    @Override
    public void sendEventView(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_view));
    }

    @Override
    public void sendOrganizationView(int orgId) {
        sendActionToStat(orgId,
                mContext.getString(R.string.stat_category_organization),
                mContext.getString(R.string.stat_action_view));
    }

    @Override
    public void sendUserView(int userId) {
        sendActionToStat(userId,
                mContext.getString(R.string.stat_category_user),
                mContext.getString(R.string.stat_action_view));
    }


    @Override
    public void sendEventOpenNotification(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_notification));
    }

    @Override
    public void sendOrgOpenNotification(int orgId) {
        sendActionToStat(orgId,
                mContext.getString(R.string.stat_category_organization),
                mContext.getString(R.string.stat_action_notification));
    }

    @Override
    public void sendUserOpenNotification(int userId) {
        sendActionToStat(userId,
                mContext.getString(R.string.stat_category_user),
                mContext.getString(R.string.stat_action_notification));
    }

    @Override
    public void sendRecommendOpenNotification() {
        //todo implement
        //sendActionToStat(id,
        //        mContext.getString(R.string.stat_category_organization),
        //        mContext.getString(R.string.stat_action_notification));
    }

    @Override
    public void sendEventHideNotification(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_hide_notification));
    }

    @Override
    public void sendOrgHideNotification(int orgId) {
        sendActionToStat(orgId,
                mContext.getString(R.string.stat_category_organization),
                mContext.getString(R.string.stat_action_hide_notification));
    }

    @Override
    public void sendUserHideNotification(int userId) {
        sendActionToStat(userId,
                mContext.getString(R.string.stat_category_user),
                mContext.getString(R.string.stat_action_hide_notification));
    }

    @Override
    public void sendEventFavoreAction(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_like));
    }

    @Override
    public void sendEventUnfavoreAction(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_dislike));
    }

    @Override
    public void sendEventClickLinkAction(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_click_on_link));
    }

    @Override
    public void sendEventOpenMap(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_open_map));
    }

    @Override
    public void sendRegistrationStarted(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_start_registration));
    }

    @Override
    public void sendRegistrationCompleted(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_complete_registration));
    }

    @Override
    public void sendRegistrationCanceled(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_cancel_registration));
    }

    @Override
    public void sendTicketingStarted(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_start_ticketing));
    }

    @Override
    public void sendTicketingCompleted(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_complete_ticketing));
    }

    @Override
    public void sendTicketingCanceled(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_cancel_ticketing));
    }

    @Override
    public void sendTicketingAborted(int eventId) {
        sendActionToStat(eventId,
                mContext.getString(R.string.stat_category_event),
                mContext.getString(R.string.stat_action_abort_ticketing));
    }

    @Override
    public void sendOrganizationSubAction(int orgId) {
        sendActionToStat(orgId,
                mContext.getString(R.string.stat_category_organization),
                mContext.getString(R.string.stat_action_subscribe));
    }

    @Override
    public void sendOrganizationUnsubAction(int orgId) {
        sendActionToStat(orgId,
                mContext.getString(R.string.stat_category_organization),
                mContext.getString(R.string.stat_action_unsubscribe));
    }

    @Override
    public void sendOrgOpenMap(int orgId) {
        sendActionToStat(orgId,
                mContext.getString(R.string.stat_category_organization),
                mContext.getString(R.string.stat_action_open_map));
    }

    @Override
    public void sendOrganizationClickLinkAction(int orgId) {
        sendActionToStat(orgId,
                mContext.getString(R.string.stat_category_organization),
                mContext.getString(R.string.stat_action_click_on_link));
    }

    @Override
    public void sendUserClickLinkAction(int userId) {
        sendActionToStat(userId,
                mContext.getString(R.string.stat_category_user),
                mContext.getString(R.string.stat_action_click_on_link));

    }

    private void sendActionToStat(int id, String category, String action) {
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(Integer.toString(id));
        EvendateApplication.getTracker().send(event.build());
        Log.i(LOG_TAG, "Sent action to GA: category=" + category + ", action=" + action + ", label=" + Integer.toString(id));
    }

    @Override
    public void sendCurrentScreenName(String screenName) {
        Tracker tracker = EvendateApplication.getTracker();
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }
}
