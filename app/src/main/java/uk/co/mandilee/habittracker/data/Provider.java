package uk.co.mandilee.habittracker.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import uk.co.mandilee.habittracker.data.HabitContract.DrinkEntry;

public class
Provider extends ContentProvider {

    /**
     * tag for log messages
     */
    private static final String LOG_TAG = Provider.class.getSimpleName();

    /**
     * uri matcher code for drinks table
     */
    private static final int DRINKS = 100;

    /** uri matcher code for single drink in drinks table */
    private static final int DRINK_ID = 101;

    /** URIMatcher object to match a content uri to a corresponding code */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // statics run first time anything from this class is called
    static {
        // URI provides access to multiple rows from the table
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_DRINKS, DRINKS);

        // URI to get a single row from the table
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_DRINKS + "/#", DRINK_ID);
    }

    /** database helper object */
    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @NonNull
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArguments, @Nullable String sortOrder) {

        // get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // cursor to hold query result
        Cursor cursor;

        // match uri to specific code if possible
        switch (sUriMatcher.match(uri)) {
            case DRINKS:
                cursor = db.query(DrinkEntry.TABLE_NAME, projection, selection, selectionArguments,
                        null, null, sortOrder);
                break;

            case DRINK_ID:
                selection = DrinkEntry._ID + "=?";
                selectionArguments = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DrinkEntry.TABLE_NAME, projection, selection, selectionArguments,
                        null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Query failed. Unknown URI: " + uri);
        }

        // if data changes, need to update cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // return the cursor
        return cursor;
    }

    @NonNull
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DRINKS:
                return DrinkEntry.CONTENT_TYPE_LIST;

            case DRINK_ID:
                return DrinkEntry.CONTENT_TYPE_ITEM;

            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        switch (sUriMatcher.match(uri)) {
            case DRINKS:
                return insertDrink(uri, contentValues);

            default:
                throw new IllegalArgumentException("Cannot insert " + uri);
        }
    }

    /** insert a drink into the database with the given values
     * return the new content uri for the new row
     */
    private Uri insertDrink(Uri uri, ContentValues values) {

        // Check that the type is not null
        String type = values.getAsString(DrinkEntry.COLUMN_DRINK_TYPE);
        if (type == null) {
            throw new IllegalArgumentException("Drink type is required");
        }

        // check millilitres is not null and greater than 0
        Integer millilitres = values.getAsInteger(DrinkEntry.COLUMN_DRINK_MILLIMETRES);
        if (millilitres == null || millilitres <= 0) {
            throw new IllegalArgumentException("Drink requires valid millilitres");
        }

        // datetime is auto added. no need to check

        // get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // insert drink with given values
        long id = db.insert(DrinkEntry.TABLE_NAME, null, values);
        // id == -1 when insert failed
        // log error and return null
        if (id == -1) {
            Log.e(LOG_TAG, "Row insertion failed for " + uri);
            return null;
        }

        // tell listeners data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        // return new uri wuth new id appended to end
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArguments) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case DRINKS:
                return updateDrink(uri, contentValues, selection, selectionArguments);

            case DRINK_ID:
                // extract the id so we know what to update
                selection = DrinkEntry._ID + "=?";
                selectionArguments = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDrink(uri, contentValues, selection, selectionArguments);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * update drinks with the given values
     * apply to specified rows
     * return number of rows updated successfully
     */
    private int updateDrink(Uri uri, ContentValues values, String selection,
                            String[] selectionArguments) {

        // Check that the type is not null
        if (values.containsKey(DrinkEntry.COLUMN_DRINK_TYPE)) {
            String type = values.getAsString(DrinkEntry.COLUMN_DRINK_TYPE);
            if (type == null) {
                throw new IllegalArgumentException("Drink type is required");
            }
        }

        if (values.containsKey(DrinkEntry.COLUMN_DRINK_MILLIMETRES)) {
            Integer millilitres = values.getAsInteger(DrinkEntry.COLUMN_DRINK_MILLIMETRES);
            if (millilitres == null || millilitres <= 0) {
                throw new IllegalArgumentException("Drink requires valid millilitres");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(DrinkEntry.TABLE_NAME, values, selection, selectionArguments);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArguments) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int rowsDeleted;

        switch (sUriMatcher.match(uri)) {
            case DRINKS:
                rowsDeleted = db.delete(DrinkEntry.TABLE_NAME, selection, selectionArguments);
                break;

            case DRINK_ID:
                selection = DrinkEntry._ID + "=?";
                selectionArguments = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(DrinkEntry.TABLE_NAME, selection, selectionArguments);
                break;

            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }
}
