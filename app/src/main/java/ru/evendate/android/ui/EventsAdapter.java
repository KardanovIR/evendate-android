package ru.evendate.android.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.Event;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.ui.feed.ReelFragment;
import ru.evendate.android.ui.utils.EventFormatter;

public class EventsAdapter extends AbstractAdapter<Event, EventsAdapter.EventHolder> {
    private String LOG_TAG = EventsAdapter.class.getSimpleName();

    private Context mContext;
    private final EventsInteractionListener mListener;
    private int type;

    public EventsAdapter(@NonNull Context context, int type, @NonNull EventsInteractionListener listener) {
        this.type = type;
        mContext = context;
        mListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        int layoutItemId;
        switch (ReelFragment.ReelType.getType(type)) {
            case ORGANIZATION:
                layoutItemId = R.layout.card_event_feed;
                break;
            case FAVORITES:
                layoutItemId = R.layout.card_event_feed;
                break;
            case SEARCH:
            case CALENDAR:
                layoutItemId = R.layout.card_event;
                break;
            case RECOMMENDATION:
                layoutItemId = R.layout.card_event_feed;
                break;
            default:
                layoutItemId = R.layout.card_event_feed;
        }
        return layoutItemId;
    }

    @Override
    public EventHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new EventHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(final EventHolder holder, int position) {
        Event eventEntry = getItem(position);
        holder.event = eventEntry;
        holder.mTitleTextView.setText(eventEntry.getTitle());
        if (holder.mOrganizationTextView != null)
            holder.mOrganizationTextView.setText(eventEntry.getOrganizationShortName());
        holder.isFavorited = eventEntry.isFavorite();
        holder.isHidden = eventEntry.isHidden();
        if (eventEntry.isFavorite()) {
            holder.mFavoriteIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.mFavoriteIndicator.setVisibility(View.INVISIBLE);
        }
        String date;
        date = EventFormatter.formatDate(EventFormatter.getNearestDateTime(eventEntry));
        holder.mDateTextView.setText(date);
        String eventBackGroundUrl = ServiceUtils.constructEventBackgroundURL(
                eventEntry.getImageHorizontalUrl(),
                (int)mContext.getResources().getDimension(R.dimen.event_background_width));
        Picasso.with(mContext)
                .load(eventBackGroundUrl)
                .error(R.drawable.default_background)
                .into(holder.mEventImageView);

        if (type == ReelFragment.ReelType.CALENDAR.type())
            return;
        if (holder.mOrganizationLogo != null)
            Picasso.with(mContext)
                    .load(eventEntry.getOrganizationLogoSmallUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.mOrganizationLogo);
        if (holder.mPriceTextView != null)
            holder.mPriceTextView.setText(eventEntry.isFree() ?
                    holder.eventFreeLabel : EventFormatter.formatPrice(mContext, eventEntry.getMinPrice()));
    }


    @Override
    public void onViewRecycled(EventHolder holder) {
        super.onViewRecycled(holder);
        if (holder.mFavoriteIndicator != null)
            holder.mFavoriteIndicator.setVisibility(View.INVISIBLE);
    }

    public class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        View holderView;
        Event event;
        @BindView(R.id.event_item_image) ImageView mEventImageView;
        @BindView(R.id.event_item_title) TextView mTitleTextView;
        @BindView(R.id.event_item_date) TextView mDateTextView;
        @Nullable @BindView(R.id.event_item_price) TextView mPriceTextView;
        @Nullable @BindView(R.id.event_item_organization) TextView mOrganizationTextView;
        @Nullable @BindView(R.id.event_item_organization_icon) ImageView mOrganizationLogo;
        @BindView(R.id.event_item_favorite_indicator) View mFavoriteIndicator;
        @BindString(R.string.event_free) String eventFreeLabel;
        private boolean isFavorited;
        private boolean isHidden;

        EventHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);
            holderView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                mListener.openEvent(event);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            final int HIDE_ID = 0;
            final int FAVE_ID = 1;
            final int INVITE_ID = 2;
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mTitleTextView.getText())
                    .setItems(getDialogTextItems(), (DialogInterface dialog, int which) -> {
                        String toastText = mContext.getString(R.string.toast_event) +
                                " «" + mTitleTextView.getText() + "» ";
                        switch (which) {
                            case HIDE_ID:
                                hideEvent();
                                if (isHidden) {
                                    toastText += mContext.getString(R.string.toast_event_unhide);
                                } else {
                                    toastText += mContext.getString(R.string.toast_event_hide);
                                }
                                break;
                            case FAVE_ID:
                                mListener.likeEvent(event);
                                if (isFavorited) {
                                    toastText += mContext.getString(R.string.toast_event_unfave);
                                } else {
                                    toastText += mContext.getString(R.string.toast_event_fave);
                                }
                                update(event);
                                break;
                            case INVITE_ID:
                                break;
                        }
                        Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
                    });
            builder.create().show();
            return true;
        }

        private void hideEvent() {
            mListener.hideEvent(event);
            switch (ReelFragment.ReelType.getType(type)) {
                case ORGANIZATION:
                    update(event);
                    break;
                case FAVORITES:
                    remove(event);
                    break;
                case CALENDAR:
                    remove(event);
                    break;
                case RECOMMENDATION:
                    remove(event);
                    break;
                default:
                    remove(event);
            }
        }

        private CharSequence[] getDialogTextItems() {
            String fave = isFavorited ? mContext.getString(R.string.dialog_event_unfave) :
                    mContext.getString(R.string.dialog_event_fave);
            String hidden = isHidden ? mContext.getString(R.string.dialog_event_unhide) :
                    mContext.getString(R.string.dialog_event_hide);
            return new CharSequence[]{
                    hidden,
                    fave,
                    //mContext.getString(R.string.dialog_event_invite_friend)
            };
        }
    }

    public interface EventsInteractionListener {
        void openEvent(Event event);

        void likeEvent(Event event);

        void hideEvent(Event event);
    }
}