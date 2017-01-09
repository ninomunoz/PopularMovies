package com.example.popular_movies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import static com.example.popular_movies.data.FavoriteMovieContract.MovieEntry;

/**
 * Created by i57198 on 1/5/17.
 */

public class FavoriteMovieDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FavoriteMovieDatabase.db";
    private static final int DATABASE_VERSION = 2;

    private static final String SQL_CREATE = "CREATE TABLE " + FavoriteMovieContract.TABLE_NAME + " (" +
            MovieEntry._ID + " INTEGER PRIMARY KEY, " +
            MovieEntry.COLUMN_TITLE + " TEXT, " +
            MovieEntry.COLUMN_DESCRIPTION + " TEXT, " +
            MovieEntry.COLUMN_POSTER + " TEXT, " +
            MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
            MovieEntry.COLUMN_RATING + " REAL)";


    private static final String SQL_DROP = "DROP TABLE IF EXISTS " + FavoriteMovieContract.TABLE_NAME;

    FavoriteMovieDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DROP);
        onCreate(db);
    }

    public Cursor getMovies(String id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        sqLiteQueryBuilder.setTables(FavoriteMovieContract.TABLE_NAME);

        if (id != null) {
            sqLiteQueryBuilder.appendWhere("_id = " + id);
        }

        Cursor cursor = sqLiteQueryBuilder.query(getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder);

        return cursor;
    }

    public long addMovie(ContentValues values) throws SQLException {
        long id = getWritableDatabase().insert(FavoriteMovieContract.TABLE_NAME, "", values);

        if (id <= 0) {
            throw new SQLException("Failed to add movie");
        }

        return id;
    }

    public int deleteMovies(String id) {
        if (id == null) {
            return getWritableDatabase().delete(FavoriteMovieContract.TABLE_NAME, null, null);
        }
        else {
            return getWritableDatabase().delete(FavoriteMovieContract.TABLE_NAME, "_id=?", new String[]{id});
        }
    }

    public int updateMovies(String id, ContentValues values) {
        if (id == null) {
            return getWritableDatabase().update(FavoriteMovieContract.TABLE_NAME, values, null, null);
        }
        else {
            return getWritableDatabase().update(FavoriteMovieContract.TABLE_NAME, values, "_id=?", new String[]{id});
        }
    }

}
