/**
 * Created by Dmitry on 01.12.2015.
 */
package ru.getlect.evendate.evendate;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;

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
            setupImage(organizationEntry, holder);
        }
    }

    private void setupImage(OrganizationModel organizationEntry, OrganizationHolder holder){
        holder.mImageView.setImageBitmap(null);
        ContentResolver contentResolver = mContext.getContentResolver();
        try {
            final ParcelFileDescriptor fileDescriptor = contentResolver
                    .openFileDescriptor(EvendateContract.BASE_CONTENT_URI.buildUpon()
                                    .appendPath("images").appendPath("organizations").appendPath("logos")
                                    .appendPath(String.valueOf(organizationEntry.getEntryId())
                                    ).build(), "r"
                    );
            if(fileDescriptor == null)
                //заглушка на случай отсутствия картинки
                holder.mImageView.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.ic_launcher));
            else {
                ImageLoadingTask imageLoadingTask = new ImageLoadingTask(holder.mImageView);
                imageLoadingTask.execute(fileDescriptor);
            }
        }catch (IOException e){
            e.printStackTrace();
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
        public TextView mSubTitle;
        public ImageView mImageView;
        public long id;

        public OrganizationHolder(View itemView){
            super(itemView);
            mItem = itemView;
            mSubTitle = (TextView)itemView.findViewById(R.id.item_subtitle);
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