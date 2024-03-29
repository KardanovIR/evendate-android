package ru.evendate.android.network;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendatePreferences;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.StatisticsEvent;
import ru.evendate.android.statistics.Statistics;

/**
 * Created by Aedirn on 16.10.16.
 */

public class ServiceImpl {
    private static final String LOG_TAG = ServiceImpl.class.getSimpleName();

    /**
     * handle subscription button
     * start subscribeOrgAndChangeState/unsubscribe loader to carry it to server
     * push subscribeOrgAndChangeState/unsubscribe stat to analytics
     */
    public static void subscribeOrgAndChangeState(Context context, OrganizationDetail organization) {
        Observable<Response> subOrganizationObservable;
        int organizationId = organization.getEntryId();
        String token = EvendateAccountManager.peekToken(context);

        if (organization.isSubscribed()) {
            subOrganizationObservable = new DataRepository(context).unSubscribeOrg(token, organizationId);
        } else {
            subOrganizationObservable = new DataRepository(context).subscribeOrg(token, organizationId);
        }
        subOrganizationObservable
                .subscribe(result -> {
                    if (result.isOk()) {
                        Log.i(LOG_TAG, "subscription applied");
                        Statistics googleStatistics = new Statistics(context);
                        if (organization.isSubscribed()) {
                            googleStatistics.sendOrganizationSubAction(organizationId);
                        } else {
                            googleStatistics.sendOrganizationUnsubAction(organizationId);
                        }
                    } else
                        Log.e(LOG_TAG, "Error with response with organization sub");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage()));

        organization.changeSubscriptionState();
    }

    /**
     * register user id at evendate service for cloud messaging
     */
    public static void sendRegistrationToServer(Context context, String userId) {
        ApiService apiService = ApiFactory.getService(context);
        String deviceModel = Build.MANUFACTURER + " " + Build.MODEL;
        Observable<Response> deviceObservable =
                apiService.putDeviceToken(EvendateAccountManager.peekToken(context),
                        userId, "android", deviceModel, Build.VERSION.RELEASE);

        deviceObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isOk()) {
                        Log.i(LOG_TAG, "registered device userId");
                        Log.i(LOG_TAG, "pushToken " + userId);
                        EvendatePreferences.setDeviceTokenSynced(context, true);
                    } else {
                        Log.e(LOG_TAG, "not registered device userId");
                        EvendatePreferences.setDeviceTokenSynced(context, false);
                    }
                }, error -> {
                    Log.e(LOG_TAG, "not registered device userId");
                    Log.e(LOG_TAG, "" + error.getMessage());
                    EvendatePreferences.setDeviceTokenSynced(context, false);
                });
    }

    public static void postEvent(Context context, List<StatisticsEvent> events) {
        ApiService apiService = ApiFactory.getService(context);
        Observable<Response> eventObservable =
                apiService.postStat(EvendateAccountManager.peekToken(context), events);
        eventObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result.isOk())
                        Log.i(LOG_TAG, "posted stat events");
                    else
                        Log.e(LOG_TAG, "error posting stat events");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage()));
    }
}
