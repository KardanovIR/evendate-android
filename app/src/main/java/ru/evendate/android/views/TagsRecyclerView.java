package ru.evendate.android.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager;
import com.beloo.widget.chipslayoutmanager.SpacingItemDecoration;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.evendate.android.R;
import ru.evendate.android.models.Tag;
import ru.evendate.android.ui.AbstractAdapter;

/**
 * Created by Dmitry on 07.02.2017.
 */

public class TagsRecyclerView extends RecyclerView {
    private OnTagClickListener listener;
    private TagAdapter mAdapter;
    private ChipsLayoutManager mSpanLayoutManager;
    private boolean isTouchable = true;
    private boolean isClickable = true;

    public TagsRecyclerView(Context context) {
        this(context, null);
    }

    public TagsRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagsRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        addItemDecoration(new SpacingItemDecoration(getResources().getDimensionPixelOffset(R.dimen.item_tag_space),
                getResources().getDimensionPixelOffset(R.dimen.item_tag_space)));
    }

    public void setTags(ArrayList<Tag> tags) {
        setTags(tags, ChipsLayoutManager.STRATEGY_DEFAULT);
    }

    //todo SOLID
    public void setTags(ArrayList<Tag> tags, int strategy) {
        mAdapter = new TagAdapter(tags);
        mSpanLayoutManager = ChipsLayoutManager.newBuilder(getContext())
                .setOrientation(ChipsLayoutManager.HORIZONTAL)
                .setScrollingEnabled(false)
                .setRowStrategy(strategy)
                .build();
        setLayoutManager(mSpanLayoutManager);
        setAdapter(mAdapter);
        setClipToPadding(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        return isTouchable;
    }

    public void setTouchable(boolean touchable) {
        isTouchable = touchable;
    }

    @Override
    public void setClickable(boolean clickable) {
        super.setClickable(clickable);
        isClickable = clickable;
    }

    public void setOnTagClickListener(OnTagClickListener listener) {
        this.listener = listener;
    }

    private class TagAdapter extends AbstractAdapter<Tag, TagHolder> {

        TagAdapter(ArrayList<Tag> mTags) {
            set(mTags);
        }

        @Override
        public TagHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new TagHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_tag, parent, false));
        }

        @Override
        public void onBindViewHolder(TagHolder holder, int position) {
            Tag tag = getItem(position);
            holder.id = tag.getEntryId();
            holder.mTitle.setText(tag.getName());
            holder.holderView.setClickable(isClickable);
        }

    }

    class TagHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View holderView;
        @BindView(R.id.title) TextView mTitle;
        public int id;

        TagHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            holderView = itemView;
            holderView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == holderView) {
                if (listener != null)
                    listener.onTagClicked((String)mTitle.getText());
            }
        }

    }

    public interface OnTagClickListener {
        void onTagClicked(String tag);
    }
}
