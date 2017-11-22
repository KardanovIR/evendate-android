package ru.evendate.android.models;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

/**
 * Created by dmitry on 30.10.2017.
 */

@Parcel
public class Price {

    @SerializedName("discount")
    float discount;
    @SerializedName("promocode_discount")
    float promoCodeDiscount;
    @SerializedName("final_sum")
    float finalSum;

    public float getDynamicDiscount() {
        return discount;
    }

    public float getPromoCodeDiscount() {
        return promoCodeDiscount;
    }

    public float getFinalSum() {
        return finalSum;
    }
}
