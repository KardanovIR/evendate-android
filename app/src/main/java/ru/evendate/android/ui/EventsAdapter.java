package ru.evendate.android.ui;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import io.reactivex.Observable;
import ru.evendate.android.R;
import ru.evendate.android.data.DataRepository;
import ru.evendate.android.data.DataSource;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.Event;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.network.Response;
import ru.evendate.android.network.ServiceUtils;
import ru.evendate.android.ui.eventdetail.EventDetailActivity;
import ru.evendate.android.ui.utils.EventFormatter;

public class EventsAdapter extends AppendableAdapter<EventFeed> {
    private String LOG_TAG = EventsAdapter.class.getSimpleName();

    private int type;

    public EventsAdapter(Context context, RecyclerView recyclerView, int type) {
        super(context, recyclerView);
        this.type = type;
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
            case CALENDAR:
                layoutItemId = R.layout.card_event;
                break;
            case RECOMMENDATION:
                layoutItemId = R.layout.card_event_feed;
                break;
            default:
                layoutItemId = R.layout.card_event_feed;
        }
        if (isLoading() && position == super.getItemCount() - 1)
            layoutItemId = AppendableAdapter.PROGRESS_VIEW_TYPE;
        return layoutItemId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == AppendableAdapter.PROGRESS_VIEW_TYPE)
            return super.onCreateViewHolder(parent, viewType);
        return new EventHolder(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        super.onBindViewHolder(viewHolder, position);
        if (!(viewHolder instanceof EventHolder))
            return;
        EventFeed eventEntry = getItem(position);
        EventHolder holder = (EventHolder)viewHolder;
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
        date = EventFormatter.formatDate(EventFormatter.getNearestDateTime((Event)eventEntry));
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
    public void onViewRecycled(RecyclerView.ViewHolder viewHolder) {
        super.onViewRecycled(viewHolder);
        if (!(viewHolder instanceof EventHolder))
            return;
        EventHolder holder = (EventHolder)viewHolder;
        if (holder.mFavoriteIndicator != null)
            holder.mFavoriteIndicator.setVisibility(View.INVISIBLE);
    }

    private void hideEvent(EventFeed event) {
        DataSource dataRepository = new DataRepository(mContext);
        if (!event.isHidden()) {
            dataRepository.hideEvent(event.getEntryId()).subscribe(result -> {
                        if (result.isOk()) {
                            Log.i(LOG_TAG, "performed hide");
                        } else
                            Log.e(LOG_TAG, "Error with response with hide");
                    }, error -> Log.e(LOG_TAG, "" + error.getMessage())
            );
        } else {
            dataRepository.unhideEvent(event.getEntryId()).subscribe(result -> {
                        if (result.isOk()) {
                            Log.i(LOG_TAG, "performed unhide");
                        } else
                            Log.e(LOG_TAG, "Error with response with unhide");
                    }, error -> Log.e(LOG_TAG, "" + error.getMessage())
            );
        }
        event.setHidden(!event.isHidden());
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

    //todo SOLID
    private void likeEvent(EventFeed event) {
        DataSource dataSource = new DataRepository(mContext);
        Observable<Response> likeEventObservable;
        int id = event.getEntryId();
        if (event.isFavorite()) {
            likeEventObservable = dataSource.unfaveEvent(id);
            Log.i(LOG_TAG, "disliking event " + id);
        } else {
            likeEventObservable = dataSource.faveEvent(id);
            Log.i(LOG_TAG, "liking event " + id);
        }

        likeEventObservable.subscribe(
                result -> {
                    if (result.isOk())
                        Log.i(LOG_TAG, "performed like");
                    else
                        Log.e(LOG_TAG, "Error with response with like");
                }, error -> Log.e(LOG_TAG, "" + error.getMessage())
        );

        event.setIsFavorite(!event.isFavorite());
        update(event);
    }

    class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        View holderView;
        EventFeed event;
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
                Intent intent = new Intent(mContext, EventDetailActivity.class);
                intent.setData(EvendateContract.EventEntry.getContentUri(event.getEntryId()));
                if (Build.VERSION.SDK_INT >= 21) {
                    mContext.startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation((Activity)mContext).toBundle());
                } else
                    mContext.startActivity(intent);
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
                                hideEvent(event);
                                if (isHidden) {
                                    toastText += mContext.getString(R.string.toast_event_unhide);
                                } else {
                                    toastText += mContext.getString(R.string.toast_event_hide);
                                }
                                break;
                            case FAVE_ID:
                                likeEvent(event);
                                if (isFavorited) {
                                    toastText += mContext.getString(R.string.toast_event_unfave);
                                } else {
                                    toastText += mContext.getString(R.string.toast_event_fave);
                                }
                                break;
                            case INVITE_ID:
                                break;
                        }
                        Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
                    });
            builder.create().show();
            return true;
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
}