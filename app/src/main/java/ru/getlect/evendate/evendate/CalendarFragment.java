package ru.getlect.evendate.evendate;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by fj on 28.09.2015.
 */
public class CalendarFragment extends Fragment {

    public static CalendarFragment newInstance() {
        CalendarFragment fragment = new CalendarFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_calendar, container, false);


//
//
//        DrawerLayout drawerLayout = (DrawerLayout)v.findViewById(R.id.drawer_layout);
//        drawerLayout.setStatusBarBackgroundColor(getResources().getColor(R.color.background_color_all));

//        AppCompatActivity activity = (AppCompatActivity) getActivity();
//        activity.setSupportActionBar(toolbar);
//        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);






        return v;

    }
}
