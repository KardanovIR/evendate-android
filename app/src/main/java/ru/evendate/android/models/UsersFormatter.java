package ru.evendate.android.models;

import android.content.Context;

import java.util.ArrayList;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 29.02.2016.
 */
public class UsersFormatter {
    public static String formatUsers(Context c, ArrayList<UserDetail> users) {
        int friend_count = 0;
        for (User user : users) {
            if (user.is_friend())
                friend_count++;
        }
        return users.size() + " " + c.getString(R.string.event_word_users_and)
                + " " + friend_count + " " + c.getString(R.string.event_word_friends_add);
    }

    public static String formatUsers(Context c, OrganizationDetail organization) {
        int friend_count = 0;
        for (User user : organization.getSubscribedUsersList()) {
            if (user.is_friend())
                friend_count++;
        }
        return organization.getSubscribedUsersList().size() + " " + c.getString(R.string.organization_word_and)
                + " " + friend_count + " " + c.getString(R.string.organization_word_friends);
    }
}
