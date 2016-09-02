package ru.evendate.android.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.EventNotification;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class NotificationListAdapter extends ArrayAdapter<NotificationListAdapter.Notification> {
    private static String LOG_TAG = NotificationListAdapter.class.getSimpleName();

    private final Context context;
    private int eventId;
    private List<Notification> notifications;

    public NotificationListAdapter(Context context, List<Notification> eventNotifications, int eventId) {
        super(context, R.layout.item_multichoice, eventNotifications);
        this.context = context;
        this.eventId = eventId;
        notifications = eventNotifications;
    }

    static class ViewHolder {
        CheckBox checkBox;
        TextView textView;
    }

    public enum NotificationType {
        ON_CREATION("notification-now"),
        CUSTOM("notification-custom"),
        ON_DATE_CHANGED("notification-event-changed-dates"),
        BEFORE_THREE_HOURS("notification-before-three-hours"),
        BEFORE_THREE_DAYS("notification-before-three-days"),
        BEFORE_DAY("notification-before-day"),
        BEFORE_WEEK("notification-before-week"),
        BEFORE_QUARTER_OF_HOUR("notification-before-quarter-of-hour"),
        EVENT_CHANGED_PRICE("notification-event-changed-price"),

        NOTIFICATION_UNDEFINED("undefined");

        final String type;


        NotificationType(String type) {
            this.type = type;
        }

        static public NotificationType getType(String pType) {
            for (NotificationType type: NotificationType.values()) {
                if (type.type().equals(pType)) {
                    return type;
                }
            }
            return NOTIFICATION_UNDEFINED;
        }
        public String type() {
            return type;
        }
    }

    public static class Notification {
        public String type;
        public boolean checked = false;
        public Boolean newChecked = null;
        public EventNotification notification;
        public boolean changed = false;

        public Notification(String notificationType) {
            type = notificationType;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_multichoice, parent, false);
            holder = new ViewHolder();
            holder.checkBox = (CheckBox) rowView.findViewById(R.id.checkBox);
            holder.textView = (TextView) rowView.findViewById(R.id.tv_checkboxtext);
            rowView.setTag(holder);
        } else {
            holder = (ViewHolder) rowView.getTag();
            holder.checkBox.setOnCheckedChangeListener(null);
        }
        Notification notification = getItem(position);
        holder.textView.setText(getTypeString(notification));
        holder.checkBox.setChecked(notification.checked);
        holder.checkBox.setOnCheckedChangeListener((CompoundButton compoundButton, boolean b) -> {
            notification.newChecked = b;
            notification.changed = true;
        });

        return rowView;
    }

    String getTypeString(Notification notification){
        switch (NotificationType.getType(notification.type)){
            case BEFORE_THREE_HOURS:
                return context.getString(R.string.notification_before_three_hours);
            case BEFORE_DAY:
                return context.getString(R.string.notification_before_day);
            case BEFORE_THREE_DAYS:
                return context.getString(R.string.notification_before_three_days);
            case BEFORE_WEEK:
                return context.getString(R.string.notification_before_week);
            case BEFORE_QUARTER_OF_HOUR:
                return context.getString(R.string.notification_before_quarter_of_hour);
            case CUSTOM:
                return formatNotificationTime(notification.notification.getNotificationTimeInMillis());
        }
        return null;
    }
    String formatNotificationTime(long timeInMillis){
        DateFormat df = new SimpleDateFormat("d MMMM HH:mm", Locale.getDefault());
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        return df.format(new Date(timeInMillis));
    }

    public void update(){
        for (Notification notification : notifications) {
            if(!notification.changed && notification.newChecked == null)
                continue;
            if(notification.newChecked){
                ApiService apiService = ApiFactory.getEvendateService();

                Observable<Response> notificationObservable =
                        apiService.setNotificationByType(EvendateAccountManager.peekToken(context), eventId, notification.type);
                notificationObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> Log.i(LOG_TAG, "notification added"),
                                error -> Log.e(LOG_TAG, error.getMessage()),
                                () -> Log.i(LOG_TAG, "completed"));
            }
            else{
                if(notification.notification == null)
                    return;
                ApiService apiService = ApiFactory.getEvendateService();

                Observable<Response> notificationObservable =
                        apiService.deleteNotification(EvendateAccountManager.peekToken(context), eventId, notification.notification.getUuid());
                notificationObservable.subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(result -> Log.i(LOG_TAG, "notification removed"),
                                error -> Log.e(LOG_TAG, error.getMessage()),
                                () -> Log.i(LOG_TAG, "completed"));
            }
        }
    }
}
