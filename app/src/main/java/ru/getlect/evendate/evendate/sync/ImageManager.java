package ru.getlect.evendate.evendate.sync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;

/**
 * Created by Dmitry on 18.10.2015.
 */
public class ImageManager {
    String LOG_TAG = EvendateSyncAdapter.class.getSimpleName();
    public LocalDataFetcher mLocalDataFetcher;
    public HashMap<Integer,File> mOrganizationImagesMap;

    public ImageManager(LocalDataFetcher localDataFetcher) {
        mLocalDataFetcher = localDataFetcher;
    }

    public void updateEventImages(ArrayList<DataEntry> eventDataList){
        HashMap<Integer,File> eventImagesMap = mLocalDataFetcher.getEventImages();

        Log.i(LOG_TAG, "images sync started");

        File filepath = Environment.getExternalStorageDirectory();

        if(eventImagesMap == null){
            for(DataEntry e : eventDataList){
                File image = new File(filepath + "/" + EvendateContract.PATH_EVENT_IMAGES, Integer.toString(e.getEntryId()) + ".jpg");
                try {
                    Log.i(LOG_TAG, "add new image");
                    URL url = new URL(((EventEntry)e).getImageHorizontalUrl());
                    //final BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 8;
                    //Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent(), null, options);
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
                    image.createNewFile();
                    BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(image));
                    // Compress into png format image from 0% - 100%
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, buf);
                    buf.flush();
                    buf.close();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return;
        }
        for(DataEntry e : eventDataList){
            File match = eventImagesMap.get(e.getEntryId());
            if(match != null){
                eventImagesMap.remove(e.getEntryId());
                //TODO (EventEntry)e).getUpdatedAt() нуль всегда возращает, ибо локальный список фетчится
                if(((EventEntry)e).getUpdatedAt() > match.lastModified()){
                    File image = new File(filepath + "/" + EvendateContract.PATH_EVENT_IMAGES, Integer.toString(e.getEntryId()) + ".jpg");

                    try {

                        Log.i(LOG_TAG, "update existed image");
                        URL url = new URL(((EventEntry)e).getImageHorizontalUrl());
                        //final BitmapFactory.Options options = new BitmapFactory.Options();
                        //options.inSampleSize = 8;
                        //Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent(), null, options);
                        Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
                        image.createNewFile();
                        BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(image));
                        // Compress into png format image from 0% - 100%
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, buf);
                        buf.flush();
                        buf.close();
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            else{
                File image = new File(filepath + "/" + EvendateContract.PATH_EVENT_IMAGES, Integer.toString(e.getEntryId()) + ".jpg");

                try {

                    Log.i(LOG_TAG, "save new image");
                    URL url = new URL(((EventEntry)e).getImageHorizontalUrl());
                    //final BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 8;
                    //Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent(), null, options);
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
                    image.createNewFile();
                    BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(image));
                    // Compress into png format image from 0% - 100%
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, buf);
                    buf.flush();
                    buf.close();
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        for (int i = 0; i < eventImagesMap.size(); i++) {
            Log.i(LOG_TAG, "delete image");
            File file = eventImagesMap.get(i);
            file.delete();
        }
        Log.i(LOG_TAG, "images sync ended");
    }
}