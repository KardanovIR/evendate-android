package ru.evendate.android.adapters;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.evendate.android.adapters.NotificationListAdapter.Notification;
import ru.evendate.android.adapters.NotificationListAdapter.NotificationType;
import ru.evendate.android.models.EventNotification;

/**
 * Created by Dmitry on 10.08.2016.
 */
public class NotificationConverter {

    public static ArrayList<Notification> convertNotificationList(List<EventNotification> eventNotifications) {
        ArrayList<Notification> list = new ArrayList<>();
        Set<EventNotification> set = new HashSet<>(eventNotifications);
        for (String type : getDefaultTypes()) {
            Notification notification = new Notification(type);
            EventNotification eventNotification = getNotificationWithType(set, type);
            if (eventNotification != null) {
                notification.checked = true;
                notification.notification = eventNotification;
            }
            list.add(notification);
            set.remove(eventNotification);
        }
        for (EventNotification eventNotification : set) {
            if (eventNotification.getNotificationType().equals("notification-now")
                    || eventNotification.getNotificationType().equals("notification-event-changed-dates"))
                continue;
            Notification notification = new Notification(eventNotification.getNotificationType());
            notification.checked = true;
            notification.notification = eventNotification;
            list.add(notification);
        }
        return list;
    }

    static String[] getDefaultTypes() {
        return new String[]{
                NotificationType.BEFORE_QUARTER_OF_HOUR.type,
                NotificationType.BEFORE_THREE_HOURS.type,
                NotificationType.BEFORE_DAY.type,
                NotificationType.BEFORE_THREE_DAYS.type,
                NotificationType.BEFORE_WEEK.type
        };
    }

    static EventNotification getNotificationWithType(Set<EventNotification> set, String type) {
        for (EventNotification notification : set) {
            if (notification.getNotificationType().equals(type))
                return notification;
        }
        return null;
    }
}
