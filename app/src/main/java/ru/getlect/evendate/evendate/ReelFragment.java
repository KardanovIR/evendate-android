package ru.getlect.evendate.evendate;

/**
 * Created by Dmitry on 23.09.2015.
 */

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class ReelFragment extends Fragment {
    private android.support.v7.widget.RecyclerView mRecyclerView;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

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

        final String[] PROJECTION = new String[] {
                EvendateContract.EventEntry._ID,
                EvendateContract.EventEntry.COLUMN_TITLE,
                EvendateContract.EventEntry.COLUMN_DESCRIPTION,
        };
        final Uri uri = EvendateContract.EventEntry.CONTENT_URI;
        Cursor c = getActivity().getContentResolver().query(uri, PROJECTION, null, null, null);
        String[] from = new String[] { EvendateContract.EventEntry.COLUMN_TITLE,
                EvendateContract.EventEntry.COLUMN_DESCRIPTION };
        int[] to = new int[] { R.id.item_title, R.id.item_subtitle };
        RVAdapter adapter = new RVAdapter(c, getActivity());
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //mRecyclerView.
        return rootView;
    }

    public class RVAdapter extends RecyclerView.Adapter<RVAdapter.ViewHolder> implements ReelCardClickListener{

        Context mContext;
        Cursor mCursor;
        CursorAdapter mCursorAdapter;

        public RVAdapter(Cursor c, Context context){
            this.mCursor = c;
            this.mContext = context;
            mCursorAdapter = new CursorAdapter(mContext, mCursor, 0) {
                @Override
                public View newView(Context context, Cursor cursor, ViewGroup parent) {
                    return LayoutInflater.from(parent.getContext()).inflate(R.layout.reel_list_item, parent, false);
                }

                @Override
                public void bindView(View view, Context context, Cursor cursor) {
                    TextView title = (TextView)view.findViewById(R.id.item_title);
                    TextView subTitle = (TextView)view.findViewById(R.id.item_subtitle);

                    title.setText(cursor.getString(cursor.getColumnIndex(EvendateContract.EventEntry.COLUMN_TITLE)));
                    subTitle.setText(cursor.getString(cursor.getColumnIndex(EvendateContract.EventEntry.COLUMN_DESCRIPTION)));
                }

            };
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
            return new ViewHolder(v, this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            mCursorAdapter.getCursor().moveToPosition(position);
            mCursorAdapter.bindView(holder.cardView, mContext, mCursorAdapter.getCursor());
        }

        @Override
        public int getItemCount() {
            return mCursorAdapter.getCount();
        }

        @Override
        public void onItem(CardView cardView) {
//            cardView.getId();
//            int itemPosition = get.getChildPosition(v);
//            Intent intent = new Intent(getContext(), DetailActivity.class);
//            intent.setData();
        }

        public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            public android.support.v7.widget.CardView cardView;
            public TextView mTitle;
            public TextView mSubTitle;
            private ReelCardClickListener mReelCardClickListener;
            public final int id;
            public ViewHolder(View itemView, ReelCardClickListener reelCardClickListener){
                super(itemView);
                mReelCardClickListener = reelCardClickListener;
                mSubTitle = (TextView)itemView.findViewById(R.id.item_subtitle);
                cardView = (android.support.v7.widget.CardView)itemView;
                mTitle = (TextView)itemView.findViewById(R.id.item_title);
                this.id = (int)this.getItemId();
            }

            @Override
            public void onClick(View v) {
                if(v instanceof CardView)
                    mReelCardClickListener.onItem((CardView)v);
            }

        }
    }

    public interface ReelCardClickListener {
        public void onItem(android.support.v7.widget.CardView cardView);
    }
}