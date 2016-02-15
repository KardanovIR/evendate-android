package ru.evendate.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ru.evendate.android.R;

/**
 * Created by ds_gordeev on 15.02.2016.
 */
public class UserProfileActivity extends AppCompatActivity {
    UserProfileFragment mUserProfileFragment;

@Override
protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        if(intent != null){
            int userId = Integer.parseInt(intent.getData().getLastPathSegment());
            mUserProfileFragment = UserProfileFragment.newInstance(userId);
        }
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.mipmap.ic_arrow_back_white);
        fragmentManager.beginTransaction().replace(R.id.main_content, mUserProfileFragment).commit();
    }
}