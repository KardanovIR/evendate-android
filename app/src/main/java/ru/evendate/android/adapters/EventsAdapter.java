package ru.evendate.android.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.EventDetail;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.ui.EventDetailActivity;
import ru.evendate.android.ui.ReelFragment;

/**
 * Created by Dmitry on 01.12.2015.
 */

public class EventsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    protected Context mContext;
    private ArrayList<EventFeed> mEventList;
    private int type;
    public static Uri mUri = EvendateContract.EventEntry.CONTENT_URI;


    public EventsAdapter(Context context, int type){
        this.mContext = context;
        this.type = type;
    }

    public void setEventList(ArrayList<EventFeed> eventList){
        mEventList = eventList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        int layoutItemId;
        if(type == ReelFragment.TypeFormat.ORGANIZATION.type()){
            layoutItemId = R.layout.reel_item;
        } else if(type == ReelFragment.TypeFormat.FAVORITES.type()){
            layoutItemId = R.layout.reel_favorite_item;
        } else if(type == ReelFragment.TypeFormat.CALENDAR.type()){
            layoutItemId = R.layout.reel_item;
        } else{
            layoutItemId = R.layout.reel_item;
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
        if(mEventList == null)
            return;
        EventFeed eventEntry = mEventList.get(position);
        EventHolder holder = (EventHolder)viewHolder;
        holder.id = eventEntry.getEntryId();
        holder.mTitleTextView.setText(eventEntry.getTitle());
        if(type != ReelFragment.TypeFormat.FAVORITES.type()){
            if(eventEntry.getTitle().length() > 60)
                holder.mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            else
                holder.mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        }
        holder.mOrganizationTextView.setText(eventEntry.getOrganizationShortName());
        if(eventEntry.isFavorite() && type != ReelFragment.TypeFormat.FAVORITES.type()){
            holder.mFavoriteIndicator.setVisibility(View.VISIBLE);
        }
        String date = EventFormatter.formatDate((EventDetail)eventEntry);
        holder.mDateTextView.setText(date);
        Picasso.with(mContext)
                .load(type == ReelFragment.TypeFormat.FAVORITES.type() ? eventEntry.getImageHorizontalUrl() : eventEntry.getImageVerticalUrl())
                .error(R.drawable.default_background)
                .into(holder.mEventImageView);
    }

    @Override
    public int getItemCount() {
        if(mEventList == null)
            return 0;
        return mEventList.size();
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
        if(!(viewHolder instanceof EventHolder))
            return;
        EventHolder holder = (EventHolder)viewHolder;
        if(holder.mFavoriteIndicator != null)
            holder.mFavoriteIndicator.setVisibility(View.INVISIBLE);
    }

    public class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View holderView;
        public ImageView mEventImageView;
        public TextView mTitleTextView;
        public TextView mDateTextView;
        public TextView mOrganizationTextView;
        public View mFavoriteIndicator;
        public int id;

        public EventHolder(View itemView){
            super(itemView);
            holderView = itemView;
            mEventImageView = (ImageView)itemView.findViewById(R.id.event_item_image);
            mTitleTextView = (TextView)itemView.findViewById(R.id.event_item_title);
            mDateTextView = (TextView)itemView.findViewById(R.id.event_item_date);
            mOrganizationTextView = (TextView)itemView.findViewById(R.id.event_item_organization);
            mFavoriteIndicator = itemView.findViewById(R.id.event_item_favorite_indicator);
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v == holderView){
                Intent intent = new Intent(mContext, EventDetailActivity.class);
                intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                mContext.startActivity(intent);
            }
        }

    }
}