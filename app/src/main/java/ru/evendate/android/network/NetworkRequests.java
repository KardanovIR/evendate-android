package ru.evendate.android.network;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationDetail;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dmitry on 25.09.2016.
 */

public class NetworkRequests {
    private static final String LOG_TAG = NetworkRequests.class.getSimpleName();

    private Context mContext;

    public NetworkRequests(Context context) {
        mContext = context;
    }

    /**
     * handle organization subscription
     * start subscribe/unsubscribe loader to carry it to server
     * push subscribe/unsubscribe stat to analytics
     */
    public void subscribeOrg(OrganizationDetail organization,
                                    @Nullable CoordinatorLayout coordinatorLayout) {

        ApiService apiService = ApiFactory.getService(mContext);
        Observable<Response> subObservable;
        String token =
                EvendateAccountManager.peekToken(mContext);
        if (organization.isSubscribed()) {
            subObservable = apiService.orgDeleteSubscription(organization.getEntryId(), token);
        } else {
            subObservable = apiService.orgPostSubscription(organization.getEntryId(), token);
        }
        subObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::checkSubscriptionResult,
                        this::onSubscriptionError
                );

        organization.subscribe();

        if (coordinatorLayout != null) {
            if (organization.isSubscribed()) {
                Snackbar.make(coordinatorLayout, R.string.organization_subscription_confirm, Snackbar.LENGTH_LONG).show();
            } else {
                Snackbar.make(coordinatorLayout, R.string.organization_subscription_remove_confirm, Snackbar.LENGTH_LONG).show();
            }
        }
        Tracker tracker = EvendateApplication.getTracker();
        tracker.send(constructStatEvent(organization).build());
    }

    private void checkSubscriptionResult(Response response) {
        if (response.isOk())
            Log.i(LOG_TAG, "subscription applied");
        else {
            Log.e(LOG_TAG, "Error with response with organization sub");
            onSubscriptionError(null);
        }
    }

    private void onSubscriptionError(@Nullable Throwable error) {
        if(error != null)
            Log.e(LOG_TAG, error.getMessage());
        if (mContext instanceof Activity)
            Toast.makeText(mContext, R.string.download_error, Toast.LENGTH_SHORT).show();
    }

    private HitBuilders.EventBuilder constructStatEvent(OrganizationDetail organization) {

        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(mContext.getString(R.string.stat_category_organization))
                .setLabel((Long.toString(organization.getEntryId())));
        if (organization.isSubscribed()) {
            event.setAction(mContext.getString(R.string.stat_action_subscribe));
        } else {
            event.setAction(mContext.getString(R.string.stat_action_unsubscribe));
        }
        return event;
    }

    public static String formatDateForServer(Date d){
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return dateFormat.format(d.getTime());
    }
}
