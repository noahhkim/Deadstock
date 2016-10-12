package com.example.android.deadstock;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

/**
 * Displays list of shoes that were entered and stored in the app
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Identifier for the shoe data loader
     */
    private static final int SHOE_LOADER = 0;

    /**
     * Adapter for the ListView
     */
    ShoeCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Set up FAB to open Editor Activity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the shoe data
        ListView shoeListView = (ListView) findViewById(R.id.list);

        // Find and set empty iew on the ListView, so that it only shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        shoeListView.setEmptyView(emptyView);

        // Setup on Adapter to create a list item for each row of shoe data in the Cursor
        // There is no shoe data yet (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new ShoeCursorAdapter(this, null);
        shoeListView.setAdapter(mCursorAdapter);

        // Setup item click listener
        shoeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific shoe that was clicked on
                Uri currentShoeUri = ContentUris.withAppendedId(ShoeEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentShoeUri);

                // Launch the EditorActivity to display data for current shoe
                startActivity(intent);
            }
        });

        // Initialize the CursorLoader
        getLoaderManager().initLoader(SHOE_LOADER, null, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     * Helper method to insert hardcoded shoe data into the database. For debugging purposes only.
     */
    private void insertShoe() {
        // Create a ContentValues object where column names are the keys,
        // and sample shoe attributes are the values.
        ContentValues values = new ContentValues();
        values.put(ShoeEntry.COLUMN_SHOE_BRAND, ShoeEntry.BRAND_JORDAN);
        values.put(ShoeEntry.COLUMN_SHOE_NAME, "Jordan");
        values.put(ShoeEntry.COLUMN_SHOE_QUANTITY, 5);
        values.put(ShoeEntry.COLUMN_SHOE_PRICE, 3);

        // Insert a new row for Toto into the provider using the ContentResolver.
        // Use the {@link PetEntry#CONTENT_URI} to indicate that we want to insert
        // into the pets database table.
        // Receive the new content URI that will allow us to access Toto's data in the future.
        Uri newUri = getContentResolver().insert(ShoeEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Adds menu items to app bar
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertShoe();
                return true;
            // Respond to a click on the "Delete All Shoes" menu option
            case R.id.action_delete_all_shoes:
                // Delete all pets from the database
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteAllShoes() {
        getContentResolver().delete(ShoeEntry.CONTENT_URI, null, null);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message and click listeners
        // for hte positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_all_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "Delete" button, so delete all shoes
                deleteAllShoes();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {

        // Define a projection that specifies which columns from the database
        // we will actually use after this query
        String[] projection = {
                ShoeEntry._ID,
                ShoeEntry.COLUMN_SHOE_NAME,
                ShoeEntry.COLUMN_SHOE_QUANTITY,
                ShoeEntry.COLUMN_SHOE_PRICE};

        return new CursorLoader(
                this,
                ShoeEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@Link ShoeCursorAdapter} with this new cursor containing updated shoe data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);

    }
}