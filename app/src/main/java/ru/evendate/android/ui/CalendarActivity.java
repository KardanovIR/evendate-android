package ru.evendate.android.ui;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.transition.TransitionManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.models.DateCalendar;
import ru.evendate.android.models.EventDate;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.ResponseArray;
import ru.evendate.android.views.LoadStateView;

/**
 * Created by fj on 28.09.2015.
 */
public class CalendarActivity extends AppCompatActivity implements ReelFragment.OnEventsDataLoadedListener,
        OnDateChangedListener, LoadStateView.OnReloadListener {
    /**
     * change localize months in rus
     */
    private static DateFormatSymbols dateFormatMonths;
    private final String LOG_TAG = CalendarActivity.class.getSimpleName();
    @Bind(R.id.calendarView) MaterialCalendarView mCalendarView;
    @Bind(R.id.calendar_button) ToggleButton mToggleButton;
    @Bind(R.id.calendar_date) TextView mSelectedDateTextView;
    @Bind(R.id.calendar_event_count) TextView mEventCountTextView;
    @Bind(R.id.toolbar) Toolbar mToolbar;
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;
    private ReelFragment mReelFragment;
    private OneDayDecorator mOneDayDecorator;
    private Date yesterdayDate;
    private BottomSheetBehavior<View> behavior;
    private DateAdapter mAdapter;
    private DrawerWrapper mDrawer;

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
        mLoadStateView.setOnReloadListener(this);
    }

    @SuppressWarnings("ConstantConditions")
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
        if (getResources().getConfiguration().locale.getLanguage().equals("ru"))
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
        View bottomSheet = mCoordinatorLayout.findViewById(R.id.bottom_sheet);
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
    protected void onStart() {
        super.onStart();
        mCalendarView.setVisibility(View.GONE);
        mLoadStateView.showProgress();
        mDrawer.getDrawer().setSelection(DrawerWrapper.CALENDAR_IDENTIFIER);
        mDrawer.start();
        loadDates();
    }

    @Override
    public void onReload() {
        loadDates();
    }

    private void setToolbarDate(CalendarDay date) {
        String month = dateFormatMonths.getMonths()[date.getMonth()];
        month = capitalize(month);
        mToolbar.setTitle(month);
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void loadDates() {
        ApiService apiService = ApiFactory.getService(this);
        Observable<ResponseArray<DateCalendar>> observable =
                apiService.getCalendarDates(EvendateAccountManager.peekToken(this), true, true, true, EventDate.FIELDS_LIST);

        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> onLoadedDates(result.getData()),
                        this::onError,
                        mLoadStateView::hideProgress
                );
    }

    public void onLoadedDates(ArrayList<DateCalendar> dateList) {
        Log.i(LOG_TAG, "loaded");
        if (Build.VERSION.SDK_INT > 19)
            TransitionManager.beginDelayedTransition(mCoordinatorLayout);
        mCalendarView.setVisibility(View.VISIBLE);

        mAdapter.setDateList(dateList);
        mAdapter.setDates();
        mReelFragment.setDateAndReload(mCalendarView.getSelectedDate().getDate());
        setSelectedDate();
    }

    public void onError(Throwable error) {
        Log.e(LOG_TAG, "" + error.getMessage());
        mLoadStateView.showErrorHint();
    }

    @Override
    public void onDateChanged(@NonNull MaterialCalendarView widget, CalendarDay date) {
        Log.i(LOG_TAG, date.toString());
        mCalendarView.removeDecorator(mOneDayDecorator);
        mOneDayDecorator.setDate(date);
        mCalendarView.addDecorator(mOneDayDecorator);
        mReelFragment.setDateAndReload(date.getDate());
        setSelectedDate();
    }

    private void setSelectedDate() {
        mSelectedDateTextView.setText(DateFormatter.formatCalendarLabel(mCalendarView.getSelectedDate().getDate()));
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

        void setDateList(ArrayList<DateCalendar> dateList) {
            this.mDateList = dateList;
        }

        void setDates() {
            ArrayList<CalendarDay> activeDates = new ArrayList<>();
            ArrayList<CalendarDay> favoritesDates = new ArrayList<>();
            for (DateCalendar date : mDateList) {
                CalendarDay day = CalendarDay.from(date.getEventDate());
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

        EventFavoriteDecorator(ArrayList<CalendarDay> dates) {
            this.color = ContextCompat.getColor(getBaseContext(), R.color.accent);
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

        EventActiveDecorator(ArrayList<CalendarDay> dates) {
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

        OneDayDecorator() {
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

        public CalendarDay getDate() {
            return date;
        }

        /**
         * We're changing the internals, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
         */
        public void setDate(Date date) {
            this.date = CalendarDay.from(date);
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
    private class CalendarNavigationItemClickListener extends DrawerWrapper.NavigationItemSelectedListener {

        CalendarNavigationItemClickListener(Activity context, Drawer drawer) {
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
