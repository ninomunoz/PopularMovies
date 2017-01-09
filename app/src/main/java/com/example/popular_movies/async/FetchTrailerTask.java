package com.example.popular_movies.async;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.popular_movies.BuildConfig;
import com.example.popular_movies.model.Trailer;

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
 * Created by i57198 on 1/2/17.
 */

public class FetchTrailerTask extends AsyncTask<String, Void, ArrayList<Trailer>> {

    private static final String LOG_TAG = FetchTrailerTask.class.getSimpleName();

    private IFetchTrailerListener mListener;

    public FetchTrailerTask(Context context) { this.mListener = (IFetchTrailerListener)context; }

    @Override
    protected ArrayList<Trailer> doInBackground(String... params) {
        // If there's no movie id provided, there's nothing to look up. Verify size of params.
        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String trailerJsonStr = null;

        try {
            // Construct the URL for themoviedb query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(params[0])
                    .appendPath("videos")
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
            trailerJsonStr = buffer.toString();

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the trailer data, there's no point in attempting to parse it.
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
            return getTrailerDataFromJson(trailerJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the trailer data.
        return null;
    }

    private ArrayList<Trailer> getTrailerDataFromJson(String trailerJsonStr) throws JSONException
    {
        // These are the names of the JSON objects that need to be extracted.
        final String TRAILER_LIST = "results";
        final String TRAILER_KEY = "key";

        JSONObject trailerJson = new JSONObject(trailerJsonStr);
        JSONArray trailerList = trailerJson.getJSONArray(TRAILER_LIST);

        ArrayList<Trailer> trailers =  new ArrayList<Trailer>();

        for (int i = 0; i < trailerList.length(); i++)
        {
            String key;

            // Get the JSON object representing the trailer
            JSONObject trailerObject = trailerList.getJSONObject(i);

            // Extract trailer data
            key = trailerObject.getString(TRAILER_KEY);

            // Create trailer object and add to ArrayList
            trailers.add(new Trailer(key));
        }

        return trailers;
    }

    @Override
    protected void onPostExecute(ArrayList<Trailer> trailers) {
        super.onPostExecute(trailers);
        mListener.onTrailerFetchComplete(trailers);
    }

    public interface IFetchTrailerListener {
        void onTrailerFetchComplete(ArrayList<Trailer> trailers);
    }
}
