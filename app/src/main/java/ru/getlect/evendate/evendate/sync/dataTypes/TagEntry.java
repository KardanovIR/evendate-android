package ru.getlect.evendate.evendate.sync.dataTypes;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import ru.getlect.evendate.evendate.data.EvendateContract;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class TagEntry extends DataEntry {
    @SerializedName("id")
    public final int tag_id;
    public final String name;
    public String created_at;
    public String updated_at;
    public String status;

    public TagEntry(int tag_id, String name) {
        this.tag_id = tag_id;
        this.name = name;
    }

    @Override
    public int getEntryId() {
        return this.tag_id;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;

    /* obj ссылается на null */
        if (obj == null) return false;

    /* Удостоверимся, что ссылки имеют тот же самый тип */
        if (!(getClass() == obj.getClass())) return false;
        TagEntry tmp = (TagEntry) obj;
        return (this.name.equals(tmp.name));
    }

    @Override
    public ContentProviderOperation getUpdate(final Uri ContentUri) {
        return ContentProviderOperation.newUpdate(ContentUri)
                .withValue(EvendateContract.OrganizationEntry.COLUMN_NAME, this.name)
                .build();
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return ContentProviderOperation.newInsert(ContentUri)
                .withValue(EvendateContract.TagEntry.COLUMN_TAG_ID, this.tag_id)
                .withValue(EvendateContract.TagEntry.COLUMN_NAME, this.name)
                .build();
    }
}