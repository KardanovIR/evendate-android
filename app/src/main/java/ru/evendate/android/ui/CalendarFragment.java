package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.style.ForegroundColorSpan;
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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import ru.evendate.android.R;
import ru.evendate.android.loaders.DateCalendarLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.models.DateCalendar;

/**
 * Created by fj on 28.09.2015.
 */
public class CalendarFragment extends Fragment  implements ReelFragment.OnEventsDataLoadedListener,
        OnDateChangedListener{
    private final String LOG_TAG = CalendarFragment.class.getSimpleName();
    private MaterialCalendarView mCalendarView;
    private ReelFragment mReelFragment;
    private OneDayDecorator mOneDayDecorator;
    public SlidingUpPanelLayout mSlidingUpPanelLayout;
    private Date minimumDate;
    private ToggleButton mToggleButton;
    private TextView mSelectedDateTextView;
    private TextView mEventCountTextView;
    private View mDragView;

    private DateCalendarLoader mLoader;
    private DateAdapter mAdapter;

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

        mSlidingUpPanelLayout = (SlidingUpPanelLayout)rootView.findViewById(R.id.sliding_layout);
        mSelectedDateTextView = (TextView)rootView.findViewById(R.id.calendar_date);
        mEventCountTextView = (TextView)rootView.findViewById(R.id.calendar_event_count);
        mCalendarView = (MaterialCalendarView)rootView.findViewById(R.id.calendarView);
        mDragView = rootView.findViewById(R.id.dragView);
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

        mToggleButton = (ToggleButton)rootView.findViewById(R.id.calendar_button);

        mSlidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelCollapsed(View panel) {
                mToggleButton.setChecked(false);
                //if (Build.VERSION.SDK_INT >= 21)
                    //Toolbar.setElevation(4.0f);
            }

            @Override
            public void onPanelExpanded(View panel) {
                mToggleButton.setChecked(true);
                //if (Build.VERSION.SDK_INT >= 21)
                    //Toolbar.setElevation(0.0f);
            }

            @Override
            public void onPanelAnchored(View panel) {

            }

            @Override
            public void onPanelHidden(View panel) {

            }
        });

        mAdapter = new DateAdapter();
        mLoader = new DateCalendarLoader(getActivity());
        mLoader.setLoaderListener(new LoaderListener<ArrayList<DateCalendar>>() {
            @Override
            public void onLoaded(ArrayList<DateCalendar> subList) {
                if(!isAdded())
                    return;
                mAdapter.setDateList(subList);
                mAdapter.setDates();
            }

            @Override
            public void onError() {
                AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(),
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mLoader.getData();
                        dialog.dismiss();
                    }
                });
                dialog.show();

            }
        });
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();

        mCalendarView.setSelectedDate(mOneDayDecorator.getDate());
        mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.CALENDAR.type(),
                mCalendarView.getSelectedDate().getDate(), false);
        mReelFragment.setDataListener(this);
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mReelFragment).commit();
    }

    @Override
    public void onDateChanged(MaterialCalendarView widget, CalendarDay date) {
        Log.i(LOG_TAG, date.toString());
        mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.CALENDAR.type(), date.getDate(), false);
        mReelFragment.setDataListener(this);
        mCalendarView.removeDecorator(mOneDayDecorator);
        mOneDayDecorator.setDate(date);
        mCalendarView.addDecorator(mOneDayDecorator);
        mReelFragment.setDate(date.getDate());
    }

    @Override
    public void onEventsDataLoaded() {
        if(!isAdded())
            return;
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

    class DateAdapter{
        private ArrayList<DateCalendar> mDateList;

        public ArrayList<DateCalendar> getDateList() {
            return mDateList;
        }
        public void setDateList(ArrayList<DateCalendar> dateList) {
            this.mDateList = dateList;
        }
        public void setDates(){
            ArrayList<CalendarDay> activeDates = new ArrayList<>();
            ArrayList<CalendarDay> favoritesDates = new ArrayList<>();
            for (DateCalendar date : mDateList){
                CalendarDay day = CalendarDay.from(new Date(date.getEventDate() * 1000));
                if(date.getEventCount() != 0)
                    activeDates.add(day);
                if(date.getFavoredCount() != 0)
                    favoritesDates.add(day);
            }
            EventActiveDecorator eventActiveDecorator = new EventActiveDecorator(activeDates);
            EventFavoriteDecorator eventFavoriteDecorator = new EventFavoriteDecorator(favoritesDates);
            mCalendarView.addDecorator(eventActiveDecorator);
            mCalendarView.addDecorator(eventFavoriteDecorator);
        }
    }

    /**
     * decorate favorite events
     */
    private class EventFavoriteDecorator implements DayViewDecorator {

        private final int color;
        private final ArrayList<CalendarDay> dates;

        public EventFavoriteDecorator(ArrayList<CalendarDay> dates) {
            this.color = getResources().getColor(R.color.accent);
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day) && day.getDate().getTime() > minimumDate.getTime();
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
            view.addSpan(new DotSpan(5, color));
        }
    }

    /**
     * decorate active date (with events)
     */
    private class EventActiveDecorator implements DayViewDecorator {

        private final ArrayList<CalendarDay> dates;

        public EventActiveDecorator(ArrayList<CalendarDay> dates) {
            this.dates = dates;
        }

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return dates.contains(day) && day.getDate().getTime() > minimumDate.getTime();
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(false);
        }
    }

    /**
     * decorate selected day
     */
    private class OneDayDecorator implements DayViewDecorator {

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

    /**
     * disable all day at the start of filling calendar
     */
    private class PrimeDayDisableDecorator implements DayViewDecorator {

        @Override
        public boolean shouldDecorate(CalendarDay day) {
            return true;
        }

        @Override
        public void decorate(DayViewFacade view) {
            view.setDaysDisabled(true);
        }
    }
}
