package ru.evendate.android.ui.networking;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import ru.evendate.android.models.DataModel;

/**
 * Created by dmitry on 29.11.2017.
 */
@Parcel
public class NetworkingProfile extends DataModel {
    public static final String FIELDS_LIST = "request";

    @SerializedName("first_name")
    String firstName;
    @SerializedName("last_name")
    String lastName;
    @SerializedName("avatar_url")
    String avatarUrl;
    @SerializedName("event_id")
    int eventId;
    @SerializedName("user_id")
    int userId;
    @SerializedName("company_name")
    String companyName;
    @SerializedName("info")
    String info;
    @SerializedName("looking_for")
    String lookingFor;

    String email;
    @SerializedName("vk_url")
    String vkUrl;
    @SerializedName("facebook_url")
    String facebookUrl;
    @SerializedName("twitter_url")
    String twitter_url;
    @SerializedName("linkedin_url")
    String linkedinUrl;
    @SerializedName("telegram_url")
    String telegramUrl;
    @SerializedName("instagram_url")
    String instagramUrl;
    @SerializedName("github_url")
    String githubUrl;
    @SerializedName("signed_up")
    boolean signedUp;


    @Nullable
    @SerializedName("request_uuid")
    String requestUuid;
    @Nullable
    @SerializedName("outgoing_request_uuid")
    String outgoingRequestUuid;

    @Nullable
    NetworkingRequest request;


    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public int getEntryId() {
        return 0;
    }

    public int getEventId() {
        return eventId;
    }

    public int getUserId() {
        return userId;
    }

    public String getInfo() {
        return info;
    }

    public String getLookingFor() {
        return lookingFor;
    }

    public String getVkUrl() {
        return vkUrl;
    }

    public String getFacebookUrl() {
        return facebookUrl;
    }

    public String getTwitter_url() {
        return twitter_url;
    }

    public String getLinkedinUrl() {
        return linkedinUrl;
    }

    public String getTelegramUrl() {
        return telegramUrl;
    }

    public String getInstagramUrl() {
        return instagramUrl;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public String getEmail() {
        return email;
    }

    public String getCompanyName() {
        return companyName;
    }

    public boolean isSignedUp() {
        return signedUp;
    }


    @Nullable
    public String getRequestUuid() {
        return requestUuid;
    }

    @Nullable
    public NetworkingRequest getRequest() {
        return request;
    }
}