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
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;
import ru.getlect.evendate.evendate.sync.models.FriendModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;
import ru.getlect.evendate.evendate.utils.Utils;

/**
 * Created by Dmitry on 18.10.2015.
 * Класс, отвечающий за синхронизацию картинок.
 */
public class ImageManager {
    private static String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();
    public LocalDataFetcher mLocalDataFetcher;

    public ImageManager(LocalDataFetcher localDataFetcher) {
        mLocalDataFetcher = localDataFetcher;
    }

    //TODO да-да, дублирование кода
    public void updateEventImages(ArrayList<DataModel> eventDataList) throws IOException{
        HashMap<Integer,File> eventImagesMap = mLocalDataFetcher.getImages(EvendateContract.PATH_EVENT_IMAGES);

        Log.i(LOG_TAG, "images sync started");

        try {
            for (DataModel entry : eventDataList) {
                EventModel eventEntry = (EventModel) entry;
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
    public void updateOrganizationsImages(ArrayList<DataModel> organizationDataList) throws IOException{
        HashMap<Integer,File> organizationImagesMap = mLocalDataFetcher.getImages(EvendateContract.PATH_ORGANIZATION_IMAGES);

        Log.i(LOG_TAG, "images sync started");

        try {
            for (DataModel entry : organizationDataList) {
                OrganizationModel organizationModel = (OrganizationModel) entry;
                File match = organizationImagesMap.get(organizationModel.getEntryId());
                if (match != null) {
                    organizationImagesMap.remove(entry.getEntryId());
                    if (organizationModel.updatedAt() <= match.lastModified())
                        continue;
                }
                if(organizationModel.getBackgroundMediumUrl() == null)
                    continue;
                String format = Utils.getFileExtension(organizationModel.getBackgroundMediumUrl());
                String filepath = EvendateContract.PATH_ORGANIZATION_IMAGES + "/" + Integer.toString(organizationModel.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(organizationModel.getBackgroundMediumUrl());
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
    public void updateOrganizationsLogos(ArrayList<DataModel> organizationDataList) throws IOException{
        HashMap<Integer,File> organizationLogosMap = mLocalDataFetcher.getImages(EvendateContract.PATH_ORGANIZATION_LOGOS);

        Log.i(LOG_TAG, "images sync started");

        try {
            for (DataModel entry : organizationDataList) {
                OrganizationModel organizationModel = (OrganizationModel) entry;
                File match = organizationLogosMap.get(organizationModel.getEntryId());
                if (match != null) {
                    organizationLogosMap.remove(entry.getEntryId());
                    if (organizationModel.updatedAt() <= match.lastModified())
                        continue;
                }
                if(organizationModel.getLogoSmallUrl() == null)
                    continue;
                String format = Utils.getFileExtension(organizationModel.getLogoSmallUrl());
                String filepath = EvendateContract.PATH_ORGANIZATION_LOGOS + "/" + Integer.toString(organizationModel.getEntryId()) + "." + format;
                format = Utils.normalizeBitmapFormat(format);
                URL url = new URL(organizationModel.getLogoSmallUrl());
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
    public void saveUserPhoto(FriendModel friendModel) throws IOException{
        try {
            if(friendModel.getAvatarUrl() == null)
                return;
            String urlStr = friendModel.getAvatarUrl();
            //cut off google params
            urlStr = urlStr.substring(0, urlStr.lastIndexOf("?"));
            String format = Utils.getFileExtension(urlStr);
            String filepath = "images" + "/user" + "." + format;
            format = Utils.normalizeBitmapFormat(format);
            URL url = new URL(friendModel.getAvatarUrl());
            ImageServerLoader.loadImage(filepath, url, Bitmap.CompressFormat.valueOf(format));
        }catch (MalformedURLException e){
            Log.e(LOG_TAG, "error parsing image url");
            throw e;
        }
    }
}