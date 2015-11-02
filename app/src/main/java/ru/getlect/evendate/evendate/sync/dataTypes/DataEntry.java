package ru.getlect.evendate.evendate.sync.dataTypes;

import android.content.ContentProviderOperation;
import android.net.Uri;

/**
 * Created by Dmitry on 10.09.2015.
 */
public abstract class DataEntry extends ResponseData {
    private int _id;
    public int getId(){
        return this._id;
    }
    public void setId(int id){
        this._id = id;
    }
    public abstract int getEntryId();
    public abstract boolean equals(Object obj);
    public abstract ContentProviderOperation getUpdate(final Uri ContentUri);
    public abstract ContentProviderOperation getInsert(final Uri ContentUri);
}

