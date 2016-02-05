package ru.evendate.android.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import ru.evendate.android.R;

/**
 * Created by Dmitry on 04.02.2016.
 */
public class UserListActivity extends AppCompatActivity {
    UserListFragment mUserListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_list);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Intent intent = getIntent();
        if(intent != null){
            Uri uri = intent.getData();
            int type = intent.getIntExtra(UserListFragment.TYPE, 0);
            int id = Integer.parseInt(uri.getLastPathSegment());
            if(type == UserListFragment.TypeFormat.event.nativeInt){
                mUserListFragment = UserListFragment.newInstance(type, id);
            } else {
                mUserListFragment = UserListFragment.newInstance(type, id);
            }
        }
        fragmentManager.beginTransaction().replace(R.id.main_content, mUserListFragment).commit();
    }
}
