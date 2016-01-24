package pl.gda.pg.eti.kask.am.mobilefood.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import pl.gda.pg.eti.kask.am.mobilefood.model.Tag;

/**
 * Created by Kuba on 2016-01-22.
 */
public class TagsDataSource {
    private static final String TAG = "TagsDataSource";

    private final SQLiteDatabase database;
    private String[] allColumns = {
            TagsTable.COLUMN_LOCAL_ID,
            TagsTable.COLUMN_ID,
            TagsTable.COLUMN_NAME
    };

    public TagsDataSource(SQLiteDatabase database) {
        this.database = database;
    }

    public List<Tag> getAllTags() {
        Cursor cursor = database.query(TagsTable.TABLE_TAG, allColumns,
                null, null, null, null, null);
        return getTagsFromActiveCursor(cursor);
    }

    public boolean isTagNameUnique(String name) {
        Cursor cursor = database.query(TagsTable.TABLE_TAG, allColumns,
                TagsTable.COLUMN_NAME + " = ? ", new String[]{name}, null, null, null);
        List<Tag> tags = getTagsFromActiveCursor(cursor);
        return tags.isEmpty();
    }

    public Tag getTagForRemoteId(int remoteId) {
        Cursor cursor = database.query(TagsTable.TABLE_TAG, allColumns,
                TagsTable.COLUMN_ID + " = ? ", new String[]{String.valueOf(remoteId)}, null, null, null);
        List<Tag> tags = getTagsFromActiveCursor(cursor);
        if (tags.isEmpty()) {
            return null;
        } else {
            return tags.get(0);
        }
    }

    public List<Tag> getAllActiveTagsForProduct(long productLocalId) {
        String rawQuery = "select * from " + TagsTable.TABLE_TAG + " where " + TagsTable.COLUMN_LOCAL_ID
                + " in (select " + ProductTagRelationTable.COLUMN_TAG_LOCAL_ID + " from "
                + ProductTagRelationTable.TABLE_PRODUCT_TAG_RELATION
                + " where " + ProductTagRelationTable.COLUMN_PRODUCT_LOCAL_ID + " = " + productLocalId
                + " and " + ProductTagRelationTable.COLUMN_IS_ACTIVE + " = " + DBUtils.booleanToInt(true) + ")";
        Cursor cursor = database.rawQuery(rawQuery, null);
        return getTagsFromActiveCursor(cursor);
    }

    public List<Tag> getAllInactiveTagsForProduct(long productLocalId) {
        List<Tag> allTags = getAllTags();
        List<Tag> activelyAssociated = getAllActiveTagsForProduct(productLocalId);
        allTags.removeAll(activelyAssociated);
        return allTags;
    }

    public Tag getTag(long localId) {
        Cursor cursor = database.query(TagsTable.TABLE_TAG, allColumns,
                TagsTable.COLUMN_LOCAL_ID + " = ? ", new String[]{String.valueOf(localId)}, null, null, null);
        cursor.moveToFirst();
        Tag foundTag = cursorToTag(cursor);
        cursor.close();
        return foundTag;
    }

    public Tag createTag(String name) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TagsTable.COLUMN_ID, 0);
        contentValues.put(TagsTable.COLUMN_NAME, name);
        return createTagInternal(contentValues);
    }

    public Tag createTagFromRemote(Tag remote) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TagsTable.COLUMN_ID, remote.getId());
        contentValues.put(TagsTable.COLUMN_NAME, remote.getName());
        return createTagInternal(contentValues);
    }

    public void updateTagRemoteId(long localId, long remoteId) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TagsTable.COLUMN_ID, remoteId);
        updateTagInternal(localId, contentValues);
    }

    private void updateTagInternal(long localId, ContentValues contentValues) {
        database.update(TagsTable.TABLE_TAG, contentValues,
                TagsTable.COLUMN_LOCAL_ID + " = ? ", new String[]{String.valueOf(localId)});
    }

    private Tag createTagInternal(ContentValues contentValues) {
        long insertId = database.insert(TagsTable.TABLE_TAG, null, contentValues);
        Tag created = getTag(insertId);
        Log.d(TAG, "Created tag: " + created.getName() + ", local id: " + created.getLocalId() + ", id: " + created.getId());
        return created;
    }

    @NonNull
    private List<Tag> getTagsFromActiveCursor(Cursor cursor) {
        List<Tag> tags = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Tag tag = cursorToTag(cursor);
            tags.add(tag);
            cursor.moveToNext();
        }
        cursor.close();
        return tags;
    }

    private Tag cursorToTag(Cursor cursor) {
        Tag tag = new Tag();
        tag.setLocalId(cursor.getLong(cursor.getColumnIndex(TagsTable.COLUMN_LOCAL_ID)));
        tag.setName(cursor.getString(cursor.getColumnIndex(TagsTable.COLUMN_NAME)));
        tag.setId(cursor.getInt(cursor.getColumnIndex(TagsTable.COLUMN_ID)));
        return tag;
    }
}
