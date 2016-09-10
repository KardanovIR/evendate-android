package ru.evendate.android.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateChangedListener;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.DateCalendar;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by fj on 28.09.2015.
 */
public class CalendarActivity extends AppCompatActivity implements ReelFragment.OnEventsDataLoadedListener,
        OnDateChangedListener {
    private final String LOG_TAG = CalendarActivity.class.getSimpleName();
    @Bind(R.id.calendarView) MaterialCalendarView mCalendarView;
    private ReelFragment mReelFragment;
    private OneDayDecorator mOneDayDecorator;
    private Date yesterdayDate;
    @Bind(R.id.calendar_button) ToggleButton mToggleButton;
    @Bind(R.id.calendar_date) TextView mSelectedDateTextView;
    @Bind(R.id.calendar_event_count) TextView mEventCountTextView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    private BottomSheetBehavior<View> behavior;

    private DateAdapter mAdapter;
    private DrawerWrapper mDrawer;
    AlertDialog errorDialog;

    /**
     * change localize months in rus
     */
    private static DateFormatSymbols dateFormatMonths;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        ButterKnife.bind(this);

        initToolbar();
        initCalendar();
        initBottomSheet();
        initReel();
        initDrawer();
        mAdapter = new DateAdapter();

        setToolbarDate(mCalendarView.getCurrentDate());
        loadDates();
        mDrawer.getDrawer().setSelection(DrawerWrapper.CALENDAR_IDENTIFIER);
        mDrawer.start();
    }

    private void initToolbar() {
        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        mToolbar.setNavigationOnClickListener((View v) -> mDrawer.getDrawer().openDrawer());
    }

    private void initCalendar() {
        mCalendarView.getCurrentDate();
        mCalendarView.setOnDateChangedListener(this);
        yesterdayDate = getYesterdayDate();
        mCalendarView.setMinimumDate(yesterdayDate);
        mCalendarView.setOnMonthChangedListener(
                (MaterialCalendarView widget, CalendarDay date) -> setToolbarDate(date)
        );
        mCalendarView.addDecorator(new PrimeDayDisableDecorator());
        mOneDayDecorator = new OneDayDecorator();
        mCalendarView.addDecorator(mOneDayDecorator);
        mCalendarView.setShowOtherDates(true);

        mCalendarView.setSelectedDate(mOneDayDecorator.getDate());

        dateFormatMonths = new DateFormatSymbols();
        if(getResources().getConfiguration().locale.getLanguage().equals("ru"))
            dateFormatMonths.setMonths(
                    new String[]{"январь", "февраль", "март", "апрель", "май", "июнь",
                            "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь"});
    }

    private Date getYesterdayDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    private void initBottomSheet() {
        View bottomSheet = coordinatorLayout.findViewById(R.id.bottom_sheet);
        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setPeekHeight(getPeekHeightInPx());
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (Build.VERSION.SDK_INT < 21)
                    return;
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    mAppBarLayout.setElevation(0.0f);
                    mToggleButton.setChecked(true);
                } else {
                    mAppBarLayout.setElevation(getResources().getDimensionPixelSize(R.dimen.toolbarElevation));
                    mToggleButton.setChecked(false);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
        });
    }
    private int getPeekHeightInPx() {
        return getResources().getDimensionPixelSize(R.dimen.calendar_slide_height);
    }

    private void initReel() {
        mReelFragment = ReelFragment.newInstance(ReelFragment.ReelType.CALENDAR.type(),
                mCalendarView.getSelectedDate().getDate(), false);
        mReelFragment.setDataListener(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mReelFragment).commit();
    }
    private void initDrawer() {
        mDrawer = DrawerWrapper.newInstance(this);
        mDrawer.getDrawer().setOnDrawerItemClickListener(
                new CalendarNavigationItemClickListener(this, mDrawer.getDrawer()));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(errorDialog != null)
            errorDialog.dismiss();
    }

    private void setToolbarDate(CalendarDay date){
        String month = dateFormatMonths.getMonths()[date.getMonth()];
        month = capitalize(month);
        mToolbar.setTitle(month);
    }
    private String capitalize(String str){
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void loadDates() {
        ApiService apiService = ApiFactory.getEvendateService();
        Observable<ResponseArray<DateCalendar>> observable =
                apiService.getCalendarDates(EvendateAccountManager.peekToken(this), true, true, true, null);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoadedDates(result.getData()),
                        this::onError
                );
    }

    public void onLoadedDates(ArrayList<DateCalendar> dateList) {
        Log.i(LOG_TAG, "loaded");
        mAdapter.setDateList(dateList);
        mAdapter.setDates();
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, error.getMessage());
        errorDialog = ErrorAlertDialogBuilder.newInstance(getBaseContext(),
                (DialogInterface dialog, int which) -> {
                        loadDates();
                        dialog.dismiss();
                });
        errorDialog.show();
    }

    @Override
    public void onDateChanged(MaterialCalendarView widget, CalendarDay date) {
        Log.i(LOG_TAG, date.toString());
        mCalendarView.removeDecorator(mOneDayDecorator);
        mOneDayDecorator.setDate(date);
        mCalendarView.addDecorator(mOneDayDecorator);
        mReelFragment.setDateAndReload(date.getDate());
        setSelectedDate();
    }

    private void setSelectedDate(){
        SimpleDateFormat dateFormat = new SimpleDateFormat("cc, d MMMM", Locale.getDefault());
        mSelectedDateTextView.setText(dateFormat.format(mCalendarView.getSelectedDate().getDate()));
    }

    @Override
    public void onEventsDataLoaded() {
        Log.i(LOG_TAG, "data loaded");
        //TODO нужно как-то изящнее это сделать
        if (mReelFragment.getAdapter() != null && mReelFragment.getAdapter().isEmpty())
            return;
        //mEventCountTextView.setText(mReelFragment.getEventList().size() + " " + getString(R.string.calendar_events));
    }

    class DateAdapter {
        private ArrayList<DateCalendar> mDateList;

        public ArrayList<DateCalendar> getDateList() {
            return mDateList;
        }

        public void setDateList(ArrayList<DateCalendar> dateList) {
            this.mDateList = dateList;
        }

        public void setDates() {
            ArrayList<CalendarDay> activeDates = new ArrayList<>();
            ArrayList<CalendarDay> favoritesDates = new ArrayList<>();
            for (DateCalendar date : mDateList) {
                CalendarDay day = CalendarDay.from(new Date(date.getEventDate() * 1000));
                if (date.getEventCount() != 0)
                    activeDates.add(day);
                if (date.getFavoredCount() != 0)
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
            return dates.contains(day) && day.getDate().getTime() > yesterdayDate.getTime();
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
            return dates.contains(day) && day.getDate().getTime() > yesterdayDate.getTime();
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

    /**
     * handle clicks on items of navigation drawer list in main activity
     */
    private class CalendarNavigationItemClickListener extends NavigationItemSelectedListener {

        public CalendarNavigationItemClickListener(Activity context, Drawer drawer) {
            super(context, drawer);
            mContext = context;
        }

        @Override
        public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
            switch (drawerItem.getIdentifier()) {
                case DrawerWrapper.CALENDAR_IDENTIFIER:
                    mDrawer.closeDrawer();
                    break;
                default:
                    super.onItemClick(view, position, drawerItem);
            }
            return true;
        }
    }
}
