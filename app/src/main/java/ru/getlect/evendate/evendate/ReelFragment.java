package ru.getlect.evendate.evendate;

/**
 * Created by Dmitry on 23.09.2015.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.ParseException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.FileObserver;
import android.os.ParcelFileDescriptor;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.chalup.microorm.MicroOrm;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateApiFactory;
import ru.getlect.evendate.evendate.sync.EvendateService;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.ImageLoaderTask;
import ru.getlect.evendate.evendate.sync.LocalDataFetcher;
import ru.getlect.evendate.evendate.sync.ServerDataFetcher;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModelWithEvents;
import ru.getlect.evendate.evendate.utils.Utils;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReelFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private String LOG_TAG = ReelFragment.class.getSimpleName();

    private android.support.v7.widget.RecyclerView mRecyclerView;

    private final static int EVENT_INFO_LOADER_ID = 0;
    private RVAdapter mAdapter;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final String ORGANIZATION_ID = "organization_id";

    public static final String IS_SUBSCRIBED = "is_subscribed";

    private int organizationId;
    /**
     * argument represent that fragment should get only favorite events
     */
    public static final String TYPE = "feed";

    private int type = 0;
    public enum TypeFormat {
        feed                (0),
        favorites           (1),
        organization        (2),
        organizationSubscribed  (3);

        TypeFormat(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }


    private Uri mUri = EvendateContract.EventEntry.CONTENT_URI;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_reel, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        Bundle args = getArguments();
        if(args != null) {
            type = args.getInt(TYPE);
            if(type == TypeFormat.organization.nativeInt){
                organizationId = args.getInt(ORGANIZATION_ID);
                if(!checkInternetConnection()){
                    Toast.makeText(getContext(), R.string.subscription_fail_cause_network, Toast.LENGTH_LONG).show();
                }else{
                    OrganizationAsyncLoader organizationAsyncLoader = new OrganizationAsyncLoader(getContext());
                    organizationAsyncLoader.execute();
                }
            }
            if(type == TypeFormat.organizationSubscribed.nativeInt){
                organizationId = args.getInt(ORGANIZATION_ID);
            }
        }

        mAdapter = new RVAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        if(type != TypeFormat.organization.nativeInt){
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(EVENT_INFO_LOADER_ID, null, this);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
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
        //else{
        //    selection = EvendateContract.EventDateEntry.COLUMN_DATE + "> date('now')";
        //}
        switch (id) {
            case EVENT_INFO_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        mUri,
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
                ArrayList<EventModel> eventArrayList = new ArrayList<>();
                MicroOrm mOrm = new MicroOrm();
                List<EventModel> eventList = mOrm.listFromCursor(cursor, EventModel.class);
                eventArrayList.addAll(eventList);
                setDateRange(eventArrayList);
                mAdapter.setEventList(eventArrayList);
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
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder>{

        Context mContext;
        //Cursor mCursor;
        private ArrayList<EventModel> mEventList;

        public RVAdapter(Context context){
            this.mContext = context;
        }

        public void setEventList(ArrayList<EventModel> eventList){
            mEventList = eventList;
            notifyDataSetChanged();
        }

        //public void setCursor(Cursor cursor) {
        //    //this.mCursor = cursor;
        //    notifyDataSetChanged();
        //}

        public ArrayList<EventModel> getEventList() {
            return mEventList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutItemId;
            if(type == TypeFormat.organization.nativeInt)
                layoutItemId = R.layout.reel_item;
            else if(type == TypeFormat.favorites.nativeInt){
                layoutItemId = R.layout.reel_favorite_item;
            }
            else{
                layoutItemId = R.layout.reel_item;
            }
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutItemId, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(mEventList == null)
                return;
            EventModel eventEntry = mEventList.get(position);
            holder.id = eventEntry.getEntryId();
            holder.mTitleTextView.setText(eventEntry.getTitle());
            holder.mOrganizationTextView.setText(eventEntry.getOrganizationShortName());
            holder.mEventImageView.setImageBitmap(null);

            String time;
            if(eventEntry.isFullDay())
                time = getResources().getString(R.string.event_all_day);
            else{
                //cut off seconds
                //TODO temporary
                time = "";
                if(eventEntry.getBeginTime() != null && eventEntry.getEndTime() != null)
                    time = eventEntry.getBeginTime().substring(0, 5) + " - " + eventEntry.getEndTime().substring(0, 5);
            }
            Date date = eventEntry.getActialDate();
            DateFormat[] formats = new DateFormat[] {
                    new SimpleDateFormat("dd", Locale.getDefault()),
                    new SimpleDateFormat("MMMM", Locale.getDefault()),
            };
            if(date != null){
                String day = formats[0].format(date);
                if(day.substring(0, 1).equals("0"))
                    day = day.substring(1);
                String dateString = day + " " + formats[1].format(date) + " " + time;
                holder.mDateTextView.setText(dateString);
            }


            if(type == TypeFormat.organization.nativeInt){
                if(eventEntry.getImageHorizontalUrl() == null)
                    holder.mEventImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
                else{
                    ImageLoaderTask imageLoader = new ImageLoaderTask(holder.mEventImageView);
                    imageLoader.execute(eventEntry.getImageHorizontalUrl());
                }

                if(holder.mImageObserver == null){
                    holder.mImageObserver = new ImageObserver(getContext(), holder.mEventImageView,
                            getActivity().getExternalCacheDir().toString() + "/" +
                                    EvendateContract.PATH_EVENT_IMAGES + "/" + holder.id + ".jpg", holder.id);
                    holder.mImageObserver.startWatching();
                }
            }
            else{
                try {
                    final ParcelFileDescriptor fileDescriptor = getActivity().getContentResolver()
                            .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                            .appendPath("images").appendPath("events")
                                            .appendPath(String.valueOf(eventEntry.getEntryId())
                                            ).build(), "r"
                            );
                    if(fileDescriptor == null)
                        //заглушка на случай отсутствия картинки
                        holder.mEventImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
                    else {
                        ImageLoadingTask imageLoadingTask = new ImageLoadingTask(holder.mEventImageView);
                        imageLoadingTask.execute(fileDescriptor);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public int getItemCount() {
            if(mEventList == null)
                return 0;
            return mEventList.size();
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            if(holder.mFavoriteIndicator != null)
                holder.mFavoriteIndicator.setVisibility(View.GONE);
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public android.support.v7.widget.CardView cardView;
            public ImageView mEventImageView;
            public TextView mTitleTextView;
            public TextView mDateTextView;
            public TextView mOrganizationTextView;
            public View mFavoriteIndicator;
            public int id;

            ImageObserver mImageObserver;

            public ViewHolder(View itemView){
                super(itemView);
                cardView = (android.support.v7.widget.CardView)itemView;
                mEventImageView = (ImageView)itemView.findViewById(R.id.event_item_image);
                mTitleTextView = (TextView)itemView.findViewById(R.id.event_item_title);
                mDateTextView = (TextView)itemView.findViewById(R.id.event_item_date);
                mOrganizationTextView = (TextView)itemView.findViewById(R.id.event_item_organization);
                mFavoriteIndicator = itemView.findViewById(R.id.event_item_favorite_indicator);
                cardView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(v instanceof CardView){
                    Intent intent = new Intent(getContext(), EventDetailActivity.class);
                    intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                    if(type == TypeFormat.organization.nativeInt)
                        intent.putExtra(EventDetailActivity.IS_LOCAL, true);
                    getActivity().startActivity(intent);
                }
            }

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
            mAdapter.setEventList(((OrganizationModelWithEvents) dataModel).getEvents());
        }
    }
    public void onUnsubscripted(){
        if(!checkInternetConnection()){
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
        Uri ContentUri = mUri;
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
    private boolean checkInternetConnection(){
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean result = true;
        if(activeNetwork == null)
            result = false;
        else{
            boolean isConnected = activeNetwork.isConnected();
            if (!isConnected){
                result = false;
            }
        }
        return result;
    }
    class ImageObserver extends FileObserver {
        Context mContext;
        ImageView mImageView;
        int eventId;
        public ImageObserver(Context context, ImageView imageView, String path, int eventId) {
            super(path);
            mImageView = imageView;
            this.eventId = eventId;
            mContext = context;
        }
        @Override
        public void onEvent(int event, String path) {
            if(event == FileObserver.CLOSE_WRITE){
                mImageView.setImageBitmap(null);
                try {
                    final ParcelFileDescriptor fileDescriptor = mContext.getContentResolver()
                            .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                            .appendPath("images").appendPath("events")
                                            .appendPath(String.valueOf(eventId)
                                            ).build(), "r"
                            );
                    if(fileDescriptor == null)
                        //заглушка на случай отсутствия картинки
                        mImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.butterfly));
                    else {
                        ImageLoadingTask imageLoadingTask = new ImageLoadingTask(mImageView);
                        imageLoadingTask.execute(fileDescriptor);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}