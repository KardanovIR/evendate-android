package ru.evendate.android.ui.tinder;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daprlabs.aaron.swipedeck.SwipeDeck;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.views.TagsRecyclerView;

public class RecommenderFragment extends Fragment implements RecommenderContract.View {

    @Bind(R.id.swipe_deck) SwipeDeck swipeDeck;
    SwipeDeckAdapter mAdapter;
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
    public void onStart() {
        super.onStart();
        mPresenter.start();
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
                Event event = mAdapter.getItem((int) swipeDeck.getTopCardItemId());
                mPresenter.hideEvent(event);
                Toast.makeText(getContext(), "=(((((", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void cardSwipedRight(long stableId) {
                Event event = mAdapter.getItem((int) swipeDeck.getTopCardItemId());
                mPresenter.faveEvent(event);
                Toast.makeText(getContext(), "=)))", Toast.LENGTH_SHORT).show();
            }

            @Override
            public boolean isDragEnabled(long itemId) {
                return true;
            }

            @Override
            public void cardUnSwipe(long itemId) {
                Event event = mAdapter.getItem((int) swipeDeck.getTopCardItemId());
                mPresenter.unfaveEvent(event);
                mPresenter.unhideEvent(event);
            }

        });
        swipeDeck.setLeftImage(R.id.image_hide);
        swipeDeck.setRightImage(R.id.image_fave);
        swipeDeck.setMask(R.id.tinder_mask);
        return view;
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

    @Override
    public void setLoadingIndicator(boolean active) {

    }

    @Override
    public void showRecommends(List<Event> events, boolean isLast) {

    }

    @Override
    public void reshowRecommends(List<Event> events, boolean isLast) {
        mAdapter.setData(events);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void showEmptyState() {

    }

    @Override
    public void showError() {

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

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Event getItem(int position) {
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
            Event event = (Event) getItem(position);
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
