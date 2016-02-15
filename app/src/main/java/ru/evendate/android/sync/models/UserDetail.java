package ru.evendate.android.sync.models;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserDetail extends UserModel {
    public static final String FIELDS_LIST = UserModel.FIELDS_LIST + ",subscriptions";

    ArrayList<OrganizationDetail> subscriptions;

    public ArrayList<OrganizationDetail> getSubscriptions() {
        return subscriptions;
    }
}
