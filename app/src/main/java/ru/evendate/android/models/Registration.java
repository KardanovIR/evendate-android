package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 02.02.2017.
 */

public class Registration extends DataModel {
    @SerializedName("registration_fields")
    private ArrayList<RegistrationField> registrationFields;
    @SerializedName("tickets")
    private ArrayList<Ticket> tickets;
    @SerializedName("orders")
    private ArrayList<Order> orders;


    @Override
    public int getEntryId() {
        return 0;
    }

    public ArrayList<RegistrationField> getRegistrationFieldsList() {
        return registrationFields;
    }

    public void setRegistrationFieldsList(ArrayList<RegistrationField> registrationFieldsList) {
        this.registrationFields = registrationFieldsList;
    }
}
