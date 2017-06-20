package ru.evendate.android.models;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RegistrationField extends DataModel {

    @SerializedName("uuid")
    private String uuid;
    @SerializedName("type")
    private String type;
    @SerializedName("label")
    private String label;
    @SerializedName("required")
    private boolean required;
    @SerializedName("value")
    private String value;
    @Nullable
    @SerializedName("values")
    private ArrayList<RegistrationField> values;
    @SerializedName("error")
    private String error;

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
