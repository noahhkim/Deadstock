package com.example.android.deadstock;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

import java.io.FileDescriptor;
import java.io.IOException;
import java.text.NumberFormat;

/**
 * Allows user to create a new shoe or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /**
     * Global variables for EditorActivity
     */
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private static final int EXITING_SHOE_LOADER = 0;
    private static final int PICK_IMAGE = 1;
    private Spinner mBrandSpinner;
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private Button mIncreaseButton;
    private Button mDecreaseButton;
    private Button mOrderButton;
    private Button mGetImageButton;
    private EditText mPriceEditText;
    private int mBrand = ShoeEntry.BRAND_OTHER;
    private String quantity;
    private int currentQuantity;
    private String setQuantity;
    private Uri mCurrentShoeUri;
    private Uri mUri;
    private ImageView mImageView;
    private ImageView mThumbnailView;

    /**
     * Boolean flag that keeps track of whether the shoe has been edited (true) or not (false)
     */
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

        // Examine intent that was used to launch this activity
        mCurrentShoeUri = getIntent().getData();

        // If the intent DOES NOT contain a shoe content URI, then we're creating a new shoe
        if (mCurrentShoeUri == null) {
            // This is a new shoe so change app bar to say "Add a Shoe"
            setTitle(R.string.editor_activity_title_new_shoe);
            // Invalidate the options menu so "Delete" menu option can be hidden
            invalidateOptionsMenu();
        } else {
            // Change app bar to say "Edit Shoe"
            setTitle(R.string.editor_activity_title_edit_shoe);

            // Initialize a loader to read shoe data from the database and display
            // current values in editor
            getLoaderManager().initLoader(EXITING_SHOE_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mBrandSpinner = (Spinner) findViewById(R.id.spinner_brand);
        mNameEditText = (EditText) findViewById(R.id.edit_shoe_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_shoe_quantity);
        mIncreaseButton = (Button) findViewById(R.id.increase_button);
        mDecreaseButton = (Button) findViewById(R.id.decrease_button);
        mOrderButton = (Button) findViewById(R.id.order_button);
        mPriceEditText = (EditText) findViewById(R.id.edit_shoe_price);
        mGetImageButton = (Button) findViewById(R.id.get_shoe_image);
        mImageView = (ImageView) findViewById(R.id.editor_image_preview);
        mThumbnailView = (ImageView) findViewById(R.id.list_item_thumbnail);

        setupSpinner();

        // Setup OnTouchListener on all input fields
        mBrandSpinner.setOnTouchListener(mTouchListener);
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);

        // Set up onClickListener for '+' button
        mIncreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity = mQuantityEditText.getText().toString().trim();
                // If EditText field is blank, show Toast message requiring user to enter
                // a number first
                if (quantity.matches("")) {
                    Toast.makeText(EditorActivity.this, getString(R.string.editor_enter_number_first),
                            Toast.LENGTH_SHORT).show();
                } else {
                    currentQuantity = Integer.parseInt(quantity);
                    currentQuantity = currentQuantity + 1;
                    setQuantity = String.valueOf(currentQuantity);
                    mQuantityEditText.setText(setQuantity);
                }
            }
        });

        // Set up onClickListener for '-' button
        mDecreaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                quantity = mQuantityEditText.getText().toString().trim();
                // If EditText field is blank, show Toast message requiring user to enter
                // a number first
                if (quantity.matches("")) {
                    Toast.makeText(EditorActivity.this, getString(R.string.editor_enter_number_first),
                            Toast.LENGTH_SHORT).show();
                } else {
                    currentQuantity = Integer.parseInt(quantity);
                    // Show Toast message if user tries to decrease quantity below 1
                    if (currentQuantity == 1) {
                        Toast.makeText(EditorActivity.this, getString(R.string.editor_quantity_minimum_limit),
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    currentQuantity = currentQuantity - 1;
                    setQuantity = String.valueOf(currentQuantity);
                    mQuantityEditText.setText(setQuantity);
                }
            }
        });

        // Set up OnClickListener for get picture button
        mGetImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});

                startActivityForResult(pickIntent, PICK_IMAGE);
            }
        });
    }

    // Display thumbnail of image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK) {
            mUri = data.getData();

            if (data != null) {
                mUri = data.getData();

                Log.i(LOG_TAG, "Uri: " + mUri.toString());
                Bitmap bitmap = getBitmapFromUri(mUri);
                int nh = (int) (bitmap.getHeight() * (512.0 / bitmap.getWidth()));
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 512, nh, true);

                mImageView.setImageBitmap(scaled);
            }
        }
    }

    // Helper method for getting Bitmap from image URI
    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    /**
     * Setup the dropdown spinner that allows the user to select the brand of the shoe
     */
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
                    if (selection.equals(getString(R.string.brand_other))) {
                        mBrand = ShoeEntry.BRAND_OTHER;
                    } else if (selection.equals(getString(R.string.brand_jordan))) {
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

    /**
     * Get user input from editor and save new shoe into database
     */
    private void saveShoe() {
        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();

        // Check if this is supposed to be a new shoe
        if (mCurrentShoeUri == null &&
                TextUtils.isEmpty(nameString) &&
                TextUtils.isEmpty(quantityString) &&
                TextUtils.isEmpty(priceString) &&
                mUri == null &&
                mBrand == ShoeEntry.BRAND_OTHER) {
            // Return early without creating new shoe
            return;
        }

        // Create a ContentValues object where column names are the keys
        // and shoe attributes from the editor are the values
        ContentValues values = new ContentValues();
        values.put(ShoeEntry.COLUMN_SHOE_BRAND, mBrand);
        values.put(ShoeEntry.COLUMN_SHOE_NAME, nameString);

        // If no quantity is provided, use 10 by default for quantity
        int quantity = 10;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        values.put(ShoeEntry.COLUMN_SHOE_QUANTITY, quantity);

        // If no price is provided, use 0 by default for price
        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(ShoeEntry.COLUMN_SHOE_PRICE, price);

        // If no image is provided, use "no image" by default
        String imageString = "no image";
        if (mUri != null) {
            imageString = mUri.toString();
        }
        values.put(ShoeEntry.COLUMN_SHOE_IMAGE, imageString);

        if (mCurrentShoeUri == null) {
            // Insert a new shoe into the provider, returning the content URI for the new shoe
            Uri newUri = getContentResolver().insert(ShoeEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.editor_insert_shoe_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Toast to show insertion was successful
                Toast.makeText(this, getString(R.string.editor_insert_shoe_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Update show with mCurrentShoeUri and pass in new ContentValues
            int rowsAffected = getContentResolver().update(mCurrentShoeUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with update
                Toast.makeText(this, getString(R.string.editor_insert_shoe_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Toast to show update was successful
                Toast.makeText(this, getString(R.string.editor_insert_shoe_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu options from menu_editor.xml
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to "Save" menu option
            case R.id.action_save:
                // Save shoe to database
                saveShoe();
                // Exit activity
                finish();
                return true;
            // Respond to "Delete" menu option
            case R.id.action_delete:
                // pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            //Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If shoe hasn't changed, continue with navigating up
                if (!mShoeHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // If there are unsaved changes, show warning dialog
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "discard" button
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show dialog to notify unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Define a projection that contains all columns from the table
        String[] projection = {
                ShoeEntry._ID,
                ShoeEntry.COLUMN_SHOE_IMAGE,
                ShoeEntry.COLUMN_SHOE_BRAND,
                ShoeEntry.COLUMN_SHOE_NAME,
                ShoeEntry.COLUMN_SHOE_QUANTITY,
                ShoeEntry.COLUMN_SHOE_PRICE
        };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,
                mCurrentShoeUri,
                projection,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        if (cursor.moveToFirst()) {

            int imageColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_IMAGE);
            int brandColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_BRAND);
            int nameColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_PRICE);

            // Extract out the value from the Cursor for the given column index
            final String IMAGE = cursor.getString(imageColumnIndex);
            final int BRAND = cursor.getInt(brandColumnIndex);
            final String NAME = cursor.getString(nameColumnIndex);
            final int QUANTITY = cursor.getInt(quantityColumnIndex);
            final int PRICE = cursor.getInt(priceColumnIndex);

            // Map constant value from database into one of the dropdown options,
            // then call setSelection() so that option is displayed on screen as
            // the current selection
            switch (BRAND) {
                case ShoeEntry.BRAND_OTHER:
                    mBrandSpinner.setSelection(0);
                    break;
                case ShoeEntry.BRAND_JORDAN:
                    mBrandSpinner.setSelection(1);
                    break;
                case ShoeEntry.BRAND_NIKE:
                    mBrandSpinner.setSelection(2);
                    break;
                case ShoeEntry.BRAND_ADIDAS:
                    mBrandSpinner.setSelection(3);
                    break;
                case ShoeEntry.BRAND_PUMA:
                    mBrandSpinner.setSelection(4);
                    break;
                case ShoeEntry.BRAND_REEBOK:
                    mBrandSpinner.setSelection(5);
                    break;
                case ShoeEntry.BRAND_CONVERSE:
                    mBrandSpinner.setSelection(6);
                    break;
            }

            // Update the views on the screen with the values from the database
            Uri imageUri = Uri.parse(IMAGE);
            mImageView.setImageBitmap(getBitmapFromUri(imageUri));
            mNameEditText.setText(NAME);
            mQuantityEditText.setText(Integer.toString(QUANTITY));
            mPriceEditText.setText(Integer.toString(PRICE));

            // Set up OnClickListener for order button
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String message = getString(R.string.order_summary_name, NAME);
                    message += "\n" + getString(R.string.order_summary_quantity, QUANTITY);
                    message += "\n" + getString(R.string.order_summary_price, NumberFormat.getCurrencyInstance().format(PRICE));

                    Intent intent = ShareCompat.IntentBuilder.from(EditorActivity.this).setStream(mUri)
                            .setSubject(getString(R.string.order_summary_email_subject))
                            .setText(message)
                            .getIntent();

                    if (mUri == null) {
                        Toast.makeText(EditorActivity.this, R.string.order_summary_select_image, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    intent.setData(mUri);
                    intent.setType("message/rfc822");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Share with"));

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });
        }
    }

    public Bitmap StringToBitmap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields
        mBrandSpinner.setSelection(0);
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mImageView.setImageBitmap(null);
    }

    // Method for creating a "Discard changes" dialog
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {

        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the shoe.
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
    public void onBackPressed() {

        // If the shoe hasn't changed, continue with handling back button press
        if (!mShoeHasChanged) {
            super.onBackPressed();
            return;

        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the shoe.
                deleteShoe();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the shoe.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteShoe() {
        // Only perform the delete if this is an existing shoe
        if (mCurrentShoeUri != null) {
            // Deletes the words that match the selection criteria
            int rowsDeleted = getContentResolver().delete(
                    mCurrentShoeUri,
                    null,
                    null
            );

            // Show a toast message depending on whether or not the delete was successful
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with delete
                Toast.makeText(this, getString(R.string.editor_delete_shoe_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_shoe_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}
