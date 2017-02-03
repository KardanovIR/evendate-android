package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

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

    public String getError() {
        return error;
    }

    @SerializedName("error")
    private String error;

    @Override
    public int getEntryId() {
        return 0;
    }

    public String getUuid() {
        return uuid;
    }

    public RegistrationField(String uuid, String value) {
        this.uuid = uuid;
        this.value = value;
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
}
