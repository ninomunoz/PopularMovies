package com.example.popular_movies.async;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.example.popular_movies.BuildConfig;
import com.example.popular_movies.model.Review;

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

public class FetchReviewTask extends AsyncTask<String, Void, ArrayList<Review>> {

    private static final String LOG_TAG = FetchReviewTask.class.getSimpleName();

    private IFetchReviewListener mListener;

    public FetchReviewTask(Context context) { this.mListener = (IFetchReviewListener)context; }

    @Override
    protected ArrayList<Review> doInBackground(String... params) {
        // If there's no movie id provided, there's nothing to look up.
        if (params.length == 0) {
            return null;
        }

        // These two need to be declared outside the try/catch so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Will contain the raw JSON response as a string.
        String reviewJsonStr = null;

        try {
            // Construct the URL for themoviedb query
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("http")
                    .authority("api.themoviedb.org")
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(params[0])
                    .appendPath("reviews")
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
            reviewJsonStr = buffer.toString();

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
            return getReviewDataFromJson(reviewJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        // This will only happen if there was an error getting or parsing the review data.
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Review> reviews) {
        super.onPostExecute(reviews);
        mListener.onFetchReviewComplete(reviews);
    }

    private ArrayList<Review> getReviewDataFromJson(String reviewJsonStr) throws JSONException
    {
        // These are the names of the JSON objects that need to be extracted.
        final String REVIEW_LIST = "results";
        final String REVIEW_AUTHOR = "author";
        final String REVIEW_CONTENT = "content";

        JSONObject reviewJson = new JSONObject(reviewJsonStr);
        JSONArray reviewList = reviewJson.getJSONArray(REVIEW_LIST);

        ArrayList<Review> reviews =  new ArrayList<Review>();

        for (int i = 0; i < reviewList.length(); i++)
        {
            String author;
            String content;

            // Get the JSON object representing the review
            JSONObject reviewObject = reviewList.getJSONObject(i);

            // Extract review data
            author = reviewObject.getString(REVIEW_AUTHOR);
            content = reviewObject.getString(REVIEW_CONTENT);

            // Create review object and add to ArrayList
            reviews.add(new Review(author, content));
        }

        return reviews;
    }

    public interface IFetchReviewListener {
        void onFetchReviewComplete(ArrayList<Review> reviews);
    }
}
