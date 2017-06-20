package ru.evendate.android.ui.utils;

import android.content.Context;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.User;
import ru.evendate.android.models.UserDetail;

/**
 * Created by Dmitry on 29.02.2016.
 */
public class UsersFormatter {
    public static String formatUsers(Context c, ArrayList<UserDetail> users) {
        int friend_count = 0;
        for (User user : users) {
            if (user.isFriend())
                friend_count++;
        }
        return users.size() + " " + c.getString(R.string.event_word_users_and)
                + " " + friend_count + " " + c.getString(R.string.event_word_friends_add);
    }

    public static String formatUsers(Context c, OrganizationDetail organization) {
        return c.getString(R.string.organization_word_subscribers) + " - " +
                organization.getSubscribedCount() + " " + c.getString(R.string.organization_word_users);
    }
}
