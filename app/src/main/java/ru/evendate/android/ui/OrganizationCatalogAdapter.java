package ru.evendate.android.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.evendate.android.EvendateApplication;
import ru.evendate.android.R;
import ru.evendate.android.data.EvendateContract;
import ru.evendate.android.sync.models.OrganizationModel;

/**
 * Created by Dmitry on 01.12.2015.
 * adapter for organization model into catalog item
 */
public class OrganizationCatalogAdapter extends RecyclerView.Adapter<OrganizationCatalogAdapter.OrganizationHolder>{

    Context mContext;
    ArrayList<OrganizationModel> mOrganizationList;
    private Uri mUri = EvendateContract.OrganizationEntry.CONTENT_URI;

    public OrganizationCatalogAdapter(Context context){
        this.mContext = context;
    }

    public void setOrganizationList(ArrayList<OrganizationModel> organizationList){
        mOrganizationList = organizationList;
        notifyDataSetChanged();
    }

    @Override
    public OrganizationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new OrganizationHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.organization_catalog_little_item, parent, false));
    }

    @Override
    public void onBindViewHolder(OrganizationHolder holder, int position) {
        if (mOrganizationList != null) {
            OrganizationModel organizationEntry = mOrganizationList.get(position);
            holder.id = organizationEntry.getEntryId();
            holder.mTitle.setText(organizationEntry.getShortName());
            String subs = organizationEntry.getSubscribedCount() + " " +
                    mContext.getResources().getString(R.string.organization_subscribers);
            holder.mSubCounts.setText(subs);
            Picasso.with(mContext)
                    .load(organizationEntry.getLogoSmallUrl())
                    .error(R.mipmap.ic_launcher)
                    .into(holder.mImageView);
        }
    }

    @Override
    public int getItemCount() {
        if (mOrganizationList == null) {
            return 0;
        } else {
            return mOrganizationList.size();
        }
    }


    public class OrganizationHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private View mItem;
        public TextView mTitle;
        public TextView mSubCounts;
        public ImageView mImageView;
        public long id;

        public OrganizationHolder(View itemView){
            super(itemView);
            mItem = itemView;
            mSubCounts = (TextView)itemView.findViewById(R.id.organization_item_subs);
            mTitle = (TextView)itemView.findViewById(R.id.item_title);
            mImageView = (ImageView)itemView.findViewById(R.id.organization_icon);
            this.id = (int)this.getItemId();
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if(v.equals(mItem)){
                Intent intent = new Intent(mContext, OrganizationDetailActivity.class);
                intent.setData(mUri.buildUpon().appendPath(Long.toString(id)).build());

                Tracker tracker = EvendateApplication.getTracker();
                HitBuilders.EventBuilder event = new HitBuilders.EventBuilder()
                        .setCategory(mContext.getString(R.string.stat_category_organization))
                        .setAction(mContext.getString(R.string.stat_action_view))
                        .setLabel((Long.toString(id)));
                tracker.send(event.build());

                mContext.startActivity(intent);
            }
        }
    }

    /**
     * layout manager for nested recycler view
     * http://stackoverflow.com/questions/26649406/nested-recycler-view-height-doesnt-wrap-its-content
     */
    public static class CatalogLinearLayoutManager extends LinearLayoutManager {

        public CatalogLinearLayoutManager(Context context, int orientation, boolean reverseLayout){
            super(context, orientation, reverseLayout);
        }

        private int[] mMeasuredDimension = new int[2];

        @Override
        public void onMeasure(RecyclerView.Recycler recycler, RecyclerView.State state,
                              int widthSpec, int heightSpec) {
            final int widthMode = View.MeasureSpec.getMode(widthSpec);
            final int heightMode = View.MeasureSpec.getMode(heightSpec);
            final int widthSize = View.MeasureSpec.getSize(widthSpec);
            final int heightSize = View.MeasureSpec.getSize(heightSpec);
            int width = 0;
            int height = 0;
            for (int i = 0; i < getItemCount(); i++) {
                measureScrapChild(recycler, i,
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        View.MeasureSpec.makeMeasureSpec(i, View.MeasureSpec.UNSPECIFIED),
                        mMeasuredDimension);

                if (getOrientation() == HORIZONTAL) {
                    width = width + mMeasuredDimension[0];
                    if (i == 0) {
                        height = mMeasuredDimension[1];
                    }
                } else {
                    height = height + mMeasuredDimension[1];
                    if (i == 0) {
                        width = mMeasuredDimension[0];
                    }
                }
            }
            switch (widthMode) {
                case View.MeasureSpec.EXACTLY:
                    width = widthSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            switch (heightMode) {
                case View.MeasureSpec.EXACTLY:
                    height = heightSize;
                case View.MeasureSpec.AT_MOST:
                case View.MeasureSpec.UNSPECIFIED:
            }

            setMeasuredDimension(width, height);
        }

        private void measureScrapChild(RecyclerView.Recycler recycler, int position, int widthSpec,
                                       int heightSpec, int[] measuredDimension) {
            View view = recycler.getViewForPosition(position);
            if (view != null) {
                RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
                int childWidthSpec = ViewGroup.getChildMeasureSpec(widthSpec,
                        getPaddingLeft() + getPaddingRight(), p.width);
                int childHeightSpec = ViewGroup.getChildMeasureSpec(heightSpec,
                        getPaddingTop() + getPaddingBottom(), p.height);
                view.measure(childWidthSpec, childHeightSpec);
                measuredDimension[0] = view.getMeasuredWidth() + p.leftMargin + p.rightMargin;
                measuredDimension[1] = view.getMeasuredHeight() + p.bottomMargin + p.topMargin;
                recycler.recycleView(view);
            }
        }
    }
}
