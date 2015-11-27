package ru.getlect.evendate.evendate;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.utils.Utils;

/**
 * Created by fj on 28.09.2015.
 */
public class CalendarFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>,
        ReelFragment.OnEventsDataLoadedListener, OnDateChangedListener{
    private final String LOG_TAG = CalendarFragment.class.getSimpleName();
    private MaterialCalendarView mCalendarView;
    private ReelFragment mReelFragment;
    private EventDecorator mEventDecorator;
    public SlidingUpPanelLayout mSlidingUpPanelLayout;

    final int DATES_LOADER_ID = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);


        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((Toolbar)rootView.findViewById(R.id.toolbar)).setNavigationIcon(R.mipmap.ic_arrow_back_white);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout)rootView.findViewById(R.id.sliding_layout);
        mCalendarView = (MaterialCalendarView)rootView.findViewById(R.id.calendarView);
        mCalendarView.getCurrentDate();
        mCalendarView.setOnDateChangedListener(this);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.add(Calendar.DATE, -1);
        mCalendarView.setMinimumDate(calendar.getTime());
        mCalendarView.addDecorator(new PrimeDayDisableDecorator());
        mCalendarView.setShowOtherDates(true);
        getLoaderManager().initLoader(DATES_LOADER_ID, null, this);

        return rootView;
    }

    @Override
    public void onDateChanged(MaterialCalendarView widget, CalendarDay date) {
        Log.i(LOG_TAG, date.toString());
        mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.calendar.nativeInt, date.getDate(), this);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mReelFragment).commit();
    }

    @Override
    public void onEventsDataLoaded() {
        Log.i(LOG_TAG, "data loaded");
        mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }
    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashSet<CalendarDay> dates;

        public EventDecorator(Collection<CalendarDay> dates) {
            this.color = Color.RED;
            this.dates = new HashSet<>(dates);
        }

        public void addDate(CalendarDay calendarDay){
            dates.add(calendarDay);
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
            view.addSpan(new DotSpan(5, color));
        }
    }
    public void setActiveDays(ArrayList<String> datesList){
        HashSet<CalendarDay> dates = new HashSet<>();
        for(String dateString : datesList){
            Date dateStamp;
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat format2 = new SimpleDateFormat("yyyy-MM-dd");
            dateStamp = Utils.formatDate(dateString, format);
            if(dateStamp == null){
                dateStamp = Utils.formatDate(dateString, format2);
            }
            if(dateStamp == null)
                break;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dateStamp);
            if(!dates.contains(CalendarDay.from(calendar)))
                dates.add(CalendarDay.from(calendar));
        }

        mEventDecorator = new EventDecorator(dates);
        mCalendarView.addDecorator(mEventDecorator);
    }
    private static class PrimeDayDisableDecorator implements DayViewDecorator {

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return true;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case DATES_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        EvendateContract.BASE_CONTENT_URI.buildUpon().
                                appendPath(EvendateContract.PATH_DATES).build(),
                        null,
                        null,
                        null,
                        null
                );
                default:
                    throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<String> list = new ArrayList<>();
        while (data.moveToNext()){
            list.add(data.getString(data.getColumnIndex(EvendateContract.EventDateEntry.COLUMN_DATE)));
        }
        setActiveDays(list);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
