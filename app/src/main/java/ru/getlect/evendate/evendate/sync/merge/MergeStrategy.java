package ru.getlect.evendate.evendate.sync.merge;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.ArrayList;

import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;

/**
 * Created by Dmitry on 11.09.2015.
 */
public abstract class MergeStrategy {
    protected ContentResolver mContentResolver;

    public MergeStrategy(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }
    public abstract void mergeData(final Uri ContentUri, ArrayList<DataEntry> cloudList, ArrayList<DataEntry> localList);
}
