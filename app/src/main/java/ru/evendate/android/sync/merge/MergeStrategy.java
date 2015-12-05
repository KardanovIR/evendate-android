package ru.evendate.android.sync.merge;

import android.content.ContentResolver;
import android.net.Uri;

import java.util.ArrayList;

import ru.evendate.android.sync.models.DataModel;

/**
 * Created by Dmitry on 11.09.2015.
 */
public abstract class MergeStrategy {
    protected ContentResolver mContentResolver;

    public MergeStrategy(ContentResolver contentResolver) {
        mContentResolver = contentResolver;
    }
    public abstract void mergeData(final Uri ContentUri, ArrayList<DataModel> cloudList, ArrayList<DataModel> localList);
}