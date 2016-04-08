package ru.evendate.android.models;

import android.net.Uri;

/**
 * Created by Dmitry on 22.02.2016.
 */
public interface ActionTarget {
    int TYPE_ORGANIZATION = 1;
    int TYPE_EVENT = 2;

    String getTargetName();

    Uri getTargetUri();

    String getTargetImageLink();

    int getTargetType();
}
