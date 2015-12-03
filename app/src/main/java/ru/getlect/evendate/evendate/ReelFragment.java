package ru.getlect.evendate.evendate;

/**
 * Created by Dmitry on 23.09.2015.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.chalup.microorm.MicroOrm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateApiFactory;
import ru.getlect.evendate.evendate.sync.EvendateService;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.LocalDataFetcher;
import ru.getlect.evendate.evendate.sync.ServerDataFetcher;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModelWithEvents;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReelFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private String LOG_TAG = ReelFragment.class.getSimpleName();

    private android.support.v7.widget.RecyclerView mRecyclerView;

    private final static int EVENT_INFO_LOADER_ID = 0;
    private EventAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ProgressBar mProgressBar;
    boolean refreshingEnabled = false;

    private ArrayList<EventModel> mEventList;


    private int organizationId;

    private int type = 0;
    private Date mDate;

    public enum TypeFormat {
        feed                (0),
        favorites           (1),
        organization        (2),
        organizationSubscribed  (3),
        calendar  (4);

        TypeFormat(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }

    private OnEventsDataLoadedListener mDataListener;


    public static ReelFragment newInstance(int type, int organizationId, boolean enableRefreshing){
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.organizationId = organizationId;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }
    public static ReelFragment newInstance(int type, boolean enableRefreshing){
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }
    public static ReelFragment newInstance(int type, Date date, boolean enableRefreshing){
        ReelFragment reelFragment = new ReelFragment();
        reelFragment.type = type;
        reelFragment.mDate = date;
        reelFragment.refreshingEnabled = enableRefreshing;
        return reelFragment;
    }

    public void setDataListener(OnEventsDataLoadedListener dataListener) {
        this.mDataListener = dataListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reel, container, false);
        mProgressBar = (ProgressBar)rootView.findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.accent), PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        if(type == TypeFormat.organization.nativeInt){
            if(!EvendateSyncAdapter.checkInternetConnection(getContext())){
                Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            }else{
                OrganizationAsyncLoader organizationAsyncLoader = new OrganizationAsyncLoader(getContext());
                organizationAsyncLoader.execute();
            }
        }

        mSwipeRefreshLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipe_refresh_layout);
        if(!refreshingEnabled)
            mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!EvendateSyncAdapter.checkInternetConnection(getContext())){
                    Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
                    mSwipeRefreshLayout.setRefreshing(false);
                    return;
                }
                Log.d(LOG_TAG, "request sync");
                EvendateSyncAdapter.syncImmediately(getContext());
            }
        });
        mAdapter = new EventAdapter(getActivity(), type);
        mRecyclerView.setAdapter(mAdapter);

        if(type != TypeFormat.organization.nativeInt){
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(EVENT_INFO_LOADER_ID, null, this);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(RecyclerView view, int scrollState) {
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                boolean enable = false;
                if (mRecyclerView != null && mRecyclerView.getChildCount() > 0) {
                    // check if the first item of the list is visible
                    // check if the top of the first item is visible
                    boolean verticalScrollOffset = mRecyclerView.computeVerticalScrollOffset() == 0;
                    // enabling or disabling the refresh layout
                    enable = verticalScrollOffset;
                }
                if(refreshingEnabled)
                    mSwipeRefreshLayout.setEnabled(enable);
            }
        });
        return rootView;
    }

    private BroadcastReceiver syncFinishedReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            mSwipeRefreshLayout.setRefreshing(false);
            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(syncFinishedReceiver, new IntentFilter(EvendateSyncAdapter.SYNC_FINISHED));
    }
    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(syncFinishedReceiver);
    }
    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        //Log.d(TAG, "onCreateLoader: " + id);
//
        String selection = "";
        if(type == TypeFormat.favorites.nativeInt)
            selection = EvendateContract.EventEntry.COLUMN_IS_FAVORITE + " = 1";
                    //+ " AND " + EvendateContract.EventDateEntry.COLUMN_DATE + "> date('now')";
        else if(type == TypeFormat.organizationSubscribed.nativeInt){
            selection = EvendateContract.EventEntry.TABLE_NAME + "." +
                            EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID + "=" + organizationId;
                    //+ " AND " + EvendateContract.EventDateEntry.COLUMN_DATE + "> date('now')";
        }
        else if(type == TypeFormat.calendar.nativeInt){
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(mDate);
            calendar.add(Calendar.DATE, 1);
            calendar.add(Calendar.SECOND, -1);
            selection = EvendateContract.EventDateEntry.COLUMN_DATE + " BETWEEN DATETIME(" + mDate.getTime() / 1000L + ", 'unixepoch') AND DATETIME(" + calendar.getTime().getTime() / 1000L + ", 'unixepoch')";
        }
        switch (id) {
            case EVENT_INFO_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        EventAdapter.mUri,
                        null,
                        selection,
                        null,
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        //Log.d(TAG, "onLoadFinished: " + loader.getId());
//
        switch (loader.getId()) {
            case EVENT_INFO_LOADER_ID:
                mEventList = new ArrayList<>();
                MicroOrm mOrm = new MicroOrm();
                List<EventModel> eventList = mOrm.listFromCursor(cursor, EventModel.class);
                mEventList.addAll(eventList);
                setDateRange(mEventList);
                setFriends(mEventList);
                sortEvents();
                mProgressBar.setVisibility(View.GONE);
                mAdapter.setEventList(mEventList);
                if(mDataListener != null)
                    mDataListener.onEventsDataLoaded();
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        //Log.d(TAG, "onLoaderReset: " + loader.getId());
//
        switch (loader.getId()) {
            case EVENT_INFO_LOADER_ID:
                mAdapter.setEventList(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    private void setDateRange(ArrayList<EventModel> eventModels){
        LocalDataFetcher localDataFetcher = new LocalDataFetcher(getActivity().getContentResolver(), getContext());
        for(EventModel eventModel : eventModels){
            eventModel.setDataRangeList(localDataFetcher.getEventDatesDataFromDB(eventModel.getEntryId(), true));
        }
    }
    private void setFriends(ArrayList<EventModel> eventModels){
        LocalDataFetcher localDataFetcher = new LocalDataFetcher(getActivity().getContentResolver(), getContext());
        for(EventModel eventModel : eventModels){
            eventModel.setFriendList(localDataFetcher.getEventFriendDataFromDB(eventModel.getEntryId()));
        }
    }
    private void sortEvents(){
        if(mEventList == null)
            return;
        for(int i = 0; i < mEventList.size() - 1; i++ ){
            //костыльное решение
            // синхронизация не проходит, когда вызывается метод
            if(mEventList.get(i).getActialDate() == null)
                return;
            long min = mEventList.get(i).getActialDate().getTime();
            int min_ind = i;
            for(int j = i+1; j < mEventList.size(); j++){
                if(mEventList.get(j).getActialDate().getTime() < min){
                    min = mEventList.get(j).getActialDate().getTime();
                    min_ind = j;
                }
            }
            if(min_ind == i)
                continue;
            EventModel temp = mEventList.get(i);
            mEventList.set(i, mEventList.get(min_ind));
            mEventList.set(min_ind, temp);
        }
    }

    private class OrganizationAsyncLoader extends AsyncTask<Void, Void, DataModel> {
        Context mContext;

        public OrganizationAsyncLoader(Context context) {
            this.mContext = context;
        }

        @Override
        protected DataModel doInBackground(Void... params) {
            Account account = EvendateSyncAdapter.getSyncAccount(getContext());
            String token = null;
            try{
                token = AccountManager.get(getContext()).blockingGetAuthToken(account, mContext.getString(R.string.account_type), false);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(token == null)
                return null;
            EvendateService evendateService = EvendateApiFactory.getEvendateService();
            return ServerDataFetcher.getOrganizationWithEventsData(evendateService, token, organizationId);
        }

        @Override
        protected void onPostExecute(DataModel dataModel) {
            mProgressBar.setVisibility(View.GONE);
            mEventList = ((OrganizationModelWithEvents) dataModel).getEvents();
            mAdapter.setEventList(mEventList);
            if(mDataListener != null)
                mDataListener.onEventsDataLoaded();
        }
    }
    public void onUnsubscripted(){
        if(!EvendateSyncAdapter.checkInternetConnection(getContext())){
            Toast.makeText(getContext(), R.string.subscription_fail_cause_network, Toast.LENGTH_SHORT).show();
            return;
        }
        this.type = TypeFormat.organization.nativeInt;
        OrganizationAsyncLoader organizationAsyncLoader = new OrganizationAsyncLoader(getContext());
        organizationAsyncLoader.execute();
    }
    public void onSubscribed(){
        if(mAdapter.getEventList() == null)
            return;
        ContentResolver contentResolver = getContext().getContentResolver();
        Uri ContentUri = EventAdapter.mUri;
        ArrayList<ContentProviderOperation> batch = new ArrayList<>();


        for (DataModel e : mAdapter.getEventList()) {
            Log.i(LOG_TAG, "Scheduling insert: entry_id=" + e.getEntryId());
            batch.add(e.getInsert(ContentUri));
        }
        try {
            contentResolver.applyBatch(EvendateContract.CONTENT_AUTHORITY, batch);
            this.type = TypeFormat.organizationSubscribed.nativeInt;
        }catch (Exception e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
            return;
        }
        contentResolver.notifyChange(
                ContentUri, // URI where data was modified
                null,                           // No local observer
                false);                         // IMPORTANT: Do not sync to network
        // This sample doesn't support uploads, but if *your* code does, make sure you set
        // syncToNetwork=false in the line above to prevent duplicate syncs.
        Log.i(LOG_TAG, "Batch update done");
        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(EVENT_INFO_LOADER_ID, null, this);
    }

    /**
     * fix cause bug in ChildFragmentManager
     * http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
     */
    @Override
    public void onDetach() {
        super.onDetach();
        Log.i(LOG_TAG, "onDetach");

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            childFragmentManager.setAccessible(true);
            childFragmentManager.set(this, null);

        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<EventModel> getEventList() {
        return mEventList;
    }

    interface OnEventsDataLoadedListener{
        void onEventsDataLoaded();
    }
}