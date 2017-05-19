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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import uk.co.mandilee.habittracker.data.HabitContract.DrinkEntry;

public class
Provider extends ContentProvider {

    private static final String LOG_TAG = Provider.class.getSimpleName();

    private static final int DRINKS = 100;

    private static final int DRINK_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_DRINKS, DRINKS);
        sUriMatcher.addURI(HabitContract.CONTENT_AUTHORITY, HabitContract.PATH_DRINKS + "/#", DRINK_ID);
    }

    private DbHelper mDbHelper;

    @Override
    public boolean onCreate() {
        mDbHelper = new DbHelper(getContext());
        return true;
    }

    @NonNull
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArguments, @Nullable String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case DRINKS:
                cursor = db.query(DrinkEntry.TABLE_NAME, projection, selection, selectionArguments, null, null, sortOrder);
                break;

            case DRINK_ID:
                selection = DrinkEntry._ID + "=?";
                selectionArguments = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = db.query(DrinkEntry.TABLE_NAME, projection, selection, selectionArguments, null, null, sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Query failed. Unknown URI: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

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

    private Uri insertDrink(Uri uri, ContentValues values) {

        Integer millilitres = values.getAsInteger(DrinkEntry.COLUMN_DRINK_MILLIMETRES);
        if (millilitres == null || millilitres <= 0) {
            throw new IllegalArgumentException("Drink requires valid millilitres");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(DrinkEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.e(LOG_TAG, "Row insertion failed for " + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String selection, String[] selectionArguments) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case DRINKS:
                return updateDrink(uri, contentValues, selection, selectionArguments);

            case DRINK_ID:
                selection = DrinkEntry._ID + "=?";
                selectionArguments = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateDrink(uri, contentValues, selection, selectionArguments);

            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateDrink(Uri uri, ContentValues values, String selection, String[] selectionArguments) {

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

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
