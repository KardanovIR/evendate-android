package ru.getlect.evendate.evendate.sync.dataTypes;

import java.util.ArrayList;

/**
 * Created by Дмитрий on 02.11.2015.
 */
public class TagResponse extends ResponseData{
    private ArrayList<TagEntry> tags;
    private ArrayList<OrganizationEntry> organizations;

    public ArrayList<TagEntry> getTags() {
        return tags;
    }
}
