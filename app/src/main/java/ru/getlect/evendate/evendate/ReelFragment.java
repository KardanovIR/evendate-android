package ru.getlect.evendate.evendate;

/**
 * Created by Dmitry on 23.09.2015.
 */

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import ru.getlect.evendate.evendate.authorization.AuthActivity;
import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateApiFactory;
import ru.getlect.evendate.evendate.sync.EvendateService;
import ru.getlect.evendate.evendate.sync.ImageLoaderTask;
import ru.getlect.evendate.evendate.sync.ServerDataFetcher;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntryWithEvents;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReelFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
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
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ReelFragment newInstance(int sectionNumber) {
        ReelFragment fragment = new ReelFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ReelFragment() {
    }

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
                OrganizationAsyncLoader organizationAsyncLoader = new OrganizationAsyncLoader();
                organizationAsyncLoader.execute();
            }
            if(type == TypeFormat.organizationSubscribed.nativeInt){
                organizationId = args.getInt(ORGANIZATION_ID);
            }
        }

        mAdapter = new RVAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(EVENT_INFO_LOADER_ID, null, this);

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
        else if(type == TypeFormat.organizationSubscribed.nativeInt){
            selection = EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID + "=" + organizationId;
        }
        switch (id) {
            case EVENT_INFO_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        new String[] {
                                EvendateContract.EventEntry._ID,
                                EvendateContract.EventEntry.COLUMN_EVENT_ID,
                                EvendateContract.EventEntry.COLUMN_TITLE,
                                EvendateContract.EventEntry.COLUMN_DESCRIPTION,
                                EvendateContract.EventEntry.COLUMN_IS_FAVORITE,
                        },
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
                mAdapter.setCursor(cursor);
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
                mAdapter.setCursor(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder>{

        Context mContext;
        Cursor mCursor;
        private ArrayList<EventEntry> mEventList;

        public RVAdapter(Context context){
            this.mContext = context;
        }

        public void setEventList(ArrayList<EventEntry> eventList){
            mEventList = eventList;
            notifyDataSetChanged();
        }

        public void setCursor(Cursor cursor) {
            this.mCursor = cursor;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            int layoutItemId;
            if(type == TypeFormat.organization.nativeInt)
                layoutItemId = R.layout.reel_list_item;
            else if(type == TypeFormat.favorites.nativeInt){
                layoutItemId = R.layout.reel_favorite_item;
            }
            else{
                layoutItemId = R.layout.reel_list_item;
            }
                return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutItemId, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if(type != TypeFormat.organization.nativeInt){
                if (mCursor != null) {
                    mCursor.moveToPosition(position);
                    holder.id = mCursor.getInt(mCursor.getColumnIndex(EvendateContract.EventEntry.COLUMN_EVENT_ID));

                    holder.mTitleTextView.setText(mCursor.getString(mCursor.getColumnIndex(EvendateContract.EventEntry.COLUMN_TITLE)));
                    ContentResolver contentResolver = getActivity().getContentResolver();
                    if(mCursor.getInt(mCursor.getColumnIndex(EvendateContract.EventEntry.COLUMN_IS_FAVORITE)) == 1
                            && type != TypeFormat.favorites.nativeInt){
                        holder.mFavoriteIndicator.setVisibility(View.VISIBLE);
                    }
                    holder.mEventImageView.setImageBitmap(null);
                    try {
                        final ParcelFileDescriptor fileDescriptor = contentResolver
                                .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                                .appendPath("images").appendPath("events")
                                                .appendPath(mCursor.getString(
                                                                mCursor.getColumnIndex(EvendateContract.EventEntry
                                                                        .COLUMN_EVENT_ID))
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
            else{
                EventEntry eventEntry = mEventList.get(position);
                holder.id = eventEntry.getEntryId();
                holder.mTitleTextView.setText(eventEntry.getTitle());
                holder.mEventImageView.setImageBitmap(null);
                if(eventEntry.getImageHorizontalUrl() == null)
                    holder.mEventImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
                else{
                    ImageLoaderTask imageLoader = new ImageLoaderTask(holder.mEventImageView);
                    imageLoader.execute(eventEntry.getImageHorizontalUrl());
                }
            }
        }

        @Override
        public int getItemCount() {
            if(type != TypeFormat.organization.nativeInt){
                if (mCursor == null) {
                    return 0;
                } else {
                    return mCursor.getCount();
                }
            }else {
                if(mEventList == null)
                    return 0;
                return mEventList.size();
            }
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public android.support.v7.widget.CardView cardView;
            public ImageView mEventImageView;
            public TextView mTitleTextView;
            public TextView mDateTextView;
            public TextView mOrganizationTextView;
            public View mFavoriteIndicator;
            public long id;

            public ViewHolder(View itemView){
                super(itemView);
                cardView = (android.support.v7.widget.CardView)itemView;
                mEventImageView = (ImageView)itemView.findViewById(R.id.event_item_image);
                mTitleTextView = (TextView)itemView.findViewById(R.id.event_item_title);
                mDateTextView = (TextView)itemView.findViewById(R.id.event_item_date);
                mOrganizationTextView = (TextView)itemView.findViewById(R.id.event_item_organization);
                mFavoriteIndicator = itemView.findViewById(R.id.event_item_favorite_indicator);

                this.id = (int)this.getItemId();
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(v instanceof CardView){
                    Intent intent = new Intent(getContext(), DetailActivity.class);
                    intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                    getActivity().startActivity(intent);
                }
            }

        }
    }

    private class OrganizationAsyncLoader extends AsyncTask<Void, Void, DataEntry> {
        @Override
        protected DataEntry doInBackground(Void... params) {
            AccountManager accountManager = AccountManager.get(getContext());
            Account[] accounts = accountManager.getAccountsByType(getContext().getString(R.string.account_type));
            if (accounts.length == 0) {
                Log.e("SYNC", "No Accounts");
                Intent dialogIntent = new Intent(getContext(), AuthActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(dialogIntent);
            }
            Account account = accounts[0];
            String token = null;
            try{
                token = accountManager.blockingGetAuthToken(account, getContext().getString(R.string.account_type), false);
            }catch (Exception e){
                e.printStackTrace();
            }
            EvendateService evendateService = EvendateApiFactory.getEvendateService();
            return ServerDataFetcher.getOrganizationWithEventsData(evendateService, token, organizationId);
        }

        @Override
        protected void onPostExecute(DataEntry dataEntry) {
            mAdapter.setEventList(((OrganizationEntryWithEvents)dataEntry).getEvents());
        }
    }
}