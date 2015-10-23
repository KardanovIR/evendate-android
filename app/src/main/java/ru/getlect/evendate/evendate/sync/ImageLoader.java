package ru.getlect.evendate.evendate.sync;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 21.10.2015.
 *
 * Класс, отвечающий за загрузку и сохрание картинок на sd карту
 */
public class ImageLoader {
    private static final String LOG_TAG = ImageLoader.class.getSimpleName();

    private static final String SDCARD_APP_DIR = "Evendate";
    final static String BASE_PATH = Environment.getExternalStorageDirectory().toString() + "/" + SDCARD_APP_DIR;
    static {
        //создание папочки для картиночек
        File dir = new File(Environment.getExternalStorageDirectory()
                + "/Evendate/" + EvendateContract.PATH_ORGANIZATION_IMAGES);
        dir.mkdirs();
        dir = new File(Environment.getExternalStorageDirectory()
                + "/Evendate/" + EvendateContract.PATH_EVENT_IMAGES);
        dir.mkdirs();
        dir = new File(Environment.getExternalStorageDirectory()
                + "/Evendate/" + EvendateContract.PATH_ORGANIZATION_LOGOS);
        dir.mkdirs();
    }

    public static void loadImage(String filepath, URL fileUrl, Bitmap.CompressFormat compressFormat) throws IOException{
        File image = new File(BASE_PATH + "/" + filepath);
        Log.i(LOG_TAG, "save new image");
        try{
            boolean res = image.createNewFile();
            if(!res)
                throw new IOException("can't create file " + image);
            Bitmap bitmap = BitmapFactory.decodeStream((InputStream) fileUrl.getContent());
            if(bitmap == null) {
                Log.e(LOG_TAG, "now image at url " + fileUrl);
                return;
            }
            BufferedOutputStream buf = new BufferedOutputStream(new FileOutputStream(image));
            bitmap.compress(compressFormat, 100, buf);
            buf.flush();
            buf.close();
        }catch (IOException e){
            Log.e(LOG_TAG, "bitmap save error");
            image.delete();
            throw e;
        }
    }
}
