package com.example.android.deadstock;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.deadstock.data.ShoeContract.ShoeEntry;

/**
 * Created by noahkim on 10/4/16.
 */

public class ShoeCursorAdapter extends CursorAdapter {

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
        TextView nameView = (TextView) view.findViewById(R.id.name);
        TextView quantityView = (TextView) view.findViewById(R.id.quantity);
        TextView priceView = (TextView) view.findViewById(R.id.price);

        // Find columns of pet attributes
        int nameColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ShoeEntry.COLUMN_SHOE_PRICE);

        // Read shoe attributes from Cursor for current shoe
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

        // Update the TexViews with the attributes for the current shoe
        nameView.setText(shoeName);
        quantityView.setText(shoeQuantity);
        priceView.setText(shoePrice);
    }
}
