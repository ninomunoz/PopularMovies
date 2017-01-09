package com.example.popular_movies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.popular_movies.R;
import com.example.popular_movies.data.FavoriteMovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by i57198 on 1/5/17.
 */

public class FavoriteMovieAdapter extends CursorAdapter {

    private Context mContext;

    public FavoriteMovieAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_movie_poster, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView moviePoster = (ImageView)view.findViewById(R.id.iv_movie_poster);
        String posterPath = cursor.getString(cursor.getColumnIndexOrThrow(FavoriteMovieContract.MovieEntry.COLUMN_POSTER));

        Picasso.with(context)
                .load(posterPath)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(moviePoster);
    }
}
