package ru.evendate.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.HashSet;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.AbsctractLoader;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.OrganizationLoader;
import ru.evendate.android.sync.EvendateApiFactory;
import ru.evendate.android.sync.EvendateService;
import ru.evendate.android.sync.EvendateServiceResponse;
import ru.evendate.android.sync.models.EventModel;
import ru.evendate.android.sync.models.OrganizationModel;
import ru.evendate.android.sync.models.OrganizationModelWithEvents;
import ru.evendate.android.sync.models.UserModel;

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
        mSubscriptionCountView.setOnClickListener(this);
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

    private class SubOrganizationLoader extends AbsctractLoader<Void> {
        OrganizationModel mOrganization;
        public SubOrganizationLoader(Context context, OrganizationModel organizationModel) {
            super(context);
            mOrganization = organizationModel;
        }

        public void execute(){
            Log.d(LOG_TAG, "performing sub");
            EvendateService evendateService = EvendateApiFactory.getEvendateService();
            Call<EvendateServiceResponse> call;
            if(mOrganization.isSubscribed()){
                call = evendateService.organizationDeleteSubscription(mOrganization.getEntryId(), peekToken());
            } else {
                call = evendateService.organizationPostSubscription(mOrganization.getEntryId(), peekToken());
            }

            call.enqueue(new Callback<EvendateServiceResponse>() {
                @Override
                public void onResponse(Response<EvendateServiceResponse> response,
                                       Retrofit retrofit) {
                    if (response.isSuccess()) {
                        mOrganization.setIsSubscribed(!mOrganization.isSubscribed());
                        int count_change = mOrganization.isSubscribed() ? 1 : -1;
                        mOrganization.setSubscribedCount(mOrganization.getSubscribedCount() + count_change);
                        mAdapter.setOrganizationInfo();
                        if(mOrganization.isSubscribed())
                            Snackbar.make(mCoordinatorLayout, R.string.subscription_confirm, Snackbar.LENGTH_LONG).show();
                        else
                            Snackbar.make(mCoordinatorLayout, R.string.removing_subscription_confirm, Snackbar.LENGTH_LONG).show();
                        setFabIcon();
                    } else {
                        Log.e(LOG_TAG, "Error with response with organization sub");
                        mListener.onError();
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    Log.e("Error", t.getMessage());
                    mListener.onError();
                }
            });
        }
    }
    public void onClick(View v) {
        if(v == mFAB) {
            SubOrganizationLoader subOrganizationLoader = new SubOrganizationLoader(getActivity(), mAdapter.getOrganizationModel());
            subOrganizationLoader.execute();
        }
        if(v == mSubscriptionCountView){
            Intent intent = new Intent(getContext(), UserListActivity.class);
            intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                    .appendPath(String.valueOf(mAdapter.getOrganizationModel().getEntryId())).build());
            intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.organization.nativeInt);
            startActivity(intent);
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
            for(UserModel friend : event.getFriendList())
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
        if(!isAdded())
            return;
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
        builder.setTitle(getString(R.string.loading_error));
        builder.setMessage(getString(R.string.loading_error_description));

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
