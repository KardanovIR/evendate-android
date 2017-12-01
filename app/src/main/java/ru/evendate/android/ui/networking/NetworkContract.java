package ru.evendate.android.ui.networking;

/**
 * Created by dmitry on 30.11.2017.
 */

public interface NetworkContract {
    interface OnProfileInteractionListener {
        void openProfile(Profile profile);
    }
}
