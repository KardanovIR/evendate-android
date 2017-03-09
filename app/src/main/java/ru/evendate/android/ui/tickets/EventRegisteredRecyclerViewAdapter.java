package ru.evendate.android.ui.tickets;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.adapters.AbstractEndlessAdapter;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.models.EventRegistered;
import ru.evendate.android.ui.FormatUtils;

/**
 * Created by Aedirn on 07.03.17.
 */

public class EventRegisteredRecyclerViewAdapter extends AbstractEndlessAdapter<EventRegistered,
        EventRegisteredRecyclerViewAdapter.EventRegisteredViewHolder> {

    private Context mContext;
    private final EventRegisteredListFragment.OnEventInteractionListener mListener;

    EventRegisteredRecyclerViewAdapter(Context context, EventRegisteredListFragment.OnEventInteractionListener listener) {
        mListener = listener;
        mContext = context;
    }

    @Override
    public EventRegisteredViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_ticket, parent, false);
        return new EventRegisteredViewHolder(view);
    }

    @Override
    public void onViewRecycled(EventRegisteredViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mTicketCount.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onBindViewHolder(final EventRegisteredViewHolder holder, int position) {
        EventRegistered event = getItem(position);
        holder.mTitle.setText(event.getTitle());
        holder.mDatetime.setText(EventFormatter.formatDate(event.getNearestDate()));
        holder.mPlace.setText(event.getLocation());
        holder.mEvent = event;
        if (event.getTicketsCount() > 1) {
            holder.mTicketCount.setText(
                    TicketFormatter.formatTicketCount(FormatUtils.getCurrentLocale(mContext),
                            event.getTicketsCount(), mContext.getString(R.string.ticket_count_label)));
            holder.mTicketCount.setVisibility(View.VISIBLE);
        }

        holder.holderView.setOnClickListener((View v) -> {
            if (null != mListener) {
                mListener.onEventClick(holder.mEvent);
            }
        });
        holder.holderView.setOnLongClickListener((View v) -> {
            if (null != mListener) {
                mListener.onEventLongClick(holder.mEvent);
            }
            return true;
        });
    }

    class EventRegisteredViewHolder extends RecyclerView.ViewHolder {
        View holderView;
        @Bind(R.id.hint) TextView mHint;
        @Bind(R.id.title) TextView mTitle;
        @Bind(R.id.datetime) TextView mDatetime;
        @Bind(R.id.place) TextView mPlace;
        @Bind(R.id.ticket_count) TextView mTicketCount;

        @Nullable EventRegistered mEvent;

        EventRegisteredViewHolder(View view) {
            super(view);
            holderView = itemView;
            ButterKnife.bind(this, view);
        }
    }
}
