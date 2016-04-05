package ru.evendate.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.ui.EventDetailActivity;
import ru.evendate.android.ui.ReelFragment;

/**
 * Created by Dmitry on 01.12.2015.
 */

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Context mContext;
    private ArrayList<EventFeed> mEventList;
    private int type;
    public static Uri mUri = EvendateContract.EventEntry.CONTENT_URI;


    public EventsAdapter(Context context, int type) {
        this.mContext = context;
        this.type = type;
    }

    public void setEventList(ArrayList<EventFeed> eventList) {
        mEventList = eventList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int layoutItemId;
        if (type == ReelFragment.TypeFormat.ORGANIZATION.type()) {
            layoutItemId = R.layout.card_event_organization;
        } else if (type == ReelFragment.TypeFormat.FAVORITES.type()) {
            layoutItemId = R.layout.card_event_feed;
        } else if (type == ReelFragment.TypeFormat.CALENDAR.type()) {
            layoutItemId = R.layout.card_event;
        } else if (type == ReelFragment.TypeFormat.FEED.type()) {
            layoutItemId = R.layout.card_event_feed;
        } else {
            layoutItemId = R.layout.card_event;
        }
        return layoutItemId;
    }

    public ArrayList<EventFeed> getEventList() {
        return mEventList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EventHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (mEventList == null)
            return;
        EventFeed eventEntry = mEventList.get(position);
        EventHolder holder = (EventHolder)viewHolder;
        holder.id = eventEntry.getEntryId();
        holder.mTitleTextView.setText(eventEntry.getTitle());
        if (holder.mOrganizationTextView != null)
            holder.mOrganizationTextView.setText(eventEntry.getOrganizationShortName());
        if (eventEntry.isFavorite())
            holder.mFavoriteIndicator.setVisibility(View.VISIBLE);
        String date = EventFormatter.formatDate(eventEntry.getNearestDate());
        holder.mDateTextView.setText(date);
        Picasso.with(mContext)
                .load(eventEntry.getImageHorizontalUrl())
                .error(R.drawable.default_background)
                .into(holder.mEventImageView);

        if (type == ReelFragment.TypeFormat.CALENDAR.type())
            return;
        if (holder.mOrganizationLogo != null)
            Picasso.with(mContext)
                    .load(eventEntry.getOrganizationLogoSmallUrl())
                    .error(R.drawable.evendate_logo)
                    .into(holder.mOrganizationLogo);
        holder.mPriceTextView.setText(eventEntry.isFree() ? holder.eventFreeLabel : String.valueOf(eventEntry.getMinPrice()));
    }

    @Override
    public int getItemCount() {
        if (mEventList == null)
            return 0;
        return mEventList.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
        if (!(viewHolder instanceof EventHolder))
            return;
        EventHolder holder = (EventHolder)viewHolder;
        if (holder.mFavoriteIndicator != null)
            holder.mFavoriteIndicator.setVisibility(View.INVISIBLE);
    }

    public class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        @Bind(R.id.event_item_image)
        public ImageView mEventImageView;
        @Bind(R.id.event_item_title)
        public TextView mTitleTextView;
        @Bind(R.id.event_item_date)
        public TextView mDateTextView;
        @Nullable @Bind(R.id.event_item_price)
        public TextView mPriceTextView;
        @Nullable @Bind(R.id.event_item_organization)
        public TextView mOrganizationTextView;
        @Nullable @Bind(R.id.event_item_organization_icon)
        public ImageView mOrganizationLogo;
        @Bind(R.id.event_item_favorite_indicator)
        public View mFavoriteIndicator;
        public int id;
        @BindString(R.string.event_free)
        public String eventFreeLabel;

        public EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                Intent intent = new Intent(mContext, EventDetailActivity.class);
                intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                mContext.startActivity(intent);
            }
        }

    }
}