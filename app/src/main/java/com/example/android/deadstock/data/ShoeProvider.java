package com.example.android.deadstock.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

/**
 * {@Link ContentProvider} for the Deadstock app.
 */
public class ShoeProvider extends ContentProvider {

    /** Tag for the log messages */
    public static final String LOG_TAG = ShoeProvider.class.getSimpleName();

    /** URI matcher code for the content URI for the shoes table */
    private static final int SHOES = 100;

    /** URI matcher code for the content URI for a single shoe in the shoes table */
    private static final int SHOE_ID = 101;

    /** UriMatcher object to match a content URI to a corresponding code */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /** Static initializer */
    static {
        // URI to provide access to multiple rows of shoes table
        sUriMatcher.addURI(ShoeContract.CONTENT_AUTHORITY, ShoeContract.PATH_SHOES, SHOES);

        // URI to provide access to a single row of the shoes table
        sUriMatcher.addURI(ShoeContract.CONTENT_AUTHORITY, ShoeContract.PATH_SHOES + "/#", SHOE_ID);
    }

    /** Database helper object */
    private ShoeDbHelper mDbHelper;


    @Override
    public boolean onCreate() {
        mDbHelper = new ShoeDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //GGet readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOES:
                // Query shoes table directly. Cursor could contain multiple rows of shoes table
                cursor = database.query(ShoeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case SHOE_ID:
                // Extract out ID from URI
                selection = ShoeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                // Perform query on shoes table to return a Cursor containing that row of the table
                cursor = database.query(ShoeEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("cannot query unknown URI " + uri);
        }

        // Set notification URI on the cursor so we know what content URI the cursor was created for
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the cursor
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOES:
                return ShoeEntry.CONTENT_LIST_TYPE;
            case SHOE_ID:
                return ShoeEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOES:
                return insertShoe(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a shoe into the database with the given content values. Return the new content URI for that
     * specific row in the database
     */
    private Uri insertShoe(Uri uri, ContentValues values){

        // Check that the brand is not null
        Integer shoeBrand = values.getAsInteger(ShoeEntry.COLUMN_SHOE_BRAND);
        if (shoeBrand == null || !ShoeEntry.isValidBrand(shoeBrand)) {
            throw new IllegalArgumentException("Shoe requires a brand");
        }

        // Check that the name is not null
        String shoeName = values.getAsString(ShoeEntry.COLUMN_SHOE_NAME);
        if (shoeName == null) {
            throw new IllegalArgumentException("Shoe requires a name");
        }

        // If quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(ShoeEntry.COLUMN_SHOE_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new
                    IllegalArgumentException("Shoe requires valid quantity");
        }

        // If price is provided, check that it's greater than or equal to 0
        Long price = values.getAsLong(ShoeEntry.COLUMN_SHOE_PRICE);
        if (price != null && price < 0) {
            throw new
                    IllegalArgumentException("Shoe requires valid price");
        }

        //TODO: Check image is valid

        // Get write-able database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new show with the given values
        long id = database.insert(ShoeEntry.TABLE_NAME, null, values);
        // If ID is -1, then insertion failed. Log an error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        // Notify all listeners that hte data has changed for the shoe content URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return the new URI with the ID appended at the end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get write-able database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOES:
                // Delete all rows that match the selection and selectionArgs
                rowsDeleted = database.delete(ShoeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case SHOE_ID:
                // Delete a single row given by the ID in the URI
                selection = ShoeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ShoeEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // If 1 or more rows were deleted, then notify all listeners that the data at the given URI
        // has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case SHOES:
                return updateShoe(uri, contentValues, selection, selectionArgs);
            case SHOE_ID:
                // For the SHOE_ID code, extract out the ID from the URI
                selection = ShoeEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateShoe(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update shoes in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more shoes).
     * Return number of rows successfully updated.
     */
    private int updateShoe(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If the {@link ShoeEntry#COLUMN_SHOE_BRAND} key is present,
        // check that the name value is not null.
        if (values.containsKey(ShoeEntry.COLUMN_SHOE_BRAND)) {
            Integer shoeBrand = values.getAsInteger(ShoeEntry.COLUMN_SHOE_BRAND);
            if (shoeBrand == null || !ShoeEntry.isValidBrand(shoeBrand)) {
                throw new IllegalArgumentException("Shoe requires a valid brand");
            }
        }

        // If the {@link ShoeEntry#COLUMN_SHOE_NAME} key is present,
        // check that the name value is not null.
        if (values.containsKey(ShoeEntry.COLUMN_SHOE_NAME)) {
            String shoeName = values.getAsString(ShoeEntry.COLUMN_SHOE_NAME);
            if (shoeName == null) {
                throw new IllegalArgumentException("Shoe requires a name");
            }
        }

        // If the {@link ShoeEntry#COLUMN_SHOE_QUANTITY} key is present,
        // check that the quantity value is valid.
        if (values.containsKey(ShoeEntry.COLUMN_SHOE_QUANTITY)) {
            // Check that the weight is greater than or equal to 0 kg
            Integer quantity = values.getAsInteger(ShoeEntry.COLUMN_SHOE_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Shoe requires valid quantity");
            }
        }

        // If the {@link ShoeEntry#COLUMN_SHOE_PRICE} key is present,
        // check that the price value is valid.
        if (values.containsKey(ShoeEntry.COLUMN_SHOE_PRICE)) {
            // Check that the weight is greater than or equal to 0 kg
            Long price = values.getAsLong(ShoeEntry.COLUMN_SHOE_PRICE);
            if (price != null && price < 0) {
                throw new IllegalArgumentException("Shoe requires valid price");
            }
        }

        //TODO: Check image is valid

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Get write-able database to update the data
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(ShoeEntry.TABLE_NAME, values, selection, selectionArgs);

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Returns the number of rows updated
        return rowsUpdated;
    }

}
