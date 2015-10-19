package ru.getlect.evendate.evendate.sync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.R;
import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.dataTypes.DataEntry;
import ru.getlect.evendate.evendate.sync.dataTypes.EventEntry;

/**
 * Created by Dmitry on 18.10.2015.
 */
public class ImageManager {

    public LocalDataFetcher mLocalDataFetcher;
    public HashMap<Integer,File> mOrganizationImagesMap;

    public ImageManager(LocalDataFetcher localDataFetcher) {
        mLocalDataFetcher = localDataFetcher;
    }

    public void updateEventImages(ArrayList<DataEntry> eventDataList){
        HashMap<Integer,File> eventImagesMap = mLocalDataFetcher.getEventImages();
        //HashMap<Integer, DataEntry> eventDataMap = new HashMap<>();
        //for (DataEntry e : eventDataList) {
        //    eventDataMap.put(e.getEntryId(), e);
        //}
        File filepath = Environment.getExternalStorageDirectory();
        for(DataEntry e : eventDataList){
            if(eventImagesMap == null){

                break;
            }
            File match = eventImagesMap.get(e.getEntryId());
            if(match != null){
                eventImagesMap.remove(e.getEntryId());
                if(((EventEntry)e).getUpdatedAt() > match.lastModified()){
                    File image = new File(filepath + "/" + EvendateContract.PATH_EVENT_IMAGES, Integer.toString(e.getEntryId()));

                    try {

                        URL url = new URL(((EventEntry)e).getImageHorizontalUrl());
                        //final BitmapFactory.Options options = new BitmapFactory.Options();
                        //options.inSampleSize = 8;
                        //Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent(), null, options);
                        Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
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
                File image = new File(filepath + "/" + EvendateContract.PATH_EVENT_IMAGES, Integer.toString(e.getEntryId()));

                try {

                    URL url = new URL(((EventEntry)e).getImageHorizontalUrl());
                    //final BitmapFactory.Options options = new BitmapFactory.Options();
                    //options.inSampleSize = 8;
                    //Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent(), null, options);
                    Bitmap bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
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
            for (int i = 0; i < eventImagesMap.size(); i++) {
                File file = eventImagesMap.get(i);
                file.delete();
            }
        }
    }
}