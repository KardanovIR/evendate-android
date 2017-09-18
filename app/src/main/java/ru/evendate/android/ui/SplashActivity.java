package ru.evendate.android.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;

import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.auth.AuthDialog;
import ru.evendate.android.ui.feed.MainActivity;

/**
 * Created by Aedirn on 01.07.16.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setAllowEnterTransitionOverlap(true);
            getWindow().setExitTransition(new Fade());
        }
        if (!EvendateAccountManager.getFirstAuthDone(this)) {
            AuthDialog authDialog = new AuthDialog();
            authDialog.setAuthListener(new AuthDialog.AuthListener() {
                @Override
                public void OnAuthDone(String newToken) {
                    EvendateAccountManager.setFirstAuthDone(getApplicationContext());
                    startMain();
                }

                @Override
                public void OnAuthSkipped() {
                    EvendateAccountManager.setFirstAuthDone(getApplicationContext());
                    startMain();
                }
            });
            authDialog.show(getSupportFragmentManager(), "auth");
        } else {
            startMain();
        }
    }

    private void startMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(this).toBundle());
        finish();
    }
}
