package com.example.popular_movies.model;

import android.net.Uri;

/**
 * Created by i57198 on 1/2/17.
 */

public class Trailer {

    public Uri video;

    public Trailer(String key) {
        video = getVideoFromKey(key);
    }

    private Uri getVideoFromKey(String key) {
        Uri.Builder builder = new Uri.Builder();
        builder.scheme("http")
                .authority("youtube.com")
                .appendPath("watch")
                .appendQueryParameter("v", key);

        return builder.build();
    }
}