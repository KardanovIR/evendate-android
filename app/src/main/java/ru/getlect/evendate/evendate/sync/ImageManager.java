package ru.getlect.evendate.evendate.sync;

import java.io.File;
import java.util.HashMap;

/**
 * Created by Dmitry on 18.10.2015.
 */
public class ImageManager {

    public HashMap<Double,File> mEventImagesMap;
    public HashMap<Double,File> mOrganizationImagesMap;

    public ImageManager(LocalDataFetcher localDataFetcher) {
        mEventImagesMap = localDataFetcher.getEventImages();
        mOrganizationImagesMap = localDataFetcher.getOrganizationsImages();
    }
}
