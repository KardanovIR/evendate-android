package ru.evendate.android.ui.utils;

import ru.evendate.android.models.User;

/**
 * Created by Aedirn on 17.03.17.
 */

public class UserFormatter {
    public static String formatUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
