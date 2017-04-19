package ru.evendate.android.ui.tinder;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Event;
import ru.evendate.android.ui.EventDetailActivity;
import ru.evendate.android.views.LoadStateView;
import ru.evendate.android.views.TagsRecyclerView;

import static ru.evendate.android.ui.tinder.RecommenderContract.PAGE_LENGTH;

public class RecommenderFragment extends Fragment implements RecommenderContract.View, LoadStateView.OnReloadListener {

    final int LOAD_OFFSET = 3;
    @Bind(R.id.swipe_deck) SwipeDeck swipeDeck;
    SwipeDeckAdapter mAdapter;
    boolean canLoadMore = true;
    boolean loading = true;
    @Bind(R.id.load_state) LoadStateView mLoadStateView;
    private RecommenderContract.Presenter mPresenter;

    public static RecommenderFragment newInstance() {
        RecommenderFragment fragment = new RecommenderFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setPresenter(RecommenderContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_tinder_recommender, container, false);
        ButterKnife.bind(this, view);
        mAdapter = new SwipeDeckAdapter(getContext());
        swipeDeck.setAdapter(mAdapter);
        swipeDeck.setCallback(new SwipeDeck.SwipeDeckCallback() {
            @Override
            public void cardSwipedLeft(long stableId) {
                Event event = mAdapter.getItem((int) stableId);
                mPresenter.hideEvent(event);
            }

            @Override
            public void cardSwipedRight(long stableId) {
                Event event = mAdapter.getItem((int) stableId);
                mPresenter.faveEvent(event);
            }

            @Override
            public boolean isDragEnabled(long itemId) {
                return true;
            }

            @Override
            public void cardUnSwipe(long itemId) {
                Event event = mAdapter.getItem((int) itemId);
                mPresenter.unfaveEvent(event);
                mPresenter.unhideEvent(event);
            }

            @Override
            public void onClick(long itemId) {
                Event event = mAdapter.getItem((int) itemId);
                Intent intent = new Intent(getContext(), EventDetailActivity.class);
                intent.setData(EvendateContract.EventEntry.getContentUri(event.getEntryId()));
                if (Build.VERSION.SDK_INT >= 21) {
                    getContext().startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation((Activity) getContext()).toBundle());
                } else
                    getContext().startActivity(intent);
            }
        });
        swipeDeck.setLeftImage(R.id.image_hide);
        swipeDeck.setRightImage(R.id.image_fave);
        swipeDeck.setMask(R.id.tinder_mask);
        mLoadStateView.setOnReloadListener(this);
        mLoadStateView.setEmptyHeader(getString(R.string.recommender_empty_header));
        mLoadStateView.setEmptyDescription(getString(R.string.recommender_empty_description));
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mPresenter.start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.hide_button, R.id.revert_button, R.id.fave_button})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.hide_button:
                swipeDeck.swipeTopCardLeft(200);
                break;
            case R.id.revert_button:
                swipeDeck.unSwipeCard();
                break;
            case R.id.fave_button:
                swipeDeck.swipeTopCardRight(200);
                break;
        }
    }

    public void loadNext() {
        if (canLoadMore && !loading) {
            loading = true;
            mPresenter.loadRecommends(false, mAdapter.getCount() / PAGE_LENGTH);
        }
    }

    @Override
    public void onReload() {
        mPresenter.loadRecommends(true, 0);
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
    public void showRecommends(List<Event> events, boolean isLast) {
        if (isLast) {
            canLoadMore = false;
            mLoadStateView.showEmptryHint();
        }
        loading = false;
        mAdapter.addData(events);
    }

    @Override
    public void reshowRecommends(List<Event> events, boolean isLast) {
        if (isLast) {
            canLoadMore = false;
            mLoadStateView.showEmptryHint();
        }
        loading = false;
        mAdapter.setData(events);
    }

    @Override
    public void showEmptyState() {
        mLoadStateView.showEmptryHint();
    }

    @Override
    public void showError() {
        loading = false;
        mLoadStateView.showErrorHint();
    }

    class SwipeDeckAdapter extends BaseAdapter {

        private List<Event> data = new ArrayList<>();
        private Context context;

        SwipeDeckAdapter(Context context) {
            this.context = context;
        }

        public void setData(List<Event> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        public void addData(List<Event> data) {
            this.data.addAll(data);
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Event getItem(int position) {
            if (data.size() - LOAD_OFFSET < position)
                loadNext();
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            View view = convertView;
            ViewHolder viewHolder;
            if (view == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                view = inflater.inflate(R.layout.card_tinder_event, parent, false);
                viewHolder = new ViewHolder(view);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            Event event = getItem(position);
            Picasso.with(context).load(event.getImageHorizontalUrl()).into(viewHolder.eventImage);
            viewHolder.eventTitle.setText(event.getTitle());
            viewHolder.eventOrganizator.setText(event.getOrganizationShortName());
            viewHolder.eventTags.setTags(event.getTagList());
            return view;
        }

        class ViewHolder {
            @Bind(R.id.event_image) ImageView eventImage;
            @Bind(R.id.event_title) TextView eventTitle;
            @Bind(R.id.event_organizator) TextView eventOrganizator;
            @Bind(R.id.event_tags) TagsRecyclerView eventTags;

            ViewHolder(View view) {
                ButterKnife.bind(this, view);
            }
        }
    }
}
