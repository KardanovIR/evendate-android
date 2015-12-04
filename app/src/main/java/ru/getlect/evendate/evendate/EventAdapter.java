package ru.getlect.evendate.evendate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.models.EventFormatter;
import ru.getlect.evendate.evendate.sync.models.EventModel;

/**
 * Created by Dmitry on 01.12.2015.
 */

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventHolder>{

    Context mContext;
    private ArrayList<EventModel> mEventList;
    int type;
    public static Uri mUri = EvendateContract.EventEntry.CONTENT_URI;


    public EventAdapter(Context context, int type){
        this.mContext = context;
        this.type = type;
    }

    public void setEventList(ArrayList<EventModel> eventList){
        mEventList = eventList;
        notifyDataSetChanged();
    }

    public ArrayList<EventModel> getEventList() {
        return mEventList;
    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutItemId;
        if(type == ReelFragment.TypeFormat.organization.nativeInt){
            layoutItemId = R.layout.reel_item;
        } else if(type == ReelFragment.TypeFormat.favorites.nativeInt){
            layoutItemId = R.layout.reel_favorite_item;
        } else if(type == ReelFragment.TypeFormat.calendar.nativeInt){
            layoutItemId = R.layout.reel_item;
        } else if(type == ReelFragment.TypeFormat.organizationSubscribed.nativeInt){
            layoutItemId = R.layout.reel_item;
        }
        else{
            layoutItemId = R.layout.reel_item;
        }
        return new EventHolder(LayoutInflater.from(parent.getContext()).inflate(layoutItemId, parent, false));
    }

    @Override
    public void onBindViewHolder(EventHolder holder, int position) {
        if(mEventList == null)
            return;
        EventModel eventEntry = mEventList.get(position);
        holder.id = eventEntry.getEntryId();
        holder.mTitleTextView.setText(eventEntry.getTitle());
        if(eventEntry.getTitle().length() > 60)
            holder.mTitleTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        holder.mOrganizationTextView.setText(eventEntry.getOrganizationShortName());
        if(eventEntry.isFavorite() && type != ReelFragment.TypeFormat.favorites.nativeInt){
            holder.mFavoriteIndicator.setVisibility(View.VISIBLE);
        }
        EventFormatter eventFormatter = new EventFormatter(mContext);
        String date = eventFormatter.formatDay(eventEntry) + " "
                + eventFormatter.formatMonth(eventEntry) + " " + eventFormatter.formatTime(eventEntry);
        holder.mDateTextView.setText(date);
        Picasso.with(mContext)
                .load(type == ReelFragment.TypeFormat.favorites.nativeInt ? eventEntry.getImageHorizontalUrl() : eventEntry.getImageVerticalUrl())
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
    public void onViewRecycled(EventHolder holder) {
        super.onViewRecycled(holder);
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
            if(v instanceof CardView){
                Intent intent = new Intent(mContext, EventDetailActivity.class);
                intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());
                if(type == ReelFragment.TypeFormat.organization.nativeInt)
                    intent.putExtra(EventDetailActivity.IS_LOCAL, true);
                mContext.startActivity(intent);
            }
        }

    }
}