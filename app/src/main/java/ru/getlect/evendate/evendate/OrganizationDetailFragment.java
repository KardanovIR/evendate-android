package ru.getlect.evendate.evendate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.chalup.microorm.MicroOrm;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import ru.getlect.evendate.evendate.authorization.AuthActivity;
import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class OrganizationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener{
    private final String LOG_TAG = "OrganizationFragment";

    OrganizationModel mOrganizationModel;

    private int organizationId = -1;

    private final int LOADER_ORGANIZATION_ID = 0;

    public static final String URI = "uri";
    private Uri mUri;

    private CoordinatorLayout mCoordinatorLayout;
    private ImageView mOrganizationImageView;
    private ImageView mOrganizationIconView;
    private ParcelFileDescriptor mParcelFileDescriptor;


    private TextView mEventCountView;
    private TextView mSubscriptionCountView;
    private TextView mFriendCountView;
    private TextView mFavoriteEventCountTextView;
    private TextView mOrganizationNameTextView;

    private FloatingActionButton mFAB;
    public OrganizationDetailFragment() {
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
            organizationId = Integer.parseInt(mUri.getLastPathSegment());
        }

        mCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.main_content);

        mEventCountView = (TextView)rootView.findViewById(R.id.organization_event_count);
        mSubscriptionCountView = (TextView)rootView.findViewById(R.id.organization_subscription_count);
        mFriendCountView = (TextView)rootView.findViewById(R.id.organization_friend_count);
        mFavoriteEventCountTextView = (TextView)rootView.findViewById(R.id.organization_favorite_event_count);
        mOrganizationNameTextView = (TextView)rootView.findViewById(R.id.organization_name);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.organization_icon);
        mOrganizationImageView = (ImageView)rootView.findViewById(R.id.organization_image);


        getActivity().getSupportLoaderManager().initLoader(LOADER_ORGANIZATION_ID, null,
                (LoaderManager.LoaderCallbacks) this);


        mFAB = (FloatingActionButton) rootView.findViewById((R.id.fab));

        // Set initial state based on pref

        // To over-ride the color of the FAB other then the theme color
        //fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.grey_300)));

        mFAB.setOnClickListener(this);
        EventsObserver eventsObserver = new EventsObserver(getActivity());
        getActivity().getContentResolver().registerContentObserver(EvendateContract.EventEntry.CONTENT_URI, false, eventsObserver);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id){
            case LOADER_ORGANIZATION_ID:
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        null,
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
            case LOADER_ORGANIZATION_ID:
                MicroOrm mOrm = new MicroOrm();
                data.moveToFirst();
                mOrganizationModel = mOrm.fromCursor(data, OrganizationModel.class);
                organizationId = mOrganizationModel.getEntryId();
                setOrganizationInfo();
                setFabIcon();
                data.close();
                addReel();
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        switch (loader.getId()){
            case LOADER_ORGANIZATION_ID:
                break;
            default:
                throw new IllegalArgumentException("Unknown loader id: " + loader.getId());
        }
    }

    private void setOrganizationInfo(){
        mOrganizationNameTextView.setText(mOrganizationModel.getName());
        //mEventCountView.setText(data.getString(COLUMN_ORGANIZATION_NAME));
        mSubscriptionCountView.setText(String.valueOf(mOrganizationModel.getSubscribedCount()));
        //mFriendCountView.setText();
        //mFavoriteEventCountTextView.setText(data.getString(COLUMN_LOCATION_TEXT));

        try {
            mParcelFileDescriptor = getActivity().getContentResolver()
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                            .appendPath("images").appendPath("organizations").appendPath(String.valueOf(mOrganizationModel.getEntryId())).build(), "r");
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
                            .appendPath(String.valueOf(mOrganizationModel.getEntryId())).build(), "r");
            if(fileDescriptor == null)
                mOrganizationIconView.setImageDrawable(getResources().getDrawable(R.drawable.place));
            else{
                mOrganizationIconView.setImageBitmap(BitmapFactory.decodeFileDescriptor(fileDescriptor.getFileDescriptor()));
                fileDescriptor.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public boolean Subscript(int id, String type){
        String url = "http://evendate.ru/api/subscriptions/";
        if(type.equals("POST")){
            url += "?organization_id=" + id;
        }
        else{
            url += String.valueOf(mOrganizationModel.getSubscriptionId());
        }
        if (Log.isLoggable(LOG_TAG, Log.INFO)) {
            Log.i(LOG_TAG, "Requesting service: " + url);
        }

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(url);
            urlConnection = (HttpURLConnection) urlToRequest.openConnection();
            //urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            //urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            urlConnection.setDoOutput(true);
            urlConnection.setRequestMethod(type);
            //urlConnection.setFixedLengthStreamingMode(
            //        postParameters.getBytes().length);
            urlConnection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            AccountManager accountManager = AccountManager.get(getContext());
            Account[] accounts = accountManager.getAccountsByType(getContext().getString(R.string.account_type));
            if (accounts.length == 0) {
                Log.e("SYNC", "No Accounts");
                Intent dialogIntent = new Intent(getContext(), AuthActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(dialogIntent);
                return false;
            }
            Account account = accounts[0];
            String token = null;
            try{
                token = accountManager.blockingGetAuthToken(account, getContext().getString(R.string.account_type), false);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(token == null)
                return false;
            urlConnection.setRequestProperty("Authorization", token);

            urlConnection.connect();
            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                return false;
            }
            ReelFragment reelFragment =  (ReelFragment) getChildFragmentManager().findFragmentById(R.id.organization_container);

            //if(reelFragment != null){
            //    reelFragment.subscribed();
            //}


        } catch (MalformedURLException e) {
            // handle invalid URL
        } catch (SocketTimeoutException e) {
            // hadle timeout
        } catch (IOException e) {
            // handle I/0
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return true;
    }

    private void setFabIcon(){
        if (mOrganizationModel.isSubscribed()) {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_favorite_on));
        } else {
            mFAB.setImageDrawable(this.getResources().getDrawable(R.mipmap.ic_favorite_off));
        }
    }
    private class SubscriptAsyncTask extends AsyncTask<Void, Void, Boolean>{

        @Override
        protected Boolean doInBackground(Void... params) {
            ConnectivityManager cm =
                    (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork.isConnectedOrConnecting();
            //Send the user a message to let them know change was made
            if (!isConnected){
                return false;
            }
            boolean isConfirm;
            if(!mOrganizationModel.isSubscribed()){
                isConfirm = Subscript(organizationId, "POST");
            }
            else{
                isConfirm = Subscript(mOrganizationModel.getSubscriptionId(), "DELETE");
            }
            return isConfirm;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result){
                Snackbar.make(mCoordinatorLayout, R.string.subscription_fail_cause_network, Snackbar.LENGTH_LONG).show();
            }
            else{
                mOrganizationModel.setIsSubscribed(!mOrganizationModel.isSubscribed());
                setFabIcon();
                Snackbar.make(mCoordinatorLayout, R.string.subscription_confirm, Snackbar.LENGTH_LONG).show();
                ContentResolver contentResolver = getActivity().getContentResolver();
                contentResolver.update(mUri, mOrganizationModel.getContentValues(), null, null);
                EvendateSyncAdapter.syncImmediately(getContext());
            }
        }
    }
    public void onClick(View v) {
        if(v == mFAB) {
            SubscriptAsyncTask subscriptAsyncTask = new SubscriptAsyncTask();
            subscriptAsyncTask.execute();
        }
    }
    /**
     * observe updates of events because of subscription changes
     */
    class EventsObserver extends ContentObserver {
        Activity mActivity;
        public EventsObserver(Activity activity){
            super(new Handler());
            mActivity = activity;
        }
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if(mActivity != null)
                addReel();
        }
    }
    public void addReel(){
        Bundle reelArgs = new Bundle();
        if(mOrganizationModel.isSubscribed())
            reelArgs.putInt(ReelFragment.TYPE, ReelFragment.TypeFormat.organizationSubscribed.nativeInt);
        else
            reelArgs.putInt(ReelFragment.TYPE, ReelFragment.TypeFormat.organization.nativeInt);
        reelArgs.putInt(ReelFragment.ORGANIZATION_ID, organizationId);
        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
        Fragment fragment = new ReelFragment();
        fragment.setArguments(reelArgs);
        fragmentTransaction.replace(R.id.organization_container, fragment);

        fragmentTransaction.commit();
    }

    /**
     * fix cause bug in ChildFragmentManager
     * http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
     */
    @Override
     public void onDetach() {
        super.onDetach();

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
}
