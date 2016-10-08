package com.example.android.deadstock.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * API Contract for the Deadstock app.
 */
public class ShoeContract {

    private ShoeContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.deadstock";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /** Possible path (appended to base content URI for possible URI's) */
    public static final String PATH_SHOES = "shoes";

    /**
     * Inner class that defines constant values for the shoes database table.
     * Each entry in the table represents a single shoe.
     */
    public static final class ShoeEntry implements BaseColumns {

        /** The content URI to access the shoe data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_SHOES);

        /** The MIME type of the {@link #CONTENT_URI} for a list of shoes */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOES;

        /**The MIME type of the {@link #CONTENT_URI} for a single shoe */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SHOES;

        /** Name of database table for shoes */
        public static final String TABLE_NAME = "shoes";

        /** Unique ID number for the shoe */
        public final static String _ID = BaseColumns._ID;

        /** Brand of the shoe */
        public final static String COLUMN_SHOE_BRAND ="brand";

        /** Possible values for brand of shoe */
        public static final int BRAND_OTHER = 0;
        public static final int BRAND_JORDAN = 1;
        public static final int BRAND_NIKE = 2;
        public static final int BRAND_ADIDAS = 3;
        public static final int BRAND_PUMA = 4;
        public static final int BRAND_REEBOK = 5;
        public static final int BRAND_CONVERSE = 6;

        /** Returns whether or not the given brand is valid */
        public static boolean isValidBrand(int brand) {
            if (brand == BRAND_JORDAN ||
                    brand == BRAND_NIKE ||
                    brand == BRAND_ADIDAS ||
                    brand == BRAND_PUMA ||
                    brand == BRAND_REEBOK ||
                    brand == BRAND_CONVERSE ||
                    brand == BRAND_OTHER) {
                return true;
            }
                return false;
        }

        /** Name of the shoe */
        public final static String COLUMN_SHOE_NAME = "name";

        /** Quantity of the shoe */
        public final static String COLUMN_SHOE_QUANTITY = "quantity";

        /** Price of the shoe */
        public final static String COLUMN_SHOE_PRICE = "price";

        /** Image of the shoe */
        public final static String COLUMN_SHOE_IMAGE = "image";
    }
}