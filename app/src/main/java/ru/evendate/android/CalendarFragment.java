package ru.evendate.android;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.UpdateAppearance;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;
import com.prolificinteractive.materialcalendarview.format.TitleFormatter;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.lang.reflect.Method;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.utils.Utils;

/**
 * Created by fj on 28.09.2015.
 */
public class CalendarFragment extends Fragment  implements LoaderManager.LoaderCallbacks<Cursor>,
        ReelFragment.OnEventsDataLoadedListener, OnDateChangedListener{
    private final String LOG_TAG = CalendarFragment.class.getSimpleName();
    private MaterialCalendarView mCalendarView;
    private ReelFragment mReelFragment;
    private EventDecorator mEventDecorator;
    private OneDayDecorator mOneDayDecorator;
    public SlidingUpPanelLayout mSlidingUpPanelLayout;
    private Date minimumDate;
    private ToggleButton mToggleButton;
    private TextView mSelectedDateTextView;
    private TextView mEventCountTextView;
    private View Toolbar;
    private View mDragView;

    final int DATES_LOADER_ID = 0;

    /**
     * change localize months in rus
     * //TODO move to strings
     */
    private static DateFormatSymbols myDateFormatSymbols = new DateFormatSymbols(){

        @Override
        public String[] getMonths() {
            return new String[]{"январь", "февраль", "март", "апрель", "май", "июнь",
                    "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"};
        }

    };
    class MyTitleFormatter implements TitleFormatter{
        public CharSequence format(CalendarDay day){
            SimpleDateFormat format = new SimpleDateFormat("MMMM yyyy", myDateFormatSymbols);
            return format.format(day.getDate());
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_calendar, container, false);


        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((Toolbar)rootView.findViewById(R.id.toolbar)).setNavigationIcon(R.mipmap.ic_arrow_back_white);

        mSlidingUpPanelLayout = (SlidingUpPanelLayout)rootView.findViewById(R.id.sliding_layout);
        mSelectedDateTextView = (TextView)rootView.findViewById(R.id.calendar_date);
        mEventCountTextView = (TextView)rootView.findViewById(R.id.calendar_event_count);
        mCalendarView = (MaterialCalendarView)rootView.findViewById(R.id.calendarView);
        mDragView = rootView.findViewById(R.id.dragView);
        Toolbar = rootView.findViewById(R.id.toolbar);
        mCalendarView.getCurrentDate();
        mCalendarView.setOnDateChangedListener(this);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.add(Calendar.DATE, -1);
        minimumDate = calendar.getTime();
        mCalendarView.setMinimumDate(calendar.getTime());
        mCalendarView.addDecorator(new PrimeDayDisableDecorator());
        mOneDayDecorator = new OneDayDecorator();
        mCalendarView.addDecorator(mOneDayDecorator);
        mCalendarView.setShowOtherDates(true);
        mCalendarView.setTitleFormatter(new MyTitleFormatter());
        getLoaderManager().initLoader(DATES_LOADER_ID, null, this);

        mToggleButton = (ToggleButton)rootView.findViewById(R.id.calendar_button);

        mSlidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                mToggleButton.setChecked(false);
                if (Build.VERSION.SDK_INT >= 21)
                    Toolbar.setElevation(4.0f);
            }

            @Override
            public void onPanelExpanded(View panel) {
                mToggleButton.setChecked(true);
                if (Build.VERSION.SDK_INT >= 21)
                    Toolbar.setElevation(0.0f);
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });
        return rootView;
    }
    final class ColoredUnderlineSpan extends CharacterStyle implements UpdateAppearance {
        private final int mColor;

        public ColoredUnderlineSpan(final int color) {
            mColor = color;
        }

        @Override
        public void updateDrawState(final TextPaint tp) {
            try {
                final Method method = TextPaint.class.getMethod("setUnderlineText",
                        Integer.TYPE,
                        Float.TYPE);
                method.invoke(tp, mColor, 1.0f);
            } catch (final Exception e) {
                tp.setUnderlineText(true);
            }
        }
    }
    @Override
    public void onResume() {
        super.onResume();

        mCalendarView.setSelectedDate(mOneDayDecorator.getDate());
        mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.calendar.nativeInt,
                mCalendarView.getSelectedDate().getDate(), false);
        mReelFragment.setDataListener(this);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mReelFragment).commit();
    }

    @Override
    public void onDateChanged(MaterialCalendarView widget, CalendarDay date) {
        Log.i(LOG_TAG, date.toString());
        //mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.calendar.nativeInt, date.getDate(), false);
        //mReelFragment.setDataListener(this);
        mCalendarView.removeDecorator(mOneDayDecorator);
        mOneDayDecorator.setDate(date);
        mCalendarView.addDecorator(mOneDayDecorator);
        mReelFragment.setDate(date.getDate());
    }

    @Override
    public void onEventsDataLoaded() {
        Log.i(LOG_TAG, "data loaded");
        //mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
        mEventCountTextView.setText(mReelFragment.getEventList().size() + " " + getString(R.string.calendar_events));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("cc, d MMMM", Locale.getDefault());
        mSelectedDateTextView.setText(simpleDateFormat.format(mCalendarView.getSelectedDate().getDate()));
        //mReelFragment.setRecyclerViewOnScrollListener(new RecyclerView.OnScrollListener() {
//
        //    @Override
        //    public void onScrollStateChanged(RecyclerView view, int scrollState) {
        //    }
//
        //    @Override
        //    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        //        super.onScrolled(recyclerView, dx, dy);
        //        boolean enable = false;
        //        if (recyclerView != null && recyclerView.getChildCount() > 0) {
        //            // check if the first item of the list is visible
        //            // check if the top of the first item is visible
        //            boolean verticalScrollOffset = recyclerView.computeVerticalScrollOffset() == 0;
        //            // enabling or disabling the refresh layout
        //            enable = verticalScrollOffset;
        //        }
        //        if (enable)
        //            mSlidingUpPanelLayout.setDragView(mDragView);
        //        else
        //            mSlidingUpPanelLayout.setDragView(null);
        //    }
        //});
    }
    public class EventDecorator implements DayViewDecorator {

        private final int color;
        private final HashMap<CalendarDay, Boolean> dates;

        public EventDecorator(HashMap<CalendarDay, Boolean> dates) {
            this.color = getResources().getColor(R.color.accent);
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.keySet().contains(day) && dates.get(day) &&
                    day.getDate().getTime() > minimumDate.getTime();
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
            view.addSpan(new DotSpan(5, color));
        }
    }
    public class EventActiveDecorator implements DayViewDecorator {

        private final HashMap<CalendarDay, Boolean> dates;

        public EventActiveDecorator(HashMap<CalendarDay, Boolean> dates) {
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.keySet().contains(day) &&
                    day.getDate().getTime() > minimumDate.getTime();
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
        }
    }

    /**
     * decorate selected day
     */
    public class OneDayDecorator implements DayViewDecorator {

        private CalendarDay date;

        public OneDayDecorator() {
            date = CalendarDay.today();
        }

        public void setDate(CalendarDay date) {
            this.date = date;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return date != null && day.equals(date);
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.addSpan(new ForegroundColorSpan(Color.WHITE));
        }

        /**
         * We're changing the internals, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
         */
        public void setDate(Date date) {
            this.date = CalendarDay.from(date);
        }

        public CalendarDay getDate() {
            return date;
        }
    }

    public void setActiveDays(HashMap<String, Boolean> datesList){
        HashMap<CalendarDay, Boolean> dates = new HashMap<>();
        for(String dateString : datesList.keySet()){
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
            if(!dates.containsKey(CalendarDay.from(calendar)))
                dates.put(CalendarDay.from(calendar), datesList.get(dateString));
            else{
                if(datesList.get(dateString))
                    dates.remove(CalendarDay.from(calendar));
                    dates.put(CalendarDay.from(calendar), true);
            }
        }

        EventActiveDecorator eventActiveDecorator = new EventActiveDecorator(dates);
        mCalendarView.addDecorator(eventActiveDecorator);
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
                                appendPath(EvendateContract.PATH_DATES).appendQueryParameter("with_favorites", "1").build(),
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
        HashMap<String, Boolean> list = new HashMap<>();
        while (data.moveToNext()){
            String date = data.getString(data.getColumnIndex(EvendateContract.EventDateEntry.COLUMN_DATE));
            boolean favorite = data.getInt(data.getColumnIndex(EvendateContract.EventEntry.COLUMN_IS_FAVORITE)) == 1;
            if(list.get(date) == null || !list.get(date))
                list.put(date, favorite);
        }
        setActiveDays(list);

        list.clear();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
