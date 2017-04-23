package ru.evendate.android.ui.checkin;


import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ybq.endless.Endless;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.recyclerview.animators.LandingAnimator;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.ui.AbstractEndlessAdapter;
import ru.evendate.android.ui.utils.TicketFormatter;
import ru.evendate.android.ui.utils.UserFormatter;
import ru.evendate.android.views.LoadStateView;

public class TicketsAdminFragment extends Fragment {

    int eventId;
    @Bind(R.id.view_pager) ViewPager mViewPager;
    CheckInContract.SearchClickListener mListener;
    TicketsAdminListFragment fragment;
    TicketsAdminListFragment fragment2;

    public static TicketsAdminFragment newInstance(int eventId) {
        TicketsAdminFragment fragment = new TicketsAdminFragment();
        fragment.eventId = eventId;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tickets_admin, container, false);
        ButterKnife.bind(this, view);
        setHasOptionsMenu(true);

        mViewPager.setAdapter(new FragmentStatePagerAdapter(getChildFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        fragment = TicketsAdminListFragment.newInstance(eventId, false);
                        new TicketsAdminPresenter(new DataRepository(getContext()), (CheckInContract.TicketsAdminView)fragment);
                        return fragment;
                    case 1:
                        fragment2 = TicketsAdminListFragment.newInstance(eventId, true);
                        new TicketsAdminPresenter(new DataRepository(getContext()), (CheckInContract.TicketsAdminView)fragment2);
                        return fragment2;
                    default:
                        throw new RuntimeException();
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.check_in_tickets_waiting);
                    case 1:
                        return getString(R.string.check_in_tickets_confirmed);
                    default:
                        return null;
                }
            }

            @Override
            public void restoreState(Parcelable state, ClassLoader loader) {
                super.restoreState(state, loader);
                fragment = (TicketsAdminListFragment)getChildFragmentManager().getFragment((Bundle)state, "f0");
                new TicketsAdminPresenter(new DataRepository(getContext()), fragment);
                fragment2 = (TicketsAdminListFragment)getChildFragmentManager().getFragment((Bundle)state, "f1");
                new TicketsAdminPresenter(new DataRepository(getContext()), fragment2);
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.ticket_admin_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            mListener.searchAction();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        TabLayout tabLayout = (TabLayout)getActivity().findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (CheckInContract.SearchClickListener)context;
    }

    //todo need be replace by caching and observers
    public void updateData() {
        fragment.updateData();
        fragment2.updateData();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }


    public static class TicketsAdminListFragment extends Fragment implements CheckInContract.TicketsAdminView {

        public static final String KEY_QUERY = "key_query";
        public static final String KEY_IS_SEARCH = "key_is_search";
        public static final String KEY_IS_CHECKOUT = "key_is_checkout";
        public static final String KEY_EVENT_ID = "key_event_id";
        @Bind(R.id.load_state) LoadStateView mLoadStateView;
        @Bind(R.id.recycler_view) RecyclerView mRecyclerView;
        @Bind(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
        TicketAdminRecyclerViewAdapter mAdapter;
        CheckInContract.TicketAdminPresenter mPresenter;
        TicketsAdminFragment mParent;
        CheckInContract.TicketInteractionListener mListener;
        private Endless mEndless;

        public static TicketsAdminListFragment newInstance(int eventId, boolean isCheckOut) {
            TicketsAdminListFragment fragment = new TicketsAdminListFragment();
            final Bundle args = new Bundle();
            args.putBoolean(KEY_IS_CHECKOUT, isCheckOut);
            args.putInt(KEY_EVENT_ID, eventId);
            fragment.setArguments(args);
            return fragment;
        }

        public static TicketsAdminListFragment newInstance(String query) {
            TicketsAdminListFragment fragment = new TicketsAdminListFragment();
            final Bundle args = new Bundle();
            args.putString(KEY_QUERY, query);
            fragment.setArguments(args);
            return fragment;
        }

        public static TicketsAdminListFragment newSearchInstance(int eventId) {
            TicketsAdminListFragment fragment = new TicketsAdminListFragment();
            final Bundle args = new Bundle();
            args.putBoolean(KEY_IS_SEARCH, true);
            args.putInt(KEY_EVENT_ID, eventId);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void setPresenter(CheckInContract.TicketAdminPresenter presenter) {
            mPresenter = presenter;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_tickets_admin_list, container, false);
            ButterKnife.bind(this, view);
            setHasOptionsMenu(getArguments().getBoolean(KEY_IS_SEARCH, false));

            mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

            mAdapter = new TicketAdminRecyclerViewAdapter(getContext(), mListener, getArguments().getInt(KEY_EVENT_ID));
            mRecyclerView.setAdapter(mAdapter);
            mRecyclerView.setHasFixedSize(true);
            //todo not work cause Endless notifyDataSetChanged
            mRecyclerView.setItemAnimator(new LandingAnimator());

            View loadingView = inflater.inflate(R.layout.item_progress, container, false);
            mEndless = Endless.applyTo(mRecyclerView, loadingView);
            mEndless.setLoadMoreListener((int page) -> {
                if (getArguments().getBoolean(KEY_IS_SEARCH)) {
                    mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getString(KEY_QUERY), false, page);
                } else {
                    mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getBoolean(KEY_IS_CHECKOUT), false, page);
                }
            });

            mSwipeRefreshLayout.setOnRefreshListener(() -> {
                mEndless.setLoadMoreAvailable(false);
                mParent.updateData();
                if (getArguments().getBoolean(KEY_IS_SEARCH)) {
                    mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getString(KEY_QUERY), true, 0);
                } else {
                    mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getBoolean(KEY_IS_CHECKOUT), true, 0);
                }
            });
            mLoadStateView.setOnReloadListener(() -> {
                if (getArguments().getBoolean(KEY_IS_SEARCH)) {
                    mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getString(KEY_QUERY), true, 0);
                } else {
                    mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getBoolean(KEY_IS_CHECKOUT), true, 0);
                }
            });

            if (getArguments().getBoolean(KEY_IS_SEARCH)) {
                mLoadStateView.setHintDescription(getString(R.string.check_in_search_tickets_hint_description));
                mLoadStateView.setEmptyDescription(getString(R.string.check_in_search_tickets_empty_description));
                mLoadStateView.showHint();
                mSwipeRefreshLayout.setEnabled(false);
            } else {
                mLoadStateView.setEmptyDescription(getString(R.string.check_in_tickets_empty_description));
            }
            return view;
        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mListener = (CheckInContract.TicketInteractionListener)context;
            mParent = (TicketsAdminFragment)getParentFragment();
        }

        @Override
        public void onDetach() {
            super.onDetach();
            mListener = null;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.search_menu, menu);
        }

        @Override
        public void onPrepareOptionsMenu(Menu menu) {
            MenuItem searchItem = menu.findItem(R.id.action_search);
            TicketsAdminListFragment fragment = this;

            SearchView searchView = (SearchView)searchItem.getActionView();
            searchView.setIconified(false);
            searchView.setQueryHint(getString(R.string.check_in_search_tickets_query_hint));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    fragment.search(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
            searchView.setOnCloseListener(() -> {
                getActivity().onBackPressed();
                return true;
            });
        }

        @Override
        public void onStart() {
            super.onStart();
            if (getArguments() != null && getArguments().getString(KEY_QUERY) != null)
                search(getArguments().getString(KEY_QUERY));
            if (!getArguments().getBoolean(KEY_IS_SEARCH)) {
                mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getBoolean(KEY_IS_CHECKOUT), true, 0);
            }
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            ButterKnife.unbind(this);
        }

        public void search(String query) {
            getArguments().putString(KEY_QUERY, query);
            mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), query, true, 0);
        }

        //todo may be inconsistent
        public void updateData() {
            if (mLoadStateView.isLoading() || mSwipeRefreshLayout.isRefreshing()) {
                return;
            }
            if (getArguments().getBoolean(KEY_IS_SEARCH)) {
                mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getString(KEY_QUERY), true, 0);
            } else {
                mPresenter.loadList(getArguments().getInt(KEY_EVENT_ID), getArguments().getBoolean(KEY_IS_CHECKOUT), true, 0);
            }
        }

        @Override
        public void showSearchEmptyState() {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLoadStateView.showEmptryHint();
        }

        @Override
        public void showList(List<CheckInContract.TicketAdmin> list, boolean isLast) {
            mRecyclerView.setVisibility(View.VISIBLE);
            mEndless.loadMoreComplete();
            mEndless.setLoadMoreAvailable(!isLast);
            mAdapter.add(list);
        }

        @Override
        public void reshowList(List<CheckInContract.TicketAdmin> list, boolean isLast) {
            mSwipeRefreshLayout.setRefreshing(false);
            mRecyclerView.setVisibility(View.VISIBLE);
            mEndless.loadMoreComplete();
            mEndless.setLoadMoreAvailable(!isLast);
            mAdapter.set(list);
        }

        @Override
        public void setLoadingIndicator(boolean active) {
            if (active) {
                mLoadStateView.showProgress();
            } else {
                mLoadStateView.hideProgress();
            }
        }

        @Override
        public void showEmptyState() {
            mSwipeRefreshLayout.setRefreshing(false);
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLoadStateView.showEmptryHint();
        }

        @Override
        public void showError() {
            mEndless.loadMoreComplete();
            mEndless.setLoadMoreAvailable(false);
            mSwipeRefreshLayout.setRefreshing(false);
            mLoadStateView.showErrorHint();
            mRecyclerView.setVisibility(View.INVISIBLE);
        }

        class TicketAdminRecyclerViewAdapter extends AbstractEndlessAdapter<CheckInContract.TicketAdmin,
                RecyclerView.ViewHolder> {

            private final CheckInContract.TicketInteractionListener mListener;
            private Context mContext;
            private int eventId;

            TicketAdminRecyclerViewAdapter(@NonNull Context context,
                                           @NonNull CheckInContract.TicketInteractionListener listener,
                                           int eventId) {
                mListener = listener;
                mContext = context;
                this.eventId = eventId;
            }

            @Override
            public TicketAdminViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);
                return new TicketAdminViewHolder(view);
            }

            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
                super.onViewRecycled(holder);
                if (holder instanceof TicketAdminViewHolder)
                    ((TicketAdminViewHolder)holder).mAvatar.setImageDrawable(null);
            }

            @Override
            public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
                TicketAdminViewHolder viewHolder = (TicketAdminViewHolder)holder;
                CheckInContract.TicketAdmin ticket = getItem(position);
                viewHolder.mTicket = ticket;
                viewHolder.mName.setText(UserFormatter.formatUserName(ticket.getUser()));
                viewHolder.mTicketNumber.setText(TicketFormatter.formatNumber(getContext(), ticket.getNumber()));
                Picasso.with(mContext).load(ticket.getUser().getAvatarUrl()).into(viewHolder.mAvatar);

                viewHolder.holderView.setOnClickListener((View v) -> mListener.onTicketSelected(eventId, ticket.getUuid()));
            }

            class TicketAdminViewHolder extends RecyclerView.ViewHolder {
                View holderView;

                @Bind(R.id.name) TextView mName;
                @Bind(R.id.ticket_number) TextView mTicketNumber;
                @Bind(R.id.avatar) CircleImageView mAvatar;
                @Nullable CheckInContract.TicketAdmin mTicket;

                TicketAdminViewHolder(View view) {
                    super(view);
                    holderView = itemView;
                    ButterKnife.bind(this, view);
                }
            }
        }
    }
}
