package ru.getlect.evendate.evendate;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import org.chalup.microorm.MicroOrm;

import java.io.IOException;
import java.lang.reflect.Field;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.EvendateApiFactory;
import ru.getlect.evendate.evendate.sync.EvendateService;
import ru.getlect.evendate.evendate.sync.EvendateSyncAdapter;
import ru.getlect.evendate.evendate.sync.ServerDataFetcher;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;

/**
 * A placeholder fragment containing a simple view.
 */
public class OrganizationDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener{
    private final String LOG_TAG = "OrganizationFragment";

    OrganizationModel mOrganizationModel;
    ReelFragment mReelFragment;

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

        //make status bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            // Set the status bar to dark-semi-transparentish
            getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


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
    public boolean Subscript(){
            Account account = EvendateSyncAdapter.getSyncAccount(getContext());
            String token = null;
            try{
                token = AccountManager.get(getContext()).blockingGetAuthToken(account, getContext().getString(R.string.account_type), false);
            }catch (Exception e){
                e.printStackTrace();
            }
            if(token == null)
                return false;

            EvendateService evendateService = EvendateApiFactory.getEvendateService();
            if(mOrganizationModel.isSubscribed()){
                if(ServerDataFetcher.organizationDeleteSubscription(evendateService, token, mOrganizationModel.getSubscriptionId()))
                    mOrganizationModel.setSubscriptionId(null);
            }
            else{
                OrganizationModel organizationModel = ServerDataFetcher.organizationPostSubscription(evendateService, token, organizationId);
                if(organizationModel == null)
                    return false;
                mOrganizationModel.setSubscriptionId(organizationModel.getSubscriptionId());
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
            if(activeNetwork == null)
                return false;
            boolean isConnected = activeNetwork.isConnected();
            if (!isConnected){
                return false;
            }
            return Subscript();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result){
                Snackbar.make(mCoordinatorLayout, R.string.subscription_fail_cause_network, Snackbar.LENGTH_LONG).show();
            }
            else{
                mOrganizationModel.setIsSubscribed(!mOrganizationModel.isSubscribed());
                int count_change = mOrganizationModel.isSubscribed() ? 1 : -1;
                mOrganizationModel.setSubscribedCount(mOrganizationModel.getSubscribedCount() + count_change);
                setOrganizationInfo();
                setFabIcon();
                Snackbar.make(mCoordinatorLayout, R.string.subscription_confirm, Snackbar.LENGTH_LONG).show();
                ContentResolver contentResolver = getActivity().getContentResolver();
                contentResolver.update(mUri, mOrganizationModel.getContentValues(), null, null);
                if(mOrganizationModel.isSubscribed())
                    mReelFragment.onSubscribed();
                else
                    mReelFragment.onUnsubscripted();
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
            //if(mActivity != null)
            //    addReel();
        }
    }
    public void addReel(){

        Bundle reelArgs = new Bundle();
        if(mOrganizationModel.isSubscribed()){
            reelArgs.putInt(ReelFragment.TYPE, ReelFragment.TypeFormat.organizationSubscribed.nativeInt);
        }
        else{
            reelArgs.putInt(ReelFragment.TYPE, ReelFragment.TypeFormat.organization.nativeInt);
            if(!checkInternetConnection()){
                Snackbar.make(mCoordinatorLayout, R.string.subscription_fail_cause_network, Snackbar.LENGTH_LONG).show();
                return;
            }
        }
        reelArgs.putInt(ReelFragment.ORGANIZATION_ID, organizationId);
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        if(fragmentManager != null){
            mReelFragment = new ReelFragment();
            mReelFragment.setArguments(reelArgs);
            fragmentManager.beginTransaction().replace(R.id.organization_container, mReelFragment).commit();
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
