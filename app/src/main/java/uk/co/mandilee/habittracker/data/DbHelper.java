package uk.co.mandilee.habittracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.co.mandilee.habittracker.data.HabitContract.DrinkEntry;

class DbHelper extends SQLiteOpenHelper {

    /**
     * Database name
     */
    private static final String DATABASE_NAME = "shelter.db";

    /**
     * Database version
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * New instance of {@link DbHelper}.
     * @param context of app
     */
    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Create the database the first time round
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // The sql to create the table in a string
        String SQL_CREATE_DRINKS_TABLE = "CREATE TABLE " + DrinkEntry.TABLE_NAME + " ("
                + DrinkEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + DrinkEntry.COLUMN_DRINK_TYPE + " TEXT, "
                + DrinkEntry.COLUMN_DRINK_MILLIMETRES + " INTEGER NOT NULL, "
                + DrinkEntry.COLUMN_DRINK_DATETIME + " DATETIME DEFAULT CURRENT_TIMESTAMP);";

        // run the sql statement
        db.execSQL(SQL_CREATE_DRINKS_TABLE);
    }

    /**
     * Upgrade the database if required
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Nothing to do
        // Database is still version one
    }
}
