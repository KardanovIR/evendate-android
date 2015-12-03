package ru.getlect.evendate.evendate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.ImageLoaderTask;
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
            layoutItemId = R.layout.reel_item;
        } else if(type == ReelFragment.TypeFormat.calendar.nativeInt){
            layoutItemId = R.layout.reel_item_little;
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
        holder.mOrganizationTextView.setText(eventEntry.getOrganizationShortName());
        if(eventEntry.isFavorite() && type != ReelFragment.TypeFormat.favorites.nativeInt){
            holder.mFavoriteIndicator.setVisibility(View.VISIBLE);
        }
        setupTime(eventEntry, holder);
        setupImage(eventEntry, holder);
    }

    private void setupTime(EventModel eventEntry, EventHolder holder){
        String time;
        if(eventEntry.isFullDay())
            time = mContext.getResources().getString(R.string.event_all_day);
        else{
            //cut off seconds
            //TODO temporary
            time = "";
            if(eventEntry.getBeginTime() != null && eventEntry.getEndTime() != null)
                time = eventEntry.getBeginTime().substring(0, 5) + " - " + eventEntry.getEndTime().substring(0, 5);
        }
        Date date = eventEntry.getActialDate();
        DateFormat[] formats = new DateFormat[] {
                new SimpleDateFormat("dd", Locale.getDefault()),
                new SimpleDateFormat("MMMM", Locale.getDefault()),
        };
        if(date != null){
            String day = formats[0].format(date);
            if(day.substring(0, 1).equals("0"))
                day = day.substring(1);
            String dateString = day + " " + formats[1].format(date) + " " + time;
            holder.mDateTextView.setText(dateString);
        }
    }
    private void setupImage(EventModel eventEntry, EventHolder holder){
        holder.mEventImageView.setImageBitmap(null);
        if(type == ReelFragment.TypeFormat.organization.nativeInt){
            if(eventEntry.getImageHorizontalUrl() == null)
                holder.mEventImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.butterfly));
            else{
                ImageLoaderTask imageLoader = new ImageLoaderTask(holder.mEventImageView);
                imageLoader.execute(eventEntry.getImageHorizontalUrl());
            }
        }
        else{
            try {
                final ParcelFileDescriptor fileDescriptor = mContext.getContentResolver()
                        .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                        .appendPath("images").appendPath("events")
                                        .appendPath(String.valueOf(eventEntry.getEntryId())
                                        ).build(), "r"
                        );
                if(fileDescriptor == null)
                    //заглушка на случай отсутствия картинки
                    holder.mEventImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.default_background));
                else {
                    ImageLoadingTask imageLoadingTask = new ImageLoadingTask(holder.mEventImageView);
                    imageLoadingTask.execute(fileDescriptor);
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }
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