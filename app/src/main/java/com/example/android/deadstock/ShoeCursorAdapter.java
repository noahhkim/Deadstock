package com.example.android.deadstock;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

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
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views to modify in list item layout
        ImageView imageView = (ImageView) view.findViewById(R.id.image_thumbnail);
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        TextView priceView = (TextView) view.findViewById(R.id.price);

        // Find columns of shoe attributes
        int imageColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_IMAGE);
        int nameColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_PRICE);

        // Read shoe attributes from Cursor for current shoe
        String shoeImage = cursor.getString(imageColumnIndex);
        String shoeName = cursor.getString(nameColumnIndex);
        String shoeQuantity = QUANTITY_LABEL + cursor.getString(quantityColumnIndex);
        String shoePrice = PRICE_LABEL + cursor.getString(priceColumnIndex);

        // If quantity is an empty string or null, then default text says "N/A"
        if (TextUtils.isEmpty(shoeQuantity)) {
            shoeQuantity = "N/A";
        }

        // If price is an empty string or null, then default text says "N/A"
        if (TextUtils.isEmpty(shoePrice)) {
            shoePrice = "N/A";
        }

        // Update the Views with the attributes for the current shoe
        imageView.setImageBitmap(StringToBitmap(shoeImage));
        nameView.setText(shoeName);
        quantityView.setText(shoeQuantity);
        priceView.setText(shoePrice);
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
