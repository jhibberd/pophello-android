package com.example.pophello.app.model.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.example.pophello.app.model.Tag;

public class TagActiveStore {

    private static final String TAG = "TagActiveStore";

    private SQLiteDatabase mDatabase;
    private StoreManager mStoreManager;

    public TagActiveStore(Context context) {
        mStoreManager = StoreManager.getInstance(context);
    }

    /**
     * Put a tag into local storage as the current active tag, replacing any existing active tag.
     */
    public void put(Tag tag) throws LocalStorageUnavailableException {
        clear();
        ContentValues values = new ContentValues();
        values.put(StoreManager.COLUMN_ID, tag.id);
        values.put(StoreManager.COLUMN_LATITUDE, tag.latitude);
        values.put(StoreManager.COLUMN_LONGITUDE, tag.longitude);
        values.put(StoreManager.COLUMN_TEXT, tag.text);
        if (mDatabase.insert(StoreManager.TABLE_TAG_ACTIVE, null, values) == -1) {
            Log.e(TAG, "failed to write active tag to database");
        }
    }

    /**
     * Fetch the active tag from local storage, or nil if there is no active tag.
     */
    public Tag fetch() throws LocalStorageUnavailableException {
        open();
        Cursor cursor = mDatabase.query(
                StoreManager.TABLE_TAG_ACTIVE, null, null, null, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null; // none
            } else {
                return createTagFromCursor(cursor);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Clear the active tag from local storage.
     */
    public void clearIfActive(Tag tag) throws LocalStorageUnavailableException {
        open();
        Tag tagActive = fetch();
        // this shouldn't happen because to exit a tag the device must have entered it first
        if (tagActive == null) {
            Log.e(TAG, "Attempt to clear the active data from local storage, but none exists");
            return;
        }
        if (tagActive.id.equals(tag.id)) {
            clear();
        }
    }

    /**
     * Clear the active tag from local storage.
     */
    public void clear() throws LocalStorageUnavailableException {
        open();
        mDatabase.delete(StoreManager.TABLE_TAG_ACTIVE, null, null);
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
        return new Tag(id, latitude, longitude, text);
    }
}
