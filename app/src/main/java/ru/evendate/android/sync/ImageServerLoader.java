package ru.evendate.android.sync;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ru.evendate.android.data.EvendateContract;

/**
 * Created by Dmitry on 21.10.2015.
 *
 * Класс, отвечающий за загрузку и сохрание картинок на sd карту
 */
@Deprecated
public class ImageServerLoader {
    private static Context mContext;

    public static void init(Context context) {
        mContext = context;
        BASE_PATH = mContext.getExternalCacheDir().toString();
        createStorage();
    }

    private static final String LOG_TAG = ImageServerLoader.class.getSimpleName();

    private static String BASE_PATH = null;

    private static void createStorage(){
            //создание папочки для картиночек
            File orgsDir = new File(BASE_PATH + "/" + EvendateContract.PATH_ORGANIZATION_IMAGES);
            File eventsDir = new File(BASE_PATH + "/" + EvendateContract.PATH_EVENT_IMAGES);
            File orgLogosDir = new File(BASE_PATH + "/" + EvendateContract.PATH_ORGANIZATION_LOGOS);
            if (!orgsDir.exists()) {
                boolean res = orgsDir.mkdirs();
                if (!res)
                    Log.w(LOG_TAG, "can't create dir " + orgsDir.toString());
            }
            if (!eventsDir.exists()) {
                boolean res = eventsDir.mkdirs();
                if (!res)
                    Log.w(LOG_TAG, "can't create dir " + eventsDir.toString());
            }
            if (!orgLogosDir.exists()) {
                boolean res = orgLogosDir.mkdirs();
                if (!res)
                    Log.w(LOG_TAG, "can't create dir " + orgLogosDir.toString());
            }
    }

    public static void loadImage(String filepath, URL fileUrl, Bitmap.CompressFormat compressFormat) throws IOException{
        File image = new File(BASE_PATH + "/" + filepath);
        Log.i(LOG_TAG, "save new image");
        Log.i(LOG_TAG, image.getPath());
        try{
            boolean res = image.createNewFile();
            if(!res)
                Log.w(LOG_TAG, "can't create file " + image);
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) fileUrl.getContent());
            if(bitmap == null) {
                Log.e(LOG_TAG, "now image at url " + fileUrl);
                return;
            }
            BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(image));
            bitmap.compress(compressFormat, 100, buf);
            buf.flush();
            buf.close();
            bitmap.recycle();
        }catch (IOException e){
            Log.e(LOG_TAG, "bitmap save error");
            image.delete();
            throw e;
        }
    }
}
