package ru.evendate.android.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

public class BaseActivity extends AppCompatActivity {

    //TODO deprecated parasha
    protected void onUpPressed() {
        ActivityManager activityManager = (ActivityManager)getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getClass().getName())) {
            startActivity(NavUtils.getParentActivityIntent(this));
        } else {
            onBackPressed();
        }
    }
}
