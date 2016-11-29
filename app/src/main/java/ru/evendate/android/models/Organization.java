package ru.evendate.android.models;

/**
 * Created by Aedirn on 16.10.16.
 */

interface Organization {

    int getEntryId();

    String getName();

    int getTypeId();

    String getShortName();

    String getLogoLargeUrl();

    String getBackgroundLargeUrl();

    String getTypeName();

    String getTypeOrder();
}
