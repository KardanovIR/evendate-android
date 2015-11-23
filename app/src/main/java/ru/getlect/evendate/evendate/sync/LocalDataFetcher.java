package ru.getlect.evendate.evendate.sync;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import org.chalup.microorm.MicroOrm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import ru.getlect.evendate.evendate.data.EvendateContract;
import ru.getlect.evendate.evendate.sync.models.DataModel;
import ru.getlect.evendate.evendate.sync.models.EventFriendModel;
import ru.getlect.evendate.evendate.sync.models.EventModel;
import ru.getlect.evendate.evendate.sync.models.EventTagModel;
import ru.getlect.evendate.evendate.sync.models.FriendModel;
import ru.getlect.evendate.evendate.sync.models.OrganizationModel;
import ru.getlect.evendate.evendate.sync.models.TagModel;
import ru.getlect.evendate.evendate.utils.Utils;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class LocalDataFetcher {
    ContentResolver mContentResolver;
    Context mContext;
    MicroOrm mMicroOrm = new MicroOrm();

    public LocalDataFetcher(ContentResolver contentResolver, Context context) {
        mContentResolver = contentResolver;
        mContext = context;
    }

    public ArrayList<DataModel> getOrganizationDataFromDB(){
        ArrayList<DataModel> resList = new ArrayList<>();

        Uri uri = EvendateContract.OrganizationEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            OrganizationModel entry = mMicroOrm.fromCursor(c, OrganizationModel.class);
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataModel> getTagsDataFromDB(){
        ArrayList<DataModel> resList = new ArrayList<>();

        Uri uri = EvendateContract.TagEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            TagModel entry = mMicroOrm.fromCursor(c, TagModel.class);
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataModel> getUserDataFromDB(){
        ArrayList<DataModel> resList = new ArrayList<>();

        Uri uri = EvendateContract.UserEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            FriendModel entry = mMicroOrm.fromCursor(c, FriendModel.class);
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<DataModel> getEventDataFromDB(){
        ArrayList<DataModel> resList = new ArrayList<>();

        Uri uri = EvendateContract.EventEntry.CONTENT_URI; // Get all entries
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            EventModel entry = mMicroOrm.fromCursor(c, EventModel.class);
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<TagModel> getEventTagDataFromDB(int eventId){
        ArrayList<TagModel> resList = new ArrayList<>();

        Uri uri = EvendateContract.EventTagEntry.GetContentUri(eventId); // Get all entries
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            EventTagModel entry = mMicroOrm.fromCursor(c, EventTagModel.class);
            resList.add(entry);
        }
        c.close();
        return resList;
    }
    public ArrayList<FriendModel> getEventFriendDataFromDB(int eventId){
        ArrayList<FriendModel> resList = new ArrayList<>();

        Uri uri = EvendateContract.EventEntry.CONTENT_URI.buildUpon()
                .appendPath(Integer.toString(eventId)).appendPath(EvendateContract.PATH_USERS).build();
        Cursor c = mContentResolver.query(uri, null, null, null, null);
        assert c != null;
        while (c.moveToNext()){
            EventFriendModel entry = mMicroOrm.fromCursor(c, EventFriendModel.class);
            resList.add(entry);
        }
        c.close();
        return resList;
    }

    public HashMap<Integer, File> getImages(String path) {
        File dir = new File(mContext.getExternalCacheDir().toString(), path);
        File[] pictures = dir.listFiles();
        HashMap<Integer, File> files = new HashMap<>();
        if(pictures == null)
            return files;

        for (File file : pictures) {
            if(!file.isFile())
                continue;
            files.put(Integer.parseInt(Utils.getFileNameWithoutExtension(file.getName())), file);
            Log.v("FILE:", file.getAbsolutePath());
        }
        return files;
    }
}
