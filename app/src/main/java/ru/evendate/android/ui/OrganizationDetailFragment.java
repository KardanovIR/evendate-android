package ru.evendate.android.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.adapters.OrganizationEventsAdapter;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.loaders.LoaderListener;
import ru.evendate.android.loaders.OrganizationLoader;
import ru.evendate.android.loaders.SubOrganizationLoader;
import ru.evendate.android.models.OrganizationDetail;

/**
 * Contain details of organization
 */
public class OrganizationDetailFragment extends Fragment implements LoaderListener<OrganizationDetail>,
        OrganizationEventsAdapter.OrganizationCardController {
    private final String LOG_TAG = "OrganizationFragment";

    private OrganizationEventsAdapter mAdapter;
    private OrganizationLoader mOrganizationLoader;
    @Bind(R.id.recyclerView) RecyclerView mRecyclerView;

    private int organizationId = -1;
    public static final String URI = "uri";
    private Uri mUri;

    @Bind(R.id.main_content) CoordinatorLayout mCoordinatorLayout;
    @Bind(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @Bind(R.id.organization_image) ImageView mBackgroundView;

    int appBarOffset = 0;
    int scrollOffset = 0;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_organization, container, false);
        ButterKnife.bind(this, rootView);

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

        mAdapter = new OrganizationEventsAdapter(getContext(), this);

        mOrganizationLoader = new OrganizationLoader(getActivity());
        mOrganizationLoader.setLoaderListener(this);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                scrollOffset = Math.abs(recyclerView.getScrollY());
            }
        });
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                appBarOffset = Math.abs(verticalOffset);
                setImageViewY();
            }
        });
        mOrganizationLoader.getOrganization(organizationId);
        return rootView;
    }

    private void setImageViewY(){
        mBackgroundView.setY((-appBarOffset - scrollOffset) * 0.5f);
    }
    public void onSubscribed() {
        OrganizationDetail organization = mAdapter.getOrganization();
        SubOrganizationLoader subOrganizationLoader = new SubOrganizationLoader(getActivity(),
                organization, organization.isSubscribed());
        subOrganizationLoader.setLoaderListener(new LoaderListener<Void>() {
            @Override
            public void onLoaded(Void subList) {

            }

            @Override
            public void onError() {
                Toast.makeText(getActivity(), R.string.download_error, Toast.LENGTH_SHORT).show();
            }
        });
        organization.subscribe();
        Tracker tracker = EvendateApplication.getTracker();
        HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                .setCategory(getActivity().getString(R.string.stat_category_organization))
                .setLabel((Long.toString(organization.getEntryId())));
        if(organization.isSubscribed()){
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

    public void onPlaceClicked() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q="+ mAdapter.getOrganization().getDefaultAddress());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    public void onUsersClicked() {
        Intent intent = new Intent(getContext(), UserListActivity.class);
        intent.setData(EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                .appendPath(String.valueOf(mAdapter.getOrganization().getEntryId())).build());
        intent.putExtra(UserListFragment.TYPE, UserListFragment.TypeFormat.organization.nativeInt);
        startActivity(intent);
    }
    @Override
    public void onLinkClicked() {
        Intent openLink = new Intent(Intent.ACTION_VIEW);
        openLink.setData(Uri.parse(mAdapter.getOrganization().getSiteUrl()));
        startActivity(openLink);
    }

    public void onLoaded(OrganizationDetail organization){
        if(!isAdded())
            return;
        mAdapter.setOrganization(organization);
        Picasso.with(getActivity())
                .load(organization.getBackgroundUrl())
                .error(R.drawable.default_background)
                .into(mBackgroundView);
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
