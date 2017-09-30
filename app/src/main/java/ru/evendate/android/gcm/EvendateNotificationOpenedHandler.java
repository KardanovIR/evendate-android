package ru.evendate.android.gcm;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.statistics.Statistics;
import ru.evendate.android.ui.eventdetail.EventDetailActivity;
import ru.evendate.android.ui.feed.MainActivity;
import ru.evendate.android.ui.orgdetail.OrganizationDetailActivity;
import ru.evendate.android.ui.userdetail.UserProfileActivity;

import static ru.evendate.android.gcm.NotificationAdditionalData.EVENT_TYPE;
import static ru.evendate.android.gcm.NotificationAdditionalData.ORGANIZATION_TYPE;
import static ru.evendate.android.gcm.NotificationAdditionalData.RECOMMENDATION_TYPE;
import static ru.evendate.android.gcm.NotificationAdditionalData.USER_TYPE;

/**
 * Created by Aedirn on 04.10.16.
 */
public class EvendateNotificationOpenedHandler implements OneSignal.NotificationOpenedHandler {
    private final String LOG_TAG = EvendateNotificationOpenedHandler.class.getSimpleName();
    private Context mContext;

    final static String FAVE_EVENT_ACTION_ID = "fave_event_action";

    public EvendateNotificationOpenedHandler(Context context) {
        mContext = context;
    }

    @Override
    public void notificationOpened(OSNotificationOpenResult result) {
        JSONObject data = result.notification.payload.additionalData;

        if (data == null)
            //todo send error to stat
            return;
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        NotificationAdditionalData addData = gson.fromJson(data.toString(), NotificationAdditionalData.class);

        Intent intent;
        Statistics statistics = new Statistics(mContext);
        switch (addData.type) {
            case EVENT_TYPE:
                int eventId = addData.eventId;
                intent = new Intent(mContext, EventDetailActivity.class);
                intent.setData(EvendateContract.EventEntry.getContentUri(eventId));
                statistics.sendEventOpenNotification(eventId);
                break;
            case ORGANIZATION_TYPE:
                int orgId = addData.orgId;
                intent = new Intent(mContext, OrganizationDetailActivity.class);
                intent.setData(EvendateContract.OrganizationEntry.getContentUri(orgId));
                statistics.sendOrgOpenNotification(orgId);
                break;
            case USER_TYPE:
                int userId = addData.userId;
                intent = new Intent(mContext, UserProfileActivity.class);
                intent.setData(EvendateContract.UserEntry.getContentUri(userId));
                statistics.sendUserOpenNotification(userId);
                break;
            case RECOMMENDATION_TYPE:
                intent = new Intent(mContext, MainActivity.class);
                intent.putExtra(MainActivity.SHOW_ONBOARDING, true);
                statistics.sendRecommendOpenNotification();
                //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                break;
            default:
                return;
        }

        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }
}
