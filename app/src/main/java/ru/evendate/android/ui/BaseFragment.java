package ru.evendate.android.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;

import java.util.List;

public class BaseFragment extends Fragment {

    //TODO deprecated parasha
    protected void onUpPressed() {
        ActivityManager activityManager = (ActivityManager)getActivity().getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> taskList = activityManager.getRunningTasks(10);

        if (taskList.get(0).numActivities == 1 &&
                taskList.get(0).topActivity.getClassName().equals(getActivity().getClass().getName())) {
            getActivity().startActivity(NavUtils.getParentActivityIntent(getActivity()));
        } else {
            getActivity().onBackPressed();
        }
    }
}
