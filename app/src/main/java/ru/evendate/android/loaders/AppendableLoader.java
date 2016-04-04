package ru.evendate.android.loaders;

import android.content.Context;

import java.util.ArrayList;

/**
 * Created by ds_gordeev on 30.03.2016.
 */
public abstract class AppendableLoader<D> extends AbstractLoader<D> {
    private int length = 10;
    private int offset = 0;


    public AppendableLoader(Context context) {
        super(context);
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

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    protected void onLoaded(ArrayList<D> data) {
        super.onLoaded(data);
        increaseOffset();
    }

    public void reset() {
        offset = 0;
    }
}