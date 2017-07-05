package ru.evendate.android.models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.ArrayList;

@Parcel
public class RegistrationField extends DataModel {

    @SerializedName("uuid")
    String uuid;
    @SerializedName("type")
    String type;
    @SerializedName("label")
    String label;
    @SerializedName("required")
    boolean required;
    @SerializedName("value")
    String value;
    @Nullable
    @SerializedName("values")
    ArrayList<RegistrationField> values;
    @SerializedName("error")
    String error;

    public RegistrationField() {}

    public RegistrationField(String uuid, String value) {
        this.uuid = uuid;
        this.value = value;
    }

    public RegistrationField(String uuid, @Nullable ArrayList<RegistrationField> values) {
        this.uuid = uuid;
        this.values = values;
    }

    public String getError() {
        return error;
    }

    @Override
    public int getEntryId() {
        return 0;
    }

    public String getUuid() {
        return uuid;
    }

    public String getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public boolean isRequired() {
        return required;
    }

    public String getValue() {
        return value;
    }

    @Nullable
    public ArrayList<RegistrationField> getValues() {
        return values;
    }
}
