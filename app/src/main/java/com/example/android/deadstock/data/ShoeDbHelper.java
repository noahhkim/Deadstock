package com.example.android.deadstock.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

/**
 * Database helper for Deadstock app. Manages database creation and version management
 */
public class ShoeDbHelper extends SQLiteOpenHelper {

    /** Tag for the log messages */
    public static final String LOG_TAG = ShoeDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "inventory.db";

    /** Database version */
    private static final int DATABASE_VERSION = 1;

    /** Constructs a new instance of {@Link ShoeDbHelper} */
    public ShoeDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /** Called when database is created for the first time */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the shoes table
        String SQL_CREATE_SHOES_TABLE = "CREATE TABLE " + ShoeEntry.TABLE_NAME + " ("
                + ShoeEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ShoeEntry.COLUMN_SHOE_BRAND + " TEXT NOT NULL, "
                + ShoeEntry.COLUMN_SHOE_NAME + " TEXT, "
                + ShoeEntry.COLUMN_SHOE_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ShoeEntry.COLUMN_SHOE_PRICE + " INTEGER NOT NULL, "
                + ShoeEntry.COLUMN_SHOE_IMAGE + " INTEGER NOT NULL);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_SHOES_TABLE);
    }

    /** Called when database needs to be upgraded */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
