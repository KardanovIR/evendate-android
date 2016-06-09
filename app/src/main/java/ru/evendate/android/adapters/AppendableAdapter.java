package ru.evendate.android.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 31.03.2016.
 */
public abstract class AppendableAdapter<T> extends AbstractAdapter<T, RecyclerView.ViewHolder> {
    private final String LOG_TAG = AppendableAdapter.class.getSimpleName();

    private AdapterController mController;
    private boolean isDisable = false;
    private boolean isRequesting = false;

    public AppendableAdapter(Context context, AdapterController controller) {
        super(context);
        mController = controller;
    }

    /**
     * disable starting loading next batch when user scroll to last item of list
     */
    public void disableNext() {
        isDisable = true;
    }

    /**
     * enable starting loading next batch when user scroll to last item of list
     */
    public void enableNext() {
        isDisable = false;
    }

    public boolean isRequesting() {
        return isRequesting;
    }

    public boolean isDisable() {
        return isDisable;
    }

    /**
     * request next batch of items from controller
     */
    protected void onLastReached() {
        if (isDisable)
            return;
        Log.d(LOG_TAG, "request next");
        mController.requestNext();
        isRequesting = true;
    }

    /**
     * clear adapter item list
     */
    public void reset() {
        setList(null);
    }

    /**
     * set items list if it's null or append items to exist list
     *
     * @param list item list
     */
    @Override
    public void setList(ArrayList<T> list) {
        isRequesting = false;
        if (getList() == null || list == null) {
            super.setList(list);
            notifyDataSetChanged();
        } else {
            for (T item : list)
                add(item, getList().size());
        }
    }

    private void add(T item, int position) {
        getList().add(position, item);
        //TODO потенциальная проблема
        notifyItemInserted(getItemCount());
    }

    protected void remove(T item) {
        int position = getList().indexOf(item);
        getList().remove(position);
        notifyItemRemoved(position);
    }

    /**
     * Context that handle next request and delegate to loaders
     * primarily activity or fragment
     */
    public interface AdapterController {
        void requestNext();
    }
}
