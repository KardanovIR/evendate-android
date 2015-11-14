package ru.getlect.evendate.evendate.sync;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.OrganizationEntry;
import ru.getlect.evendate.evendate.utils.Utils;

/**
 * Created by Dmitry on 18.10.2015.
 * Класс, отвечающий за синхронизацию картинок.
 */
public class ImageManager {
    String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();
    public LocalDataFetcher mLocalDataFetcher;

    public ImageManager(LocalDataFetcher localDataFetcher) {
        mLocalDataFetcher = localDataFetcher;
    }

    //TODO да-да, дублирование кода
    public void updateEventImages(ArrayList<DataEntry> eventDataList) throws IOException{
        HashMap<Integer,File> eventImagesMap = mLocalDataFetcher.getImages(EvendateContract.PATH_EVENT_IMAGES);

        Log.i(LOG_TAG, "images sync started");

        try {
            for (DataEntry entry : eventDataList) {
                EventEntry eventEntry = (EventEntry) entry;
                File match = eventImagesMap.get(eventEntry.getEntryId());
                if (match != null) {
                    eventImagesMap.remove(entry.getEntryId());
                    if (eventEntry.getUpdatedAt() <= match.lastModified())
                        continue;
                }
                if(eventEntry.getImageHorizontalUrl() == null)
                    continue;
                String format = Utils.getFileExtension(eventEntry.getImageHorizontalUrl());
                String filepath = EvendateContract.PATH_EVENT_IMAGES + "/" + Integer.toString(eventEntry.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(eventEntry.getImageHorizontalUrl());
                ImageServerLoader.loadImage(filepath, url, Bitmap.CompressFormat.valueOf(format));
            }
        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "error parsing image url");
            throw e;
        }
        for (File file : eventImagesMap.values()) {
            Log.i(LOG_TAG, "delete image");
            file.delete();
        }
        Log.i(LOG_TAG, "images sync ended");
    }
    public void updateOrganizationsImages(ArrayList<DataEntry> organizationDataList) throws IOException{
        HashMap<Integer,File> organizationImagesMap = mLocalDataFetcher.getImages(EvendateContract.PATH_ORGANIZATION_IMAGES);

        Log.i(LOG_TAG, "images sync started");

        try {
            for (DataEntry entry : organizationDataList) {
                OrganizationEntry organizationEntry = (OrganizationEntry) entry;
                File match = organizationImagesMap.get(organizationEntry.getEntryId());
                if (match != null) {
                    organizationImagesMap.remove(entry.getEntryId());
                    if (organizationEntry.updatedAt() <= match.lastModified())
                        continue;
                }
                if(organizationEntry.getBackgroundImgUrl() == null)
                    continue;
                String format = Utils.getFileExtension(organizationEntry.getBackgroundImgUrl());
                String filepath = EvendateContract.PATH_ORGANIZATION_IMAGES + "/" + Integer.toString(organizationEntry.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(organizationEntry.getBackgroundImgUrl());
                ImageServerLoader.loadImage(filepath, url, Bitmap.CompressFormat.valueOf(format));
            }
        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "error parsing image url");
            throw e;
        }
        for (File file : organizationImagesMap.values()) {
            Log.i(LOG_TAG, "delete image");
            file.delete();
        }
        Log.i(LOG_TAG, "images sync ended");
    }
    public void updateOrganizationsLogos(ArrayList<DataEntry> organizationDataList) throws IOException{
        HashMap<Integer,File> organizationLogosMap = mLocalDataFetcher.getImages(EvendateContract.PATH_ORGANIZATION_LOGOS);

        Log.i(LOG_TAG, "images sync started");

        try {
            for (DataEntry entry : organizationDataList) {
                OrganizationEntry organizationEntry = (OrganizationEntry) entry;
                File match = organizationLogosMap.get(organizationEntry.getEntryId());
                if (match != null) {
                    organizationLogosMap.remove(entry.getEntryId());
                    if (organizationEntry.updatedAt() <= match.lastModified())
                        continue;
                }
                if(organizationEntry.getLogoUrl() == null)
                    continue;
                String format = Utils.getFileExtension(organizationEntry.getLogoUrl());
                String filepath = EvendateContract.PATH_ORGANIZATION_LOGOS + "/" + Integer.toString(organizationEntry.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(organizationEntry.getLogoUrl());
                ImageServerLoader.loadImage(filepath, url, Bitmap.CompressFormat.valueOf(format));
            }
        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "error parsing image url");
            throw e;
        }
        for (File file : organizationLogosMap.values()) {
            Log.i(LOG_TAG, "delete image");
            file.delete();
        }
        Log.i(LOG_TAG, "images sync ended");
    }
}