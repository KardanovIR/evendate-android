package ru.evendate.android.models;

/**
 * Created by Aedirn on 17.03.17.
 */

public class UserFormatter {
    public static String formatUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
