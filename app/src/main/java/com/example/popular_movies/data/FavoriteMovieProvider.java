package com.example.popular_movies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by i57198 on 1/4/17.
 */

public class FavoriteMovieProvider extends ContentProvider {

    private static final int MOVIES = 1;
    private static final int MOVIE_ID = 2;
    private static final UriMatcher uriMatcher = getUriMatcher();

    private static UriMatcher getUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(FavoriteMovieContract.CONTENT_AUTHORITY, FavoriteMovieContract.TABLE_NAME, MOVIES);
        uriMatcher.addURI(FavoriteMovieContract.CONTENT_AUTHORITY, FavoriteMovieContract.TABLE_NAME + "/#", MOVIE_ID);
        return uriMatcher;
    }

    private FavoriteMovieDatabase favoriteMovieDatabase = null;

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case MOVIES:
                return FavoriteMovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return FavoriteMovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        favoriteMovieDatabase = new FavoriteMovieDatabase(context);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        String id = null;
        if (uriMatcher.match(uri) == MOVIE_ID) {
            // Query is for a single movie. Get the ID from the URI.
            id = uri.getPathSegments().get(1);
        }

        Cursor cursor = favoriteMovieDatabase.getMovies(id, projection, selection, selectionArgs, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        try {
            long id = favoriteMovieDatabase.addMovie(contentValues);
            getContext().getContentResolver().notifyChange(uri, null);
            return ContentUris.withAppendedId(FavoriteMovieContract.MovieEntry.CONTENT_URI, id);
        }
        catch (Exception e) {
            return null;
        }
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        String id = null;
        if(uriMatcher.match(uri) == MOVIE_ID) {
            //Delete is for one single movie. Get the ID from the URI.
            id = uri.getPathSegments().get(1);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return favoriteMovieDatabase.deleteMovies(id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        String id = null;
        if(uriMatcher.match(uri) == MOVIE_ID) {
            //Update is for one single movie. Get the ID from the URI.
            id = uri.getPathSegments().get(1);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return favoriteMovieDatabase.updateMovies(id, contentValues);
    }
}
