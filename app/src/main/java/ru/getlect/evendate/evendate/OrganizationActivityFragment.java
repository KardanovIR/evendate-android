package ru.getlect.evendate.evendate;

import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class OrganizationActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    private final int ORGANIZATION_ID = 0;

    public static final String URI = "uri";
    private Uri mUri;

    private ImageView mOrganizationImageView;
    private ImageView mOrganizationIconView;
    private ParcelFileDescriptor mParcelFileDescriptor;


    private TextView mEventCountView;
    private TextView mSubscriptionCountView;
    private TextView mFriendCountView;
    private TextView mFavoriteEventCountTextView;
    private TextView mOrganizationNameTextView;

    public OrganizationActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization, container, false);

        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar));
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle args = getArguments();
        if(args != null){
            mUri = Uri.parse(args.getString(URI));
        }
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.organization_container, new ReelFragment());
        fragmentTransaction.commit();

        mEventCountView = (TextView)rootView.findViewById(R.id.organization_event_count);
        mSubscriptionCountView = (TextView)rootView.findViewById(R.id.organization_subscription_count);
        mFriendCountView = (TextView)rootView.findViewById(R.id.organization_friend_count);
        mFavoriteEventCountTextView = (TextView)rootView.findViewById(R.id.organization_favorite_event_count);
        mOrganizationNameTextView = (TextView)rootView.findViewById(R.id.organization_name);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.organization_icon);
        mOrganizationImageView = (ImageView)rootView.findViewById(R.id.organization_image);


        getActivity().getSupportLoaderManager().initLoader(ORGANIZATION_ID, null,
                (LoaderManager.LoaderCallbacks) this);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case ORGANIZATION_ID:
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        new String[] {
                                EvendateContract.OrganizationEntry.COLUMN_NAME,
                                EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME,
                                EvendateContract.OrganizationEntry.COLUMN_IMG_URL,
                                EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION,
                                EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT,
                                EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID,
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
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()){
            case ORGANIZATION_ID:
                setOrganizationInfo(data);
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case ORGANIZATION_ID:
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    private void setOrganizationInfo(Cursor data){
        final int COLUMN_ORGANIZATION_ID = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_ORGANIZATION_ID);
        final int COLUMN_ORGANIZATION_NAME = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_NAME);
        final int COLUMN_ORGANIZATION_SHORT_NAME = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_SHORT_NAME);
        final int COLUMN_ORGANIZATION_IMG_URL = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_IMG_URL);
        final int COLUMN_ORGANIZATION_DESCRIPTION = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_DESCRIPTION);
        final int COLUMN_ORGANIZATION_SUBSCRIBED_COUNT = data.getColumnIndex(EvendateContract.OrganizationEntry.COLUMN_SUBSCRIBED_COUNT);
        data.moveToFirst();
        mOrganizationNameTextView.setText(data.getString(COLUMN_ORGANIZATION_DESCRIPTION));
        //mEventCountView.setText(data.getString(COLUMN_ORGANIZATION_NAME));
        mSubscriptionCountView.setText(data.getString(COLUMN_ORGANIZATION_SUBSCRIBED_COUNT));
        //mFriendCountView.setText(data.getString(COLUMN_TITLE));
        //mFavoriteEventCountTextView.setText(data.getString(COLUMN_LOCATION_TEXT));

        try {
            mParcelFileDescriptor = getActivity().getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("organizations").appendPath(data.getString(COLUMN_ORGANIZATION_ID)).build(), "r");
            if(mParcelFileDescriptor == null)
                //заглушка на случай отсутствия картинки
                mOrganizationImageView.setImageDrawable(getResources().getDrawable(R.drawable.butterfly));
            else {
                ImageLoadingTask imageLoadingTask = new ImageLoadingTask(mOrganizationImageView);
                imageLoadingTask.execute(mParcelFileDescriptor);
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        try{
            final ParcelFileDescriptor fileDescriptor = getActivity().getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("organizations").appendPath("logos")
                            .appendPath(data.getString(COLUMN_ORGANIZATION_ID)).build(), "r");
            if(fileDescriptor == null)
                mOrganizationIconView.setImageDrawable(getResources().getDrawable(R.drawable.place));
            else{
                mOrganizationIconView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor()));
                fileDescriptor.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        data.close();
    }
}
