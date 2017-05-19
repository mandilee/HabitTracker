package uk.co.mandilee.habittracker.data;

import android.content.ContentResolver;
import android.provider.BaseColumns;

class HabitContract {

    static final String CONTENT_AUTHORITY = "uk.co.mandilee.habittracker";
    static final String PATH_DRINKS = "drinks";

    private HabitContract() {
        // Empty constructor to stop accidental
        // instantiation of the contract class
    }

    static final class DrinkEntry implements BaseColumns {

        static final String CONTENT_TYPE_LIST = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DRINKS;

        static final String CONTENT_TYPE_ITEM = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_DRINKS;

        final static String TABLE_NAME = "drinks";

        final static String _ID = BaseColumns._ID;

        // column to record the type of drink i.e. water, beer, coca cola
        final static String COLUMN_DRINK_TYPE = "type";

        // column to record how much drink in millilitres i.e. 250ml
        final static String COLUMN_DRINK_MILLIMETRES = "millilitres";

        // column for date and time of the drink, auto-set on insertion
        final static String COLUMN_DRINK_DATETIME = "datetime";
    }

}