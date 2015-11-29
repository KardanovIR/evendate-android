package ru.getlect.evendate.evendate;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class CalendarActivity extends AppCompatActivity {
    CalendarFragment mCalendarFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mCalendarFragment = new CalendarFragment();
        fragmentManager.beginTransaction().replace(R.id.main_content, mCalendarFragment).commit();

    }

    @Override
    public void onBackPressed() {
        if (mCalendarFragment.mSlidingUpPanelLayout != null &&
                (mCalendarFragment.mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED
                        || mCalendarFragment.mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)) {
            mCalendarFragment.mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        } else {
            super.onBackPressed();
        }
    }
}
