package ru.evendate.android.auth;

import ru.evendate.android.models.DataModel;

/**
 * Created by dmitry on 07.09.17.
 */

@SuppressWarnings("WeakerAccess")
public class AuthUrls extends DataModel {
    String vk;
    String google;
    String facebook;

    @Override
    public int getEntryId() {
        throw new RuntimeException();
    }

    public String getVk() {
        return vk;
    }

    public String getGoogle() {
        return google;
    }

    public String getFacebook() {
        return facebook;
    }
}
