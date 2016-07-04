package ru.evendate.android.ui;

import android.util.Log;

import java.util.ArrayList;

import ru.evendate.android.adapters.AppendableAdapter;

/**
 * Created by Dmitry on 09.06.2016.
 */
public class AdapterController {
    private final String LOG_TAG = AdapterController.class.getSimpleName();

    public static final int EVENTS_LENGTH = 10;

    private boolean isDisable = false;
    private boolean isRequesting = false;
    public int length = EVENTS_LENGTH;
    private int offset = 0;
    private AppendableAdapter mAdapter;
    private AdapterContext mAdapterContext;

    public AdapterController(AdapterContext context, AppendableAdapter adapter) {
        mAdapter = adapter;
        mAdapterContext = context;
        mAdapter.setController(this);
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

    public int getLength() {
        return length;
    }

    public int getOffset() {
        return offset;
    }

    private void increaseOffset() {
        offset += length;
    }

    public void setLength(int length) {
        this.length = length;
    }


    public void reset() {
        offset = 0;
    }

    public void requestNext() {
        if(isDisable || isRequesting){
            Log.d(LOG_TAG, "requesting denied");
            return;
        }
        mAdapterContext.requestNext();
        isRequesting = true;
    }
    public void loaded(ArrayList list){
        checkNextShouldBeDisable(list.size());
        mAdapter.add(list);
        isRequesting = false;
        mAdapter.setLoaded();
        increaseOffset();
    }
    public void reloaded(ArrayList list){
        checkNextShouldBeDisable(list.size());
        mAdapter.replace(list);
        isRequesting = false;
        mAdapter.setLoaded();
        increaseOffset();
    }

    private void checkNextShouldBeDisable(int size){
        if (size < getLength()) {
            disableNext();
        }
        else
            isDisable = false;
    }

    public void loaded(){
        isRequesting = false;
        mAdapter.setLoaded();
        increaseOffset();
    }

    public void notLoadedCauseError(){
        isRequesting = false;
        mAdapter.setLoaded();
    }

    /**
     * Context that handle next request and delegate to loaders
     * primarily activity or fragment
     */
    public interface AdapterContext {
        void requestNext();
    }
}
