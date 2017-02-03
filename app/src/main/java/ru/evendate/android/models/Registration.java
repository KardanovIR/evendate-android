package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Dmitry on 02.02.2017.
 */

public class Registration extends DataModel {
    @SerializedName("registration_fields")
    private ArrayList<RegistrationField> registrationFieldsList;


    @Override
    public int getEntryId() {
        return 0;
    }

    public ArrayList<RegistrationField> getRegistrationFieldsList() {
        return registrationFieldsList;
    }

    public void setRegistrationFieldsList(ArrayList<RegistrationField> registrationFieldsList) {
        this.registrationFieldsList = registrationFieldsList;
    }
}
