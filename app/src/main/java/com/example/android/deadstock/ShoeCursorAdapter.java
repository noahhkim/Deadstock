package com.example.android.deadstock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Currency;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

import java.util.Locale;

/**
 * Created by noahkim on 10/4/16.
 */

public class ShoeCursorAdapter extends CursorAdapter {

    private final String QUANTITY_LABEL = "Quantity: ";
    private final String PRICE_LABEL = "Price: ";

    public ShoeCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views to modify in list item layout
        ImageView imageView = (ImageView) view.findViewById(R.id.list_item_thumbnail);
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        TextView priceView = (TextView) view.findViewById(R.id.price);
        Button soldButton = (Button) view.findViewById(R.id.item_sold_button);

        // Find columns of shoe attributes
        int idColumnIndex = cursor.getColumnIndex(ShoeEntry._ID);
        int imageColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_PRICE);

        // Read shoe attributes from Cursor for current shoe
        String shoeImage = cursor.getString(imageColumnIndex);
        String shoeName = cursor.getString(nameColumnIndex);
        String shoeQuantity = cursor.getString(quantityColumnIndex);
        String shoePrice = cursor.getString(priceColumnIndex);

        // If quantity is an empty string or null, then default text says "N/A"
        if (TextUtils.isEmpty(shoeQuantity)) {
            shoeQuantity = "N/A";
        }

        // If price is an empty string or null, then default text says "N/A"
        if (TextUtils.isEmpty(shoePrice)) {
            shoePrice = "N/A";
        }

        // Get local currency symbol
        Currency currency = Currency.getInstance(Locale.getDefault());
        String symbol = currency.getSymbol();

        // Update the Views with the attributes for the current shoe
        imageView.setImageBitmap(StringToBitmap(shoeImage));
        nameView.setText(shoeName);
        quantityView.setText(QUANTITY_LABEL + shoeQuantity);
        priceView.setText(PRICE_LABEL + symbol + shoePrice);

        final int currentQuantity = cursor.getInt(quantityColumnIndex);
        final int currentId = cursor.getInt(idColumnIndex);
        soldButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = currentQuantity;

                if (quantity > 0) {
                    Uri uri = Uri.parse(ShoeEntry.CONTENT_URI + "/" + currentId);

                    int newQuantity = quantity - 1;

                    ContentValues values = new ContentValues();
                    values.put(ShoeEntry.COLUMN_SHOE_QUANTITY, newQuantity);
                    context.getContentResolver().update(uri, values, null, null);
                } else {
                    Toast.makeText(context, "Cannot reduce quantity below 0", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Bitmap StringToBitmap(String encodedString) {
        try {
            byte [] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }
}
