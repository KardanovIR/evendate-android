package ru.evendate.android.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashSet;

import ru.evendate.android.R;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.OrganizationLoader;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
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
        ReelFragment.OnEventsDataLoadedListener, LoaderListener<OrganizationModelWithEvents>{
    private final String LOG_TAG = "OrganizationFragment";

    ReelFragment mReelFragment;
    OrganizationAdapter mAdapter;
    OrganizationLoader mOrganizationLoader;

    private int organizationId = -1;
    public static final String URI = "uri";
    private Uri mUri;

    private CoordinatorLayout mCoordinatorLayout;
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
        // make status bar transparent and change it state when toolbar collapsed
        ((AppBarLayout)rootView.findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new StatusBarColorChanger(getActivity()));
        mEventCountView = (TextView)rootView.findViewById(R.id.organization_event_count);
        mSubscriptionCountView = (TextView)rootView.findViewById(R.id.organization_subscription_count);
        mFriendCountView = (TextView)rootView.findViewById(R.id.organization_friend_count);
        mFavoriteEventCountTextView = (TextView)rootView.findViewById(R.id.organization_favorite_event_count);
        mOrganizationNameTextView = (TextView)rootView.findViewById(R.id.organization_name);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.organization_icon);
        mOrganizationImageView = (ImageView)rootView.findViewById(R.id.organization_image);

        mFAB = (FloatingActionButton) rootView.findViewById((R.id.fab));

        mAdapter = new OrganizationAdapter();
        mOrganizationLoader = new OrganizationLoader(getActivity());
        mOrganizationLoader.setLoaderListener(this);
        mOrganizationLoader.getOrganization(organizationId);

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
            //mFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.accent)));
            mFAB.setImageDrawable(getResources().getDrawable(R.mipmap.ic_done));
        } else {
            //mFAB.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            mFAB.setImageDrawable(getResources().getDrawable(R.mipmap.ic_add_white));
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
            }
        }
    }

    public void onClick(View v) {
        if(v == mFAB) {
            SubscriptAsyncTask subscriptAsyncTask = new SubscriptAsyncTask();
            subscriptAsyncTask.execute();
        }
    }

    //TODO выпилить?
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


    public void onLoaded(OrganizationModelWithEvents subList){
        mAdapter.setOrganizationModel(subList);
        mAdapter.setOrganizationInfo();
        setFabIcon();
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.organization.nativeInt, organizationId, false);
        mReelFragment.setDataListener(this);
        fragmentManager.beginTransaction().replace(R.id.organization_container, mReelFragment).commit();
    }

    @Override
    public void onError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("ERROR !!");
        builder.setMessage("Sorry there was an error getting data from the Internet.\nNetwork Unavailable!");

        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                mOrganizationLoader.getOrganization(organizationId);
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
