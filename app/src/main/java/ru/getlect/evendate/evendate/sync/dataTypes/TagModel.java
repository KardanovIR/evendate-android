package ru.getlect.evendate.evendate.sync.dataTypes;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import org.chalup.microorm.annotations.Column;

import ru.getlect.evendate.evendate.data.EvendateContract.TagEntry;

/**
 * Created by Dmitry on 11.09.2015.
 */
public class TagModel extends DataModel {
    @Column(TagEntry.COLUMN_TAG_ID)
    @SerializedName("id")
    int tagId;
    @Column(TagEntry.COLUMN_NAME)
    String name;
    String created_at;
    String updated_at;

    @Override
    public int getEntryId() {
        return this.tagId;
    }

    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (!(getClass() == obj.getClass())) return false;

        TagModel tmp = (TagModel) obj;
        return (this.name.equals(tmp.name));
    }

    @Override
    public ContentProviderOperation getInsert(Uri ContentUri) {
        return fillWithData(ContentProviderOperation.newInsert(ContentUri))
                .withValue(TagEntry.COLUMN_TAG_ID, this.tagId)
                .build();
    }
    protected ContentProviderOperation.Builder fillWithData(ContentProviderOperation.Builder operation){
        return operation
                .withValue(TagEntry.COLUMN_NAME, this.name);
    }
}