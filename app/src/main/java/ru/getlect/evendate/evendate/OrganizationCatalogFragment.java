package ru.getlect.evendate.evendate;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * A fragment representing a list of Items.
 * <p/>
 * <p/>
 * interface.
 */
public class OrganizationCatalogFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private String LOG_TAG = OrganizationCatalogFragment.class.getSimpleName();
    private android.support.v7.widget.RecyclerView mRecyclerView;
    private final static int ORGANIZATION_INFO_LOADER_ID = 0;
    private OrganizationCatalogAdapter mAdapter;
    private Uri mUri = EvendateContract.OrganizationEntry.CONTENT_URI;

    // TODO: Rename and change types of parameters
    //public static OrganizationCatalogFragment newInstance() {
    //    OrganizationCatalogFragment fragment = new OrganizationCatalogFragment();
    //    Bundle args = new Bundle();
    //    fragment.setArguments(args);
    //    return fragment;
    //}

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrganizationCatalogFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization_catalog, container, false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);

        mAdapter = new OrganizationCatalogAdapter(getActivity());
        mRecyclerView.setAdapter(mAdapter);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(ORGANIZATION_INFO_LOADER_ID, null, this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        //Log.d(TAG, "onCreateLoader: " + id);
//
        switch (id) {
            case ORGANIZATION_INFO_LOADER_ID:
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        new String[] {
                                EvendateContract.OrganizationEntry._ID,
                                EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
                                EvendateContract.OrganizationEntry.COLUMN_NAME,
                                EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION,
                                EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID
                        },
                        null,
                        null,
                        null
                );
            default:
                throw new IllegalArgumentException("Unknown loader id: " + id);
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor cursor) {
        Log.d(LOG_TAG, "onLoadFinished: " + loader.getId());

        switch (loader.getId()) {
            case ORGANIZATION_INFO_LOADER_ID:
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
            case ORGANIZATION_INFO_LOADER_ID:
                mAdapter.setCursor(null);
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }
    public class OrganizationCatalogAdapter extends RecyclerView.Adapter<OrganizationCatalogAdapter.ViewHolder>{

        Context mContext;
        Cursor mCursor;

        public OrganizationCatalogAdapter(Context context){
            this.mContext = context;
        }

        public void setCursor(Cursor cursor) {
            this.mCursor = cursor;
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.organization_catalog_item, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            if (mCursor != null) {
                mCursor.moveToPosition(position);
                holder.id = mCursor.getInt(mCursor.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID));
                holder.mTitle.setText(mCursor.getString(mCursor.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_NAME)));
                //holder.mSubTitle.setText(mCursor.getString(mCursor.getColumnIndex(EvendateContract.OrganizationModel.COLUMN_DESCRIPTION)));
                ContentResolver contentResolver = getActivity().getContentResolver();
                holder.mImageView.setImageBitmap(null);
                try {
                    final ParcelFileDescriptor fileDescriptor = contentResolver
                            .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                            .appendPath("images").appendPath("organizations")
                                            .appendPath(mCursor.getString(
                                                            mCursor.getColumnIndex(EvendateContract.OrganizationEntry
                                                                    .COLUMN_ORGANIZATION_ID))
                                            ).build(), "r"
                            );
                    if(fileDescriptor == null)
                        //заглушка на случай отсутствия картинки
                        holder.mImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
                    else {
                        ImageLoadingTask imageLoadingTask = new ImageLoadingTask(holder.mImageView);
                        imageLoadingTask.execute(fileDescriptor);
                    }
                }catch (IOException e){
                    e.printStackTrace();
                }
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
            public ImageView mImageView;
            public long id;

            public ViewHolder(View itemView){
                super(itemView);
                mSubTitle = (TextView)itemView.findViewById(R.id.item_subtitle);
                cardView = (android.support.v7.widget.CardView)itemView;
                mTitle = (TextView)itemView.findViewById(R.id.item_title);
                mImageView = (ImageView)itemView.findViewById(R.id.item_background);
                this.id = (int)this.getItemId();
                itemView.setOnClickListener(this);
            }

            @Override
            public void onClick(View v) {
                if(v instanceof CardView){
                    Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
                    intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                    getActivity().startActivity(intent);
                }
            }

        }
    }
}
