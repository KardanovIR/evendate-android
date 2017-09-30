package ru.evendate.android.ui.search;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;

import io.reactivex.Observable;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.OrganizationFull;
import ru.evendate.android.models.OrganizationSubscription;
import ru.evendate.android.models.User;
import ru.evendate.android.models.UserDetail;
import ru.evendate.android.ui.AbstractAdapter;
import ru.evendate.android.ui.BaseActivity;
import ru.evendate.android.ui.EndlessContract;
import ru.evendate.android.ui.EndlessListFragment;
import ru.evendate.android.ui.EventsAdapter;
import ru.evendate.android.ui.catalog.OrganizationCatalogAdapter;
import ru.evendate.android.ui.eventdetail.EventDetailActivity;
import ru.evendate.android.ui.feed.ReelFragment;
import ru.evendate.android.ui.feed.ReelPresenter;
import ru.evendate.android.ui.orgdetail.OrganizationDetailActivity;
import ru.evendate.android.ui.userdetail.UserProfileActivity;
import ru.evendate.android.ui.users.UsersAdapter;

/**
 * Created by dmitry on 14.09.17.
 */
public abstract class SearchResultFragment<T extends EndlessContract.EndlessPresenter, D, VH extends RecyclerView.ViewHolder>
        extends EndlessListFragment<T, D, VH> {
    private String query;

    @Override
    protected String getEmptyHeader() {
        return getString(R.string.search_empty_header);
    }

    @Override
    protected String getEmptyDescription() {
        return getString(R.string.search_empty_description);
    }

    public void search(String query) {
        this.query = query;
        getAdapter().reset();
        getPresenter().reload();
    }

    @Override
    public Observable<String> requestAuth() {
        return ((BaseActivity)getActivity()).requestAuth();
    }

    public static class SearchEventFragment extends SearchResultFragment<ReelPresenter, Event, EventsAdapter.EventHolder>
            implements EventsAdapter.EventsInteractionListener {

        @Override
        protected AbstractAdapter<Event, EventsAdapter.EventHolder> getAdapter() {
            if (mAdapter != null)
                return mAdapter;
            else {
                return new EventsAdapter(getContext(), ReelFragment.ReelType.CALENDAR.type(), this);
            }
        }

        @Override
        public void openEvent(Event event) {
            Intent intent = new Intent(getContext(), EventDetailActivity.class);
            intent.setData(EvendateContract.EventEntry.getContentUri(event.getEntryId()));
            if (Build.VERSION.SDK_INT >= 21) {
                getContext().startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            } else
                getContext().startActivity(intent);
        }

        @Override
        public void likeEvent(Event event) {
            getPresenter().likeEvent(event);
        }

        @Override
        public void hideEvent(Event event) {
            getPresenter().hideEvent(event);
        }

        @Override
        public void search(String query) {
            getPresenter().setQuery(query);
            super.search(query);
        }
    }

    public static class SearchOrgFragment extends SearchResultFragment<OrganizationSearchPresenter, OrganizationFull, OrganizationCatalogAdapter.OrganizationHolder>
            implements OrganizationCatalogAdapter.OrganizationInteractionListener {

        @Override
        protected AbstractAdapter<OrganizationFull, OrganizationCatalogAdapter.OrganizationHolder> getAdapter() {
            if (mAdapter != null)
                return mAdapter;
            else {
                return new OrganizationCatalogAdapter(getContext(), this);
            }
        }

        @Override
        public void openOrg(OrganizationSubscription organization) {
            Intent intent = new Intent(getContext(), OrganizationDetailActivity.class);
            intent.setData(EvendateContract.OrganizationEntry.getContentUri(organization.getEntryId()));
            if (Build.VERSION.SDK_INT > 21)
                startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            else
                startActivity(intent);
        }

        @Override
        public void search(String query) {
            getPresenter().setQuery(query);
            super.search(query);
        }
    }

    public static class SearchUsersFragment extends SearchResultFragment<UserSearchPresenter, UserDetail, UsersAdapter.UserHolder>
            implements UsersAdapter.UsersInteractionListener {

        @Override
        protected void initRecyclerView() {
            super.initRecyclerView();
            mAdapter = new UsersAdapter(getContext(), this);
            mRecyclerView.setAdapter(mAdapter);
        }

        @Override
        protected AbstractAdapter<UserDetail, UsersAdapter.UserHolder> getAdapter() {
            if (mAdapter != null)
                return mAdapter;
            else {
                return new UsersAdapter(getContext(), this);
            }
        }

        @Override
        public void search(String query) {
            getPresenter().setQuery(query);
            super.search(query);
        }

        @Override
        public void openUser(User user) {
            Intent intent = new Intent(getContext(), UserProfileActivity.class);
            intent.setData(EvendateContract.UserEntry.getContentUri(user.getEntryId()));
            if (Build.VERSION.SDK_INT >= 21) {
                getContext().startActivity(intent,
                        ActivityOptions.makeSceneTransitionAnimation(getActivity()).toBundle());
            } else
                getContext().startActivity(intent);
        }
    }
}

