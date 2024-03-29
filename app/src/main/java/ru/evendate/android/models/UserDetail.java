package ru.evendate.android.models;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
@Parcel
public class UserDetail extends User {
    public static final String FIELDS_LIST = User.FIELDS_LIST + ",subscriptions{fields:'" + OrganizationSubscription.FIELDS_LIST + "'}";

    ArrayList<OrganizationFull> subscriptions;

    public ArrayList<OrganizationFull> getSubscriptions() {
        return subscriptions;
    }
}
