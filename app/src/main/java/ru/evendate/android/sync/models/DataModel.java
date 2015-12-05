package ru.evendate.android.sync.models;

import android.content.ContentProviderOperation;
import android.net.Uri;
import android.provider.BaseColumns;

import org.chalup.microorm.annotations.Column;

/**
 * Created by Dmitry on 10.09.2015.
 */
public abstract class DataModel extends ResponseData {
    @Column(BaseColumns._ID)
    private int _id;
    public int getId(){
        return this._id;
    }
    public void setId(int id){
        this._id = id;
    }
    public abstract int getEntryId();
    public abstract boolean equals(Object obj);
    public ContentProviderOperation getUpdate(final Uri ContentUri) {
        return fillWithData(ContentProviderOperation.newUpdate(ContentUri))
                .build();
    }
    public abstract ContentProviderOperation getInsert(final Uri ContentUri);
    protected abstract ContentProviderOperation.Builder fillWithData(ContentProviderOperation.Builder operation);
}

