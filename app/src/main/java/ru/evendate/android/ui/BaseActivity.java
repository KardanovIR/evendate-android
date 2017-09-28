package ru.evendate.android.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.support.annotation.CallSuper;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import io.reactivex.Observable;
import ru.evendate.android.auth.AuthDialog;

public abstract class BaseActivity extends AppCompatActivity implements AuthHandler {
    private static final String TAG_AUTH = "tag_auth";
    protected DrawerWrapper mDrawer;
    private AuthDialog mAuthDialog;

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

    @Override
    protected void onStart() {
        super.onStart();
        mDrawer.setLogOutListener(this::onReload);
        mDrawer.start();
    }

    @CallSuper
    protected void onReload() {
        mDrawer.update();
    }

    protected abstract void initDrawer();

    @Override
    public final Observable<String> requestAuth() {
        AuthDialog authDialog = (AuthDialog)getSupportFragmentManager().findFragmentByTag(TAG_AUTH);
        if (mAuthDialog == null && authDialog == null) {
            mAuthDialog = new AuthDialog();
            mAuthDialog.show(getSupportFragmentManager(), TAG_AUTH);
        } else if (authDialog != null) {
            mAuthDialog = authDialog;
        }
        mAuthDialog.getAuthObservable().subscribe((String newToken) -> onReload(), (Throwable e) -> e.printStackTrace(), () -> mAuthDialog = null);
        return mAuthDialog.getAuthObservable();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AuthDialog authDialog = (AuthDialog)getSupportFragmentManager().findFragmentByTag(TAG_AUTH);
        if (authDialog != null) {
            authDialog.onActivityResult(requestCode, resultCode, data);
        }
    }
}
