package com.example.android.deadstock;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

/**
 * Allows user to create a new shoe or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the shoe data loader */
    private static final int EXITING_SHOE_LOADER = 0;

    /** EditText field to enter the shoe's brand */
    private Spinner mBrandSpinner;

    /** EditText field to enter the shoe's name */
    private EditText mNameEditText;

    /** EditText field to enter the shoe's quantity */
    private EditText mQuantityEditText;

    /** EditText field to enter the shoe's price */
    private EditText mPriceEditText;

    /** Brand of the pet */
    private int mBrand = ShoeEntry.BRAND_OTHER;

    /** Boolean flag that keeps track of whether the shoe has been edited (true) or not (false) */
    private boolean mShoeHasChanged = false;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mShoeHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mShoeHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
