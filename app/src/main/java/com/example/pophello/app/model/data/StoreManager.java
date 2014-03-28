package com.example.pophello.app.model.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

class StoreManager extends android.database.sqlite.SQLiteOpenHelper {

    private static final String DATABASE_NAME = "pophello.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TAGS = "tags";
    public static final String TABLE_TAG_ACTIVE = "tags_active";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_TEXT = "text";

    private static final String DATABASE_CREATE_TAGS = "create table "
            + TABLE_TAGS + "(" + COLUMN_ID + " text primary key, "
            + COLUMN_LATITUDE + " real not null, "
            + COLUMN_LONGITUDE + " real not null, "
            + COLUMN_TEXT + " text not null);";

    private static final String DATABASE_CREATE_TAG_ACTIVE = "create table "
            + TABLE_TAG_ACTIVE + "(" + COLUMN_ID + " text primary key, "
            + COLUMN_LATITUDE + " real not null, "
            + COLUMN_LONGITUDE + " real not null, "
            + COLUMN_TEXT + " text not null);";

    private static StoreManager mInstance;

    /**
     * Return a singleton database connection.
     */
    public static StoreManager getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new StoreManager(context.getApplicationContext());
        }
        return mInstance;
    }

    private StoreManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_TAGS);
        db.execSQL(DATABASE_CREATE_TAG_ACTIVE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
