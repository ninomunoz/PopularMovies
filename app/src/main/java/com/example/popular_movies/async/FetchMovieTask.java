package com.example.popular_movies.async;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.popular_movies.BuildConfig;
import com.example.popular_movies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by i57198 on 12/2/16.
 */

public class FetchMovieTask extends AsyncTask<String, Void, ArrayList<Movie>> {

    private static final String LOG_TAG = FetchMovieTask.class.getSimpleName();

    private IFetchMovieListener mListener;

    public FetchMovieTask(Context context) {
        this.mListener = (IFetchMovieListener)context;
    }

    @Override
    protected ArrayList<Movie> doInBackground(String... params) {
        // If there's no sort method provided, there's nothing to look up. Verify size of params.
        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String movieJsonStr = null;

        try {
            // Construct the URL for themoviedb query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(params[0])
                    .appendQueryParameter("api_key", BuildConfig.THEMOVIEDB_API_KEY)
                    .appendQueryParameter("language", "en-US");

            String urlString = builder.build().toString();
            URL url = new URL(urlString);

            // Create the request to themoviedb, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            movieJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the movie data, there's no point in attempting to parse it.
            return null;
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            return getMovieDataFromJson(movieJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the movie data.
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Movie> movies) {
        super.onPostExecute(movies);
        mListener.onMovieFetchComplete(movies);
    }

    private ArrayList<Movie> getMovieDataFromJson(String movieJsonStr) throws JSONException
    {
        // These are the names of the JSON objects that need to be extracted.
        final String MOVIE_LIST = "results";
        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "title";
        final String MOVIE_DESCRIPTION = "overview";
        final String MOVIE_POSTER = "poster_path";
        final String MOVIE_RELEASE_DATE = "release_date";
        final String MOVIE_RATING = "vote_average";

        JSONObject movieJson = new JSONObject(movieJsonStr);
        JSONArray movieList = movieJson.getJSONArray(MOVIE_LIST);

        ArrayList<Movie> movies =  new ArrayList<Movie>();

        for (int i = 0; i < movieList.length(); i++)
        {
            int id;
            String title, description, posterPath, releaseDate;
            double rating;

            // Get the JSON object representing the movie
            JSONObject movieObject = movieList.getJSONObject(i);

            // Extract movie data
            id = movieObject.getInt(MOVIE_ID);
            title = movieObject.getString(MOVIE_TITLE);
            description = movieObject.getString(MOVIE_DESCRIPTION);
            posterPath = movieObject.getString(MOVIE_POSTER);
            releaseDate = movieObject.getString(MOVIE_RELEASE_DATE);
            rating = movieObject.getDouble(MOVIE_RATING);

            // Create Movie object and add to movies
            Movie movie = new Movie(id, title, description, posterPath, releaseDate, rating);
            movies.add(movie);
        }

        return movies;
    }

    public interface IFetchMovieListener {
        void onMovieFetchComplete(ArrayList<Movie> movies);
    }
}
