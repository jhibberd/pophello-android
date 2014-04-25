package com.example.pophello.app.model.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.pophello.app.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagsStore {

    private static final String TAG = "TagsStore";

    private SQLiteDatabase mDatabase;
    private StoreManager mStoreManager;

    public TagsStore(Context context) {
        mStoreManager = StoreManager.getInstance(context);
    }

    /**
     * Put a new set of tags into local storage.
     *
     * This should only be called if there are currently no tags in local storage.
     */
    public void put(Tag[] tags) throws LocalStorageUnavailableException {
        open();
        for (Tag tag : tags) {
            ContentValues values = new ContentValues();
            values.put(StoreManager.COLUMN_ID, tag.id);
            values.put(StoreManager.COLUMN_LATITUDE, tag.latitude);
            values.put(StoreManager.COLUMN_LONGITUDE, tag.longitude);
            values.put(StoreManager.COLUMN_TEXT, tag.text);
            values.put(StoreManager.COLUMN_USER_ID, tag.userId);
            values.put(StoreManager.COLUMN_USER_IMAGE_URL, tag.userImageUrl);
            if (mDatabase.insert(StoreManager.TABLE_TAGS, null, values) == -1) {
                Log.e(TAG, "failed to write tag to database");
            }
        }
    }

    /**
     * Return a tag by ID, or nil if it doesn't exist.
     */
    public Tag fetch(String tagId) throws LocalStorageUnavailableException {
        open();
        Cursor cursor = mDatabase.query(
                StoreManager.TABLE_TAGS, null,
                StoreManager.COLUMN_ID + " == '" + tagId + "'",
                null, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null; // not found
            } else {
                return createTagFromCursor(cursor);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Return all stored tags.
     */
    public Tag[] fetchAll() throws LocalStorageUnavailableException {
        open();
        Cursor cursor = mDatabase.query(
                StoreManager.TABLE_TAGS, null, null, null, null, null, null);
        try {
            List<Tag> result = new ArrayList<Tag>();
            while (cursor.moveToNext()) {
                result.add(createTagFromCursor(cursor));
            }
            return result.toArray(new Tag[result.size()]);
        } finally {
            cursor.close();
        }
    }

    /**
     * Remove a single tag from local storage.
     */
    public void remove(String tagId) throws LocalStorageUnavailableException {
        open();
        mDatabase.delete(
                StoreManager.TABLE_TAGS, StoreManager.COLUMN_ID + " = ?", new String[] {tagId});
    }

    /**
     * Remove all tags from local storage.
     */
    public void clear() throws LocalStorageUnavailableException {
        open();
        mDatabase.delete(StoreManager.TABLE_TAGS, null, null);
    }

    /**
     * Open a connection to the local storage database.
     *
     * Connections are cached so this can be called before each attempt to access the database.
     */
    private void open() throws LocalStorageUnavailableException {
        try {
            mDatabase = mStoreManager.getWritableDatabase();
        } catch (SQLiteException e) {
            Log.e(TAG, e.toString());
            throw new LocalStorageUnavailableException();
        }
    }

    public void close() {
        mStoreManager.close();
    }

    private Tag createTagFromCursor(Cursor cursor) {
        String id = cursor.getString(0);
        double latitude = cursor.getDouble(1);
        double longitude = cursor.getDouble(2);
        String text = cursor.getString(3);
        String userId = cursor.getString(4);
        String userImageUrl = cursor.getString(5);
        return new Tag(id, latitude, longitude, text, userId, userImageUrl);
    }
}
