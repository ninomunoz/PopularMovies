package com.example.popular_movies.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.example.popular_movies.R;
import com.example.popular_movies.adapter.FavoriteMovieAdapter;
import com.example.popular_movies.adapter.MoviePosterAdapter;
import com.example.popular_movies.async.FetchMovieTask;
import com.example.popular_movies.data.FavoriteMovieContract;
import com.example.popular_movies.model.Movie;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

public class MoviePosterActivity extends AppCompatActivity implements FetchMovieTask.IFetchMovieListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final String INTENT_EXTRA_MOVIE = "IntentExtraMovie";
    public static final int FAVORITE_MOVIE_LOADER_ID = 1;

    private final String BUNDLE_MOVIES_KEY = "BundleMoviesKey";

    @BindView(R.id.gridview_movie_posters) GridView mGridView;
    MoviePosterAdapter mAdapter;
    FavoriteMovieAdapter mFavoriteMovieAdapter;
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_poster);
        ButterKnife.bind(this);

        String sortBy = getSortPreference();

        if (sortBy.equals(getString(R.string.favorites))) {
            mFavoriteMovieAdapter = new FavoriteMovieAdapter(this, null);
            mGridView.setAdapter(mFavoriteMovieAdapter);
            getFavorites();
        }
        else {
            if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_MOVIES_KEY)) {
                ArrayList<Movie> movies = savedInstanceState.getParcelableArrayList(BUNDLE_MOVIES_KEY);
                mAdapter = new MoviePosterAdapter(this, movies);
                updateTitle(sortBy);
            }
            else {
                mAdapter = new MoviePosterAdapter(this, new ArrayList<Movie>());
                getMovies(sortBy);
            }

            mGridView.setAdapter(mAdapter);
        }
    }

    @OnItemClick(R.id.gridview_movie_posters)
    void onItemClick(int position) {
        String sortBy = getSortPreference();
        Movie movie = null;

        if (sortBy.equals(getString(R.string.favorites))) {
            Cursor cursor = (Cursor)mFavoriteMovieAdapter.getItem(position);
            int movieId = cursor.getInt(cursor.getColumnIndex(FavoriteMovieContract.MovieEntry._ID));
            String title = cursor.getString(cursor.getColumnIndex(FavoriteMovieContract.MovieEntry.COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndex(FavoriteMovieContract.MovieEntry.COLUMN_DESCRIPTION));
            String posterPath = cursor.getString(cursor.getColumnIndex(FavoriteMovieContract.MovieEntry.COLUMN_POSTER));
            String releaseDate = cursor.getString(cursor.getColumnIndex(FavoriteMovieContract.MovieEntry.COLUMN_RELEASE_DATE));
            double rating = cursor.getDouble(cursor.getColumnIndex(FavoriteMovieContract.MovieEntry.COLUMN_RATING));

            movie = new Movie(movieId, title, description, posterPath, releaseDate, rating);
        }
        else {
            movie = mAdapter.getItem(position);
        }

        Intent intent = new Intent(MoviePosterActivity.this, MovieDetailActivity.class);
        intent.putExtra(INTENT_EXTRA_MOVIE, movie);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Get shared preferences
        if (mSharedPreferences == null) {
            mSharedPreferences = this.getSharedPreferences(getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
        }

        switch (item.getItemId()) {
            case R.id.sort_by_popularity:
                String sortByPopularity = getString(R.string.sort_by_popularity);
                mSharedPreferences.edit().putString(
                        getString(R.string.sort_pref_key),
                        sortByPopularity).apply();
                getMovies(sortByPopularity);
                return true;
            case R.id.sort_by_rating:
                String sortByRating = getString(R.string.sort_by_rating);
                mSharedPreferences.edit().putString(
                        getString(R.string.sort_pref_key),
                       sortByRating).apply();
                getMovies(sortByRating);
                return true;
            case R.id.sort_by_favorites:
                String sortByFavorites = getString(R.string.favorites);
                mSharedPreferences.edit().putString(
                        getString(R.string.sort_pref_key),
                        sortByFavorites).apply();
                getFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        String sortBy = getSortPreference();
        if (!sortBy.equals(getString(R.string.favorites))) {
            outState.putParcelableArrayList(BUNDLE_MOVIES_KEY, mAdapter.getMovies());
        }
        super.onSaveInstanceState(outState);
    }

    void getMovies(String sortBy) {
        if (!isOnline()) {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_LONG).show();
        }
        else if (!sortBy.equals(getString(R.string.favorites))) {
            mGridView.setAdapter(null);

            if (mAdapter == null) {
                mAdapter = new MoviePosterAdapter(this, new ArrayList<Movie>());
            }

            mGridView.setAdapter(mAdapter);
            FetchMovieTask fetchMovieTask = new FetchMovieTask(this);
            fetchMovieTask.execute(sortBy);

            updateTitle(sortBy);
        }
    }

    private void getFavorites() {
        mGridView.setAdapter(null);

        if (mFavoriteMovieAdapter == null) {
            mFavoriteMovieAdapter = new FavoriteMovieAdapter(this, null);
            mGridView.setAdapter(mFavoriteMovieAdapter);
            getSupportLoaderManager().initLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
        }
        else {
            mGridView.setAdapter(mFavoriteMovieAdapter);
            getSupportLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
        }

        updateTitle(getString(R.string.favorites));
    }

    void updateTitle(String sortBy) {
        if (sortBy.equals(getString(R.string.sort_by_popularity))) {
            setTitle(getString(R.string.movies_by_popularity));
        }
        else if (sortBy.equals(getString(R.string.sort_by_rating))) {
            setTitle(getString(R.string.movies_by_rating));
        }
        else if (sortBy.equals(getString(R.string.favorites))) {
            setTitle(getString(R.string.favorite_movies));
        }
    }

    boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private String getSortPreference() {
        mSharedPreferences = getSharedPreferences(getString(R.string.shared_preference_file_key), Context.MODE_PRIVATE);
        String sortBy = mSharedPreferences.getString(
                getString(R.string.sort_pref_key),
                getString(R.string.sort_by_popularity));
        return sortBy;
    }

    @Override
    public void onMovieFetchComplete(ArrayList<Movie> movies) {
        if (movies != null) {
            mAdapter.clear();
            mAdapter.addAll(movies);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, FavoriteMovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mFavoriteMovieAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoriteMovieAdapter.swapCursor(null);
    }
}