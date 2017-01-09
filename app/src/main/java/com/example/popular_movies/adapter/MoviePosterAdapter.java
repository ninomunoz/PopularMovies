package com.example.popular_movies.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.popular_movies.R;
import com.example.popular_movies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by i57198 on 11/23/16.
 */

public class MoviePosterAdapter extends ArrayAdapter<Movie> {

    public MoviePosterAdapter(Context context, ArrayList<Movie> movies) {
        super(context, R.layout.list_item_movie_poster, movies);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        Movie movie = getItem(position);
        ViewHolder viewHolder;

        // Check if an existing view is being reused, otherwise inflate the view
        if (view == null) {
            view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_movie_poster, parent, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder); // attach viewHolder object to View
        }
        else {
            // View is being recycled, retrieve the viewHolder object from tag
            viewHolder = (ViewHolder) view.getTag();
        }

        Picasso.with(getContext())
                .load(movie.posterPath)
                .placeholder(R.drawable.ic_placeholder)
                .error(R.drawable.ic_error)
                .into(viewHolder.moviePoster);

        return view;
    }

    public ArrayList<Movie> getMovies() {
        ArrayList<Movie> movies = new ArrayList<Movie>();
        for (int i = 0; i < getCount(); i++) {
            movies.add(getItem(i));
        }

        return movies;
    }

    // ViewHolder speeds up population of GridView by caching the imageView
    static class ViewHolder {
        @BindView(R.id.iv_movie_poster) ImageView moviePoster;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}