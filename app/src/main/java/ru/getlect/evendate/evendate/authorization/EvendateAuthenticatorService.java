package ru.getlect.evendate.evendate.authorization;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Dmitry on 07.09.2015.
 */
public class EvendateAuthenticatorService extends Service {

    // Instance field that stores the authenticator object
    private EvendateAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
        // Create a new authenticator object
        mAuthenticator = new EvendateAuthenticator(this);
    }
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}