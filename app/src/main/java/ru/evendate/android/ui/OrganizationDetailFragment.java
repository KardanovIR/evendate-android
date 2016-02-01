package ru.evendate.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.HashSet;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponseAttr;
import ru.evendate.android.sync.EvendateSyncAdapter;
import ru.evendate.android.sync.ServerDataFetcher;
import ru.evendate.android.sync.models.EventModel;
import ru.evendate.android.sync.models.FriendModel;
import ru.evendate.android.sync.models.OrganizationModel;
import ru.evendate.android.sync.models.OrganizationModelWithEvents;

/**
 * Contain details of organization
 */
public class OrganizationDetailFragment extends Fragment implements View.OnClickListener,
        ReelFragment.OnEventsDataLoadedListener{
    private final String LOG_TAG = "OrganizationFragment";

    ReelFragment mReelFragment;
    OrganizationAdapter mAdapter;

    private int organizationId = -1;
    public static final String URI = "uri";
    private Uri mUri;

    private CoordinatorLayout mCoordinatorLayout;
    private AppBarLayout mAppBarLayout;
    private ImageView mOrganizationImageView;
    private ImageView mOrganizationIconView;


    private TextView mEventCountView;
    private TextView mSubscriptionCountView;
    private TextView mFriendCountView;
    private TextView mFavoriteEventCountTextView;
    private TextView mOrganizationNameTextView;

    private FloatingActionButton mFAB;

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
        mAppBarLayout = (AppBarLayout)rootView.findViewById(R.id.app_bar_layout);

        mEventCountView = (TextView)rootView.findViewById(R.id.organization_event_count);
        mSubscriptionCountView = (TextView)rootView.findViewById(R.id.organization_subscription_count);
        mFriendCountView = (TextView)rootView.findViewById(R.id.organization_friend_count);
        mFavoriteEventCountTextView = (TextView)rootView.findViewById(R.id.organization_favorite_event_count);
        mOrganizationNameTextView = (TextView)rootView.findViewById(R.id.organization_name);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.organization_icon);
        mOrganizationImageView = (ImageView)rootView.findViewById(R.id.organization_image);

        mFAB = (FloatingActionButton) rootView.findViewById((R.id.fab));

        //make status bar transparent
        //if (Build.VERSION.SDK_INT >= 21) {
        //    // Set the status bar to dark-semi-transparentish
        //    getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
        //            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //    mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
        //        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        //            if(verticalOffset == 0){
        //                //TODO
        //                //getActivity().getWindow().setFlags(WindowManager.LayoutParams.,
        //                //        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //            }else{
        //                getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
        //                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //            }
        //        }
        //    });
        //}

        mAdapter = new OrganizationAdapter();
        mFAB.setOnClickListener(this);
        return rootView;
    }

    public boolean subscript(){
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
            if(mAdapter.getOrganizationModel().isSubscribed()){
                if(ServerDataFetcher.organizationDeleteSubscription(evendateService, token, mAdapter.getOrganizationModel().getSubscriptionId()))
                    mAdapter.getOrganizationModel().setSubscriptionId(null);
            }
            else{
                OrganizationModel organizationModel = ServerDataFetcher.organizationPostSubscription(evendateService, token, organizationId);
                if(organizationModel == null)
                    return false;
                mAdapter.getOrganizationModel().setSubscriptionId(organizationModel.getSubscriptionId());
            }
        return true;
    }

    private void setFabIcon(){
        if(!isAdded())
            return;
        if (mAdapter.getOrganizationModel().isSubscribed()) {
            mFAB.setImageDrawable(getResources().getDrawable(R.mipmap.ic_done));
        } else {
            mFAB.setImageDrawable(getResources().getDrawable(R.mipmap.ic_add_white));
            // To over-ride the color of the FAB other then the theme color
            mFAB.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
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
            return subscript();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(!result){
                Snackbar.make(mCoordinatorLayout, R.string.subscription_fail_cause_network, Snackbar.LENGTH_LONG).show();
            }
            else{
                mAdapter.getOrganizationModel().setIsSubscribed(!mAdapter.getOrganizationModel().isSubscribed());
                int count_change = mAdapter.getOrganizationModel().isSubscribed() ? 1 : -1;
                mAdapter.getOrganizationModel().setSubscribedCount(mAdapter.getOrganizationModel().getSubscribedCount() + count_change);
                mAdapter.setOrganizationInfo();
                setFabIcon();
                if(mAdapter.getOrganizationModel().isSubscribed())
                    Snackbar.make(mCoordinatorLayout, R.string.subscription_confirm, Snackbar.LENGTH_LONG).show();
                else
                    Snackbar.make(mCoordinatorLayout, R.string.removing_subscription_confirm, Snackbar.LENGTH_LONG).show();
                ContentResolver contentResolver = getActivity().getContentResolver();
                contentResolver.update(mUri, mAdapter.getOrganizationModel().getContentValues(), null, null);
                //if(mOrganizationModel.isSubscribed())
                    //mReelFragment.onSubscribed();
                //else
                    //mReelFragment.onUnsubscripted();
                //EvendateSyncAdapter.syncImmediately(getContext());
                for(EventModel event : mReelFragment.getEventList()){
                    try {
                        contentResolver.applyBatch(EvendateContract.CONTENT_AUTHORITY, event.getInsertDates());
                    }catch (Exception e){
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void onClick(View v) {
        if(v == mFAB) {
            SubscriptAsyncTask subscriptAsyncTask = new SubscriptAsyncTask();
            subscriptAsyncTask.execute();
        }
    }

    @Override
    public void onEventsDataLoaded() {
        if(!isAdded())
            return;
        mEventCountView.setText(String.valueOf(mReelFragment.getEventList().size()));
        int favoriteCount = 0;
        HashSet<Integer> friendSet = new HashSet<>();
        for(EventModel event : mReelFragment.getEventList()){
            favoriteCount += event.isFavorite() ? 1 : 0;
            for(FriendModel friend : event.getFriendList())
            friendSet.add(friend.getEntryId());
        }
        String favoriteEvents = favoriteCount + " " + getResources().getString(R.string.favorite_events);
        mFavoriteEventCountTextView.setText(favoriteEvents);
        mFriendCountView.setText(String.valueOf(friendSet.size()));
    }

    private class OrganizationAdapter{
        private OrganizationModel mOrganizationModel;

        public void setOrganizationModel(OrganizationModel organizationModel) {
            this.mOrganizationModel = organizationModel;
        }

        public OrganizationModel getOrganizationModel() {
            return mOrganizationModel;
        }

        private void setOrganizationInfo(){
            //prevent illegal state exception cause fragment not attached to
            if(!isAdded())
                return;
            mOrganizationNameTextView.setText(mOrganizationModel.getName());
            mSubscriptionCountView.setText(String.valueOf(mOrganizationModel.getSubscribedCount()));
            Picasso.with(getContext())
                    .load(mOrganizationModel.getBackgroundMediumUrl())
                    .error(R.drawable.default_background)
                    .into(mOrganizationImageView);
            Picasso.with(getContext())
                    .load(mOrganizationModel.getLogoSmallUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(mOrganizationIconView);
        }
    }
    private class OrganizationLoader{
        public void getOrganization(){
            if(!EvendateSyncAdapter.checkInternetConnection(getContext()))
                Toast.makeText(getContext(), R.string.no_internet_connection, Toast.LENGTH_LONG).show();
            Log.d(LOG_TAG, "getting organization");
            EvendateService evendateService = EvendateApiFactory.getEvendateService();

            AccountManager accountManager = AccountManager.get(getActivity());
            String token;
            try {
                token = accountManager.peekAuthToken(EvendateAccountManager.getSyncAccount(getActivity()),
                        getString(R.string.account_type));
            } catch (Exception e){
                Log.d(LOG_TAG, "Error with peeking token");
                e.fillInStackTrace();
                //TODO
                Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
                onDownloaded();
                return;
            }
            Call<EvendateServiceResponseAttr<OrganizationModelWithEvents>>  call = evendateService.organizationWithEventsData(organizationId, token);

            call.enqueue(new Callback<EvendateServiceResponseAttr<OrganizationModelWithEvents>>() {
                @Override
                public void onResponse(Response<EvendateServiceResponseAttr<OrganizationModelWithEvents>> response,
                                       Retrofit retrofit) {
                    if (response.isSuccess()) {
                        mAdapter.setOrganizationModel(response.body().getData());
                    } else {
                        Log.d(LOG_TAG, "Error with response with events");
                        // error response, no access to resource?
                    }
                    Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
                    onDownloaded();
                }

                @Override
                public void onFailure(Throwable t) {
                    // something went completely south (like no internet connection)
                    Log.d("Error", t.getMessage());
                    Toast.makeText(getContext(), R.string.download_error, Toast.LENGTH_LONG).show();
                    onDownloaded();
                }
            });
        }
    }

    public void onDownloaded(){
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        if(!EvendateSyncAdapter.checkInternetConnection(getContext())){
            Snackbar.make(mCoordinatorLayout, R.string.subscription_fail_cause_network, Snackbar.LENGTH_LONG).show();
            return;
        }
        else{
            mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.organization.nativeInt, organizationId, false);
            mReelFragment.setDataListener(this);
        }
        fragmentManager.beginTransaction().replace(R.id.organization_container, mReelFragment).commit();
    }


    /**
     * fix cause bug in ChildFragmentManager
     * http://stackoverflow.com/questions/15207305/getting-the-error-java-lang-illegalstateexception-activity-has-been-destroyed
     */
    //@Override
    // public void onDetach() {
    //    super.onDetach();
//
    //    try {
    //        Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
    //        childFragmentManager.setAccessible(true);
    //        childFragmentManager.set(this, null);
//
    //    } catch (NoSuchFieldException e) {
    //        throw new RuntimeException(e);
    //    } catch (IllegalAccessException e) {
    //        throw new RuntimeException(e);
    //    }
    //}
}
