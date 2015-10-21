package ru.getlect.evendate.evendate.sync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import com.google.android.gms.plus.model.people.Person;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
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
                String format = Utils.getFileExtension(eventEntry.getImageHorizontalUrl());
                String filepath = EvendateContract.PATH_EVENT_IMAGES + "/" + Integer.toString(eventEntry.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(eventEntry.getImageHorizontalUrl());
                ImageLoader.loadImage(filepath, url, Bitmap.CompressFormat.valueOf(format));
            }
        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "error parsing image url");
            throw e;
        }
        for (int i = 0; i < eventImagesMap.size(); i++) {
            Log.i(LOG_TAG, "delete image");
            File file = eventImagesMap.get(i);
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
                String format = Utils.getFileExtension(organizationEntry.getBackgroundImgUrl());
                String filepath = EvendateContract.PATH_ORGANIZATION_IMAGES + "/" + Integer.toString(organizationEntry.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(organizationEntry.getBackgroundImgUrl());
                ImageLoader.loadImage(filepath, url, Bitmap.CompressFormat.valueOf(format));
            }
        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "error parsing image url");
            throw e;
        }
        for (int i = 0; i < organizationImagesMap.size(); i++) {
            Log.i(LOG_TAG, "delete image");
            File file = organizationImagesMap.get(i);
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
                String format = Utils.getFileExtension(organizationEntry.getBackgroundImgUrl());
                String filepath = EvendateContract.PATH_ORGANIZATION_LOGOS + "/" + Integer.toString(organizationEntry.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(organizationEntry.getLogoUrl());
                ImageLoader.loadImage(filepath, url, Bitmap.CompressFormat.valueOf(format));
            }
        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "error parsing image url");
            throw e;
        }
        for (int i = 0; i < organizationLogosMap.size(); i++) {
            Log.i(LOG_TAG, "delete image");
            File file = organizationLogosMap.get(i);
            file.delete();
        }
        Log.i(LOG_TAG, "images sync ended");
    }
}