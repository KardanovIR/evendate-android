package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

/**
 * Created by Dmitry on 02.02.2017.
 */

@Parcel
public class Registration extends DataModel {
    @SerializedName("registration_fields")
    ArrayList<RegistrationField> registrationFields;
    @SerializedName("tickets")
    ArrayList<Ticket> tickets;
    @SerializedName("orders")
    ArrayList<Order> orders;


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

    public void setTickets(ArrayList<Ticket> tickets) {
        this.tickets = tickets;
    }
}
