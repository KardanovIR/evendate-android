package ru.evendate.android.ui;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.HashSet;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.OrganizationLoader;
import ru.evendate.android.loaders.SubOrganizationLoader;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.Organization;
import ru.evendate.android.models.OrganizationDetail;
import ru.evendate.android.models.User;

/**
 * Contain details of organization
 */
public class OrganizationDetailFragment extends Fragment implements View.OnClickListener,
        ReelFragment.OnEventsDataLoadedListener, LoaderListener<OrganizationDetail>{
    private final String LOG_TAG = "OrganizationFragment";

    private ReelFragment mReelFragment;
    private OrganizationAdapter mAdapter;
    private OrganizationLoader mOrganizationLoader;

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
            Tracker tracker = EvendateApplication.getTracker();
            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                    .setCategory(getString(R.string.stat_category_organization))
                    .setAction(getString(R.string.stat_action_view))
                    .setLabel(Long.toString(organizationId));
            tracker.send(event.build());
        }

        mCoordinatorLayout = (CoordinatorLayout)rootView.findViewById(R.id.main_content);
        // make status bar transparent and change it state when toolbar collapsed
        ((AppBarLayout)rootView.findViewById(R.id.app_bar_layout)).addOnOffsetChangedListener(new StatusBarColorChanger(getActivity()));
        mEventCountView = (TextView)rootView.findViewById(R.id.organization_event_count);
        mSubscriptionCountView = (TextView)rootView.findViewById(R.id.organization_subscription_count);
        rootView.findViewById(R.id.organization_subscribed_button).setOnClickListener(this);
        mFriendCountView = (TextView)rootView.findViewById(R.id.organization_friend_count);
        mFavoriteEventCountTextView = (TextView)rootView.findViewById(R.id.organization_favorite_event_count);
        mOrganizationNameTextView = (TextView)rootView.findViewById(R.id.organization_name);

        mOrganizationIconView = (ImageView)rootView.findViewById(R.id.organization_icon);
        mOrganizationImageView = (ImageView)rootView.findViewById(R.id.organization_image);

        mFAB = (FloatingActionButton) rootView.findViewById((R.id.fab));

        mAdapter = new OrganizationAdapter();
        mOrganizationLoader = new OrganizationLoader(getActivity());
        mOrganizationLoader.setLoaderListener(this);

        mFAB.setOnClickListener(this);
        mOrganizationLoader.getOrganization(organizationId);
        return rootView;
    }

    private void setFabIcon(){
        if (mAdapter.getOrganizationModel().isSubscribed()) {
            //mFAB.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.accent)));
            mFAB.setImageDrawable(getResources().getDrawable(R.mipmap.ic_done));
        } else {
            //mFAB.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
            mFAB.setImageDrawable(getResources().getDrawable(R.mipmap.ic_add_white));
        }
    }

    public void onClick(View v) {
        if(v == mFAB) {
            if(mAdapter.getOrganizationModel() == null)
                return;
            SubOrganizationLoader subOrganizationLoader = new SubOrganizationLoader(getActivity(),
                    (Organization)mAdapter.getOrganizationModel(), mAdapter.getOrganizationModel().isSubscribed());
            subOrganizationLoader.setLoaderListener(new LoaderListener<Void>() {
                @Override
                public void onLoaded(Void subList) {

                }

                @Override
                public void onError() {
                    Toast.makeText(getActivity(), R.string.download_error, Toast.LENGTH_SHORT).show();
                }
            });
            mAdapter.getOrganizationModel().subscribe();
            mAdapter.setOrganizationInfo();
            Tracker tracker = EvendateApplication.getTracker();
            HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                    .setCategory(getActivity().getString(R.string.stat_category_organization))
                    .setLabel((Long.toString(mAdapter.getOrganizationModel().getEntryId())));
            if(mAdapter.getOrganizationModel().isSubscribed()){
                event.setAction(getActivity().getString(R.string.stat_action_subscribe));
                Snackbar.make(mCoordinatorLayout, R.string.subscription_confirm, Snackbar.LENGTH_LONG).show();
            }
            else{
                event.setAction(getActivity().getString(R.string.stat_action_unsubscribe));
                Snackbar.make(mCoordinatorLayout, R.string.removing_subscription_confirm, Snackbar.LENGTH_LONG).show();
            }
            tracker.send(event.build());
            subOrganizationLoader.execute();
        }
        if(v.getId() == R.id.organization_subscribed_button){
            if(mAdapter.getOrganizationModel() == null)
                return;
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
        for(EventFeed event : mReelFragment.getEventList()){
            favoriteCount += event.isFavorite() ? 1 : 0;
        }
        String favoriteEvents = favoriteCount + " " + getResources().getString(R.string.favorite_events);
        mFavoriteEventCountTextView.setText(favoriteEvents);
    }

    private class OrganizationAdapter{
        private OrganizationDetail mOrganizationModel;

        public void setOrganizationModel(OrganizationDetail organizationModel) {
            this.mOrganizationModel = organizationModel;
        }

        public OrganizationDetail getOrganizationModel() {
            return mOrganizationModel;
        }

        private void setOrganizationInfo(){
            mOrganizationNameTextView.setText(mOrganizationModel.getName());
            mSubscriptionCountView.setText(String.valueOf(mOrganizationModel.getSubscribedCount()));
            HashSet<User> friendSet = new HashSet<>();
            for(User user : mOrganizationModel.getSubscribedUsersList()){
                if(user.is_friend())
                    friendSet.add(user);
            }
            mFriendCountView.setText(String.valueOf(friendSet.size()));
            Picasso.with(getContext())
                    .load(mOrganizationModel.getBackgroundUrl())
                    .error(R.drawable.default_background)
                    .into(mOrganizationImageView);
            Picasso.with(getContext())
                    .load(mOrganizationModel.getLogoMediumUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(mOrganizationIconView);
            setFabIcon();
        }
    }


    public void onLoaded(OrganizationDetail organization){
        mAdapter.setOrganizationModel(organization);
        if(!isAdded())
            return;
        mAdapter.setOrganizationInfo();
        android.support.v4.app.FragmentManager fragmentManager = getChildFragmentManager();
        mReelFragment = ReelFragment.newInstance(ReelFragment.TypeFormat.ORGANIZATION.type(), organizationId, false);
        mReelFragment.setDataListener(this);
        fragmentManager.beginTransaction().replace(R.id.organization_container, mReelFragment).commit();
    }

    @Override
    public void onError() {
        if(!isAdded())
            return;
        AlertDialog dialog = ErrorAlertDialogBuilder.newInstance(getActivity(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        mOrganizationLoader.getOrganization(organizationId);
                        dialog.dismiss();
                    }
                });
        dialog.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mOrganizationLoader.cancel();
    }
}
