package ru.evendate.android.adapters;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
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

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import ru.evendate.android.EvendateAccountManager;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.models.EventFeed;
import ru.evendate.android.models.EventFormatter;
import ru.evendate.android.network.ApiFactory;
import ru.evendate.android.network.ApiService;
import ru.evendate.android.network.Response;
import ru.evendate.android.ui.EventDetailActivity;
import ru.evendate.android.ui.ReelFragment;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Dmitry on 01.12.2015.
 */

public class EventsAdapter extends AppendableAdapter<EventFeed> {
    private String LOG_TAG = EventsAdapter.class.getSimpleName();

    private int type;
    public static Uri mUri = EvendateContract.EventEntry.CONTENT_URI;


    public EventsAdapter(Context context, RecyclerView recyclerView, int type) {
        super(context, recyclerView);
        this.type = type;
    }

    @Override
    public int getItemViewType(int position) {
        int layoutItemId;
        switch (ReelFragment.ReelType.getType(type)){
            case ORGANIZATION:
                layoutItemId = R.layout.card_event_organization;
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
        if(isLoading() && position == super.getItemCount() - 1)
            layoutItemId = AppendableAdapter.PROGRESS_VIEW_TYPE;
        return layoutItemId;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType == AppendableAdapter.PROGRESS_VIEW_TYPE)
            return super.onCreateViewHolder(parent,viewType);
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
        if (eventEntry.isFavorite())
            holder.mFavoriteIndicator.setVisibility(View.VISIBLE);
        String date = EventFormatter.formatDate(eventEntry.getNearestDate());
        holder.mDateTextView.setText(date);
        Picasso.with(mContext)
                .load(eventEntry.getImageHorizontalUrl())
                .error(R.drawable.default_background)
                .into(holder.mEventImageView);

        if (type == ReelFragment.ReelType.CALENDAR.type())
            return;
        if (holder.mOrganizationLogo != null)
            Picasso.with(mContext)
                    .load(eventEntry.getOrganizationLogoSmallUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.mOrganizationLogo);
        if(holder.mPriceTextView != null)
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

    public class EventHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public View holderView;
        public EventFeed event;
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
        private boolean isFavorited;
        @Bind(R.id.event_item_favorite_indicator)
        public View mFavoriteIndicator;
        @BindString(R.string.event_free)
        public String eventFreeLabel;

        public EventHolder(View itemView) {
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
                intent.setData(mUri.buildUpon().appendPath(Long.toString(event.getEntryId())).build());
                if(Build.VERSION.SDK_INT >= 21){
                    mContext.startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation((Activity)mContext).toBundle());
                }
                else
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
                    .setItems(getDialogTextItems(), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            String toastText = mContext.getString(R.string.toast_event) +
                                    " «" + mTitleTextView.getText() + "» ";
                            switch (which){
                                case HIDE_ID:
                                    hideEvent(event);
                                    toastText += mContext.getString(R.string.toast_event_hide);
                                    break;
                                case FAVE_ID:
                                    likeEvent(event);
                                    if(isFavorited) {
                                        toastText += mContext.getString(R.string.toast_event_unfave);
                                    } else {
                                        toastText += mContext.getString(R.string.toast_event_fave);
                                    }
                                    break;
                                case INVITE_ID:
                                    break;
                            }
                            Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
                        }
                    });
            builder.create().show();
            return true;
        }

        private CharSequence[] getDialogTextItems(){
            String fave = isFavorited ? mContext.getString(R.string.dialog_event_unfave) :
                    mContext.getString(R.string.dialog_event_fave);
            CharSequence[] items = {
                    mContext.getString(R.string.dialog_event_hide),
                    fave,
                    //mContext.getString(R.string.dialog_event_invite_friend)
            };
            return items;
        }
    }


    private void hideEvent(EventFeed event){
        ApiService apiService = ApiFactory.getService(mContext);
        Observable<Response> hideObservable =
                apiService.hideEvent(EvendateAccountManager.peekToken(mContext),
                        event.getEntryId(), true);
        Log.i(LOG_TAG, "hiding event " + event.getEntryId());
        hideObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> Log.i(LOG_TAG, "event hided"),
                        error -> Log.e(LOG_TAG, error.getMessage())
                );
        remove(event);
    }

    private void likeEvent(EventFeed event){
        ApiService apiService = ApiFactory.getService(mContext);
        Observable<Response> likeObservable;
        int id = event.getEntryId();
        if(event.isFavorite()) {
            likeObservable = apiService.dislikeEvent(id, EvendateAccountManager.peekToken(mContext));
            Log.i(LOG_TAG, "disliking event " + id);
        } else {
            likeObservable = apiService.likeEvent(id, EvendateAccountManager.peekToken(mContext));
            Log.i(LOG_TAG, "liking event " + id);
        }

        likeObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        result -> Log.i(LOG_TAG, "event liked/disliked"),
                        error -> Log.e(LOG_TAG, error.getMessage())
                );

        event.setIsFavorite(!event.isFavorite());
        update(event);
    }
}