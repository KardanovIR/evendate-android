package ru.evendate.android.ui.networking;

import io.reactivex.Observable;

/**
 * Created by dmitry on 30.11.2017.
 */

public interface NetworkContract {
    interface OnProfileInteractionListener {
        void openProfile(NetworkingProfile networkingProfile);

        Observable<NetworkingProfile> applyRequest(NetworkingProfile networkingProfile);

        Observable<NetworkingProfile> hideRequest(NetworkingProfile networkingProfile);
    }
}
