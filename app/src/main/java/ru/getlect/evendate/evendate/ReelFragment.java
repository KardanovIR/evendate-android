package ru.getlect.evendate.evendate;

/**
 * Created by Dmitry on 23.09.2015.
 */

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.getlect.evendate.evendate.data.EvendateContract;

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
        switch (id) {
            case EVENT_INFO_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        new String[] {
                                EvendateContract.EventEntry._ID,
                                EvendateContract.EventEntry.COLUMN_TITLE,
                                EvendateContract.EventEntry.COLUMN_DESCRIPTION,
                        },
                        null,
                        null,
                        null
                );
//
        //    case CAPITALS_LOADER_ID:
        //        return new CursorLoader(
        //                mActivity,
        //                CitiesContract.Cities.CAPITALS_CONTENT_URI,
        //                new String[]{
        //                        CitiesContract.Cities._ID,
        //                        CitiesContract.Cities.NAME
        //                },
        //                null,
        //                null,
        //                CitiesContract.Cities.NAME + " ASC"
        //        );
//
        //    case SELECTED_CITY_LOADER_ID:
        //        return new CursorLoader(
        //                mActivity,
        //                ContentUris.withAppendedId(CitiesContract.Cities.CONTENT_URI, args.getLong(CITY_ID_KEY)),
        //                null,
        //                null,
        //                null,
        //                null
        //        );
//
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
//
        //    case CAPITALS_LOADER_ID:
        //        mCapitalsAdapter.setCursor(cursor);
        //        break;
//
        //    case SELECTED_CITY_LOADER_ID:
        //        if (cursor != null) {
        //            mEditingCityId = cursor.getLong(cursor.getColumnIndex(CitiesContract.Cities._ID));
        //            mCityIdTextView.setText(getString(R.string.city_id_placeholder, mEditingCityId));
        //            mCityNameEditText.setText(cursor.getString(cursor.getColumnIndex(CitiesContract.Cities.NAME)));
        //            mCapitalCheckbox.setChecked(cursor.getInt(cursor.getColumnIndex(CitiesContract.Cities.CAPITAL)) == 1);
        //        } else {
        //            resetCityEditingForm();
        //        }
        //        break;
//
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
//
        //    case CAPITALS_LOADER_ID:
        //        mCapitalsAdapter.setCursor(null);
        //        break;
//
        //    case SELECTED_CITY_LOADER_ID:
        //        resetCityEditingForm();
        //        break;
//
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }
    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder>{

        Context mContext;
        Cursor mCursor;

        public RVAdapter(Context context){
            this.mContext = context;
        }

        public void setCursor(Cursor cursor) {
            this.mCursor = cursor;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.reel_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mCursor != null) {
                mCursor.moveToPosition(position);
                holder.id = mCursor.getInt(mCursor.getColumnIndex(EvendateContract.EventEntry._ID));
                holder.mTitle.setText(mCursor.getString(mCursor.getColumnIndex(EvendateContract.EventEntry.COLUMN_TITLE)));
                holder.mSubTitle.setText(mCursor.getString(mCursor.getColumnIndex(EvendateContract.EventEntry.COLUMN_DESCRIPTION)));
            }
        }

        @Override
        public int getItemCount() {
            if (mCursor == null) {
                return 0;
            } else {
                return mCursor.getCount();
            }
        }


        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public android.support.v7.widget.CardView cardView;
            public TextView mTitle;
            public TextView mSubTitle;
            public long id;

            public ViewHolder(View itemView){
                super(itemView);
                mSubTitle = (TextView)itemView.findViewById(R.id.item_subtitle);
                cardView = (android.support.v7.widget.CardView)itemView;
                mTitle = (TextView)itemView.findViewById(R.id.item_title);
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
}