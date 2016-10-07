package com.example.android.deadstock;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

    /** Content URI for the existing shoe (null if it's a new shoe) */
    private Uri mCurrentShoeUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine intent that was used to launch this activity
        mCurrentShoeUri = getIntent().getData();

        // If the intent DOES NOT contain a shoe content URI, then we're creating a new shoe
        if(mCurrentShoeUri == null) {
            // This is a new shoe so change app bar to say "Add a Shoe"
            setTitle(R.string.editor_activity_title_new_shoe);
            // Invalidate the options menu so "Delete" menu option can be hidden
            invalidateOptionsMenu();
        } else {
            // Change app bar to say "Edit Shoe"
            setTitle(R.string.editor_activity_title_edit_shoe);

            // Initialize a loader to read shoe data from the database and display
            // current values in editor
            getSupportLoaderManager().initLoader(EXITING_SHOE_LOADER, null, this);
        }

        // Fine all relevant views that we will need to read user input from
        mBrandSpinner = (Spinner) findViewById(R.id.spinner_brand);
        mNameEditText = (EditText) findViewById(R.id.edit_shoe_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_shoe_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_shoe_price);

        setupSpinner();

        // Setup OnTouchListener on all input fields
        mBrandSpinner.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

    }

    /** Setup the dropdown spinner that allows the user to select the brand of the shoe */
    private void setupSpinner() {
        // Create adapter for spinner
        ArrayAdapter brandSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_brand_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style
        brandSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mBrandSpinner.setAdapter(brandSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mBrandSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.brand_jordan))) {
                        mBrand = ShoeEntry.BRAND_JORDAN;
                    } else if (selection.equals(getString(R.string.brand_nike))) {
                        mBrand = ShoeEntry.BRAND_NIKE;
                    } else if (selection.equals(getString(R.string.brand_adidas))) {
                        mBrand = ShoeEntry.BRAND_ADIDAS;
                    } else if (selection.equals(getString(R.string.brand_puma))) {
                        mBrand = ShoeEntry.BRAND_PUMA;
                    } else if (selection.equals(getString(R.string.brand_reebok))) {
                        mBrand = ShoeEntry.BRAND_REEBOK;
                    } else if (selection.equals(getString(R.string.brand_converse))) {
                        mBrand = ShoeEntry.BRAND_CONVERSE;
                    } else if (selection.equals(getString(R.string.brand_other))) {
                        mBrand = ShoeEntry.BRAND_OTHER;
                    }
                }
            }

            // Define onNothingSelected
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                mBrand = ShoeEntry.BRAND_OTHER;
            }
        });
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
