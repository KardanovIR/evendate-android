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
    @SerializedName("order")
    Order order;
    @SerializedName("promocode")
    String promoCode;

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

    public void setPromocode(String code) {
        promoCode = code;
    }

    public Order getOrder() {
        return order;
    }
}
