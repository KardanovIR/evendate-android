package ru.evendate.android.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Fade;
import android.view.Window;

/**
 * Created by Aedirn on 01.07.16.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Intent intent = new Intent(this, MainActivity.class);

        if(Build.VERSION.SDK_INT >= 21){
            getWindow().setAllowEnterTransitionOverlap(true);
            getWindow().setExitTransition(new Fade());
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }
        else
            startActivity(intent);
        finish();
    }
}
