package com.example.popular_movies.activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.popular_movies.R;
import com.example.popular_movies.async.FetchReviewTask;
import com.example.popular_movies.async.FetchTrailerTask;
import com.example.popular_movies.data.FavoriteMovieContract;
import com.example.popular_movies.model.Movie;
import com.example.popular_movies.model.Review;
import com.example.popular_movies.model.Trailer;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MovieDetailActivity extends AppCompatActivity implements FetchTrailerTask.IFetchTrailerListener, FetchReviewTask.IFetchReviewListener {

    Movie mCurrentMovie;
    String mShareTrailerUrl;

    @BindView(R.id.iv_movie_detail_thumbnail) ImageView thumbnail;
    @BindView(R.id.tv_movie_detail_title) TextView tvTitle;
    @BindView(R.id.tv_movie_detail_release_date) TextView tvReleaseDate;
    @BindView(R.id.tv_movie_detail_rating) TextView tvRating;
    @BindView(R.id.tv_movie_detail_synopsis) TextView tvSynopsis;
    @BindView(R.id.ll_movie_detail_trailers) LinearLayout llTrailers;
    @BindView(R.id.ll_movie_detail_reviews) LinearLayout llReviews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        ButterKnife.bind(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(getString(R.string.movie_details));

        mCurrentMovie = getIntent().getParcelableExtra(MoviePosterActivity.INTENT_EXTRA_MOVIE);

        loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);

        if (isCurrentMovieFavorited()) {
            Drawable star = ContextCompat.getDrawable(this, R.drawable.ic_star_checked);
            star.setColorFilter(ContextCompat.getColor(this, R.color.yellow), PorterDuff.Mode.MULTIPLY);
            menu.findItem(R.id.action_favorite).setIcon(star);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_favorite:
                onClickFavorite(item);
                return true;
            case R.id.action_share:
                onClickShare();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onClickShare() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_trailer_msg) + mCurrentMovie.title + ":\n" + mShareTrailerUrl);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_trailer_title)));
    }

    private void onClickFavorite(MenuItem item) {
        String msg;

        if (!isCurrentMovieFavorited()) {
            addToFavorites();
            Drawable star = ContextCompat.getDrawable(this, R.drawable.ic_star_checked);
            star.setColorFilter(ContextCompat.getColor(this, R.color.yellow), PorterDuff.Mode.MULTIPLY);
            item.setIcon(star);
            msg = mCurrentMovie.title + getString(R.string.added_to_favorites);
        }
        else {
            removeFromFavorites();
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_star_unchecked));
            msg = mCurrentMovie.title + getString(R.string.removed_from_favorites);
        }

        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void loadData() {
        if (mCurrentMovie != null) {
            Picasso.with(this).load(mCurrentMovie.posterPath).into(thumbnail);
            tvTitle.setText(mCurrentMovie.title);
            tvReleaseDate.setText(formatReleaseDate(mCurrentMovie.releaseDate));
            tvRating.setText(formatRating(mCurrentMovie.rating));
            tvSynopsis.setText(mCurrentMovie.description);

            getTrailers();
            getReviews();
        }
    }

    private void addToFavorites() {
        ContentValues cv = new ContentValues();
        cv.put(FavoriteMovieContract.MovieEntry._ID, mCurrentMovie.id);
        cv.put(FavoriteMovieContract.MovieEntry.COLUMN_TITLE, mCurrentMovie.title);
        cv.put(FavoriteMovieContract.MovieEntry.COLUMN_DESCRIPTION, mCurrentMovie.description);
        cv.put(FavoriteMovieContract.MovieEntry.COLUMN_POSTER, mCurrentMovie.posterPath);
        cv.put(FavoriteMovieContract.MovieEntry.COLUMN_RELEASE_DATE, mCurrentMovie.releaseDate);
        cv.put(FavoriteMovieContract.MovieEntry.COLUMN_RATING, mCurrentMovie.rating);

        getContentResolver().insert(FavoriteMovieContract.MovieEntry.CONTENT_URI, cv);
    }

    private boolean isCurrentMovieFavorited() {
        Cursor cursor = getContentResolver().query(
                FavoriteMovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(mCurrentMovie.id)).build(),
                null,
                null,
                null,
                null
        );

        return cursor.getCount() > 0;
    }

    private void removeFromFavorites() {
        getContentResolver().delete(
                FavoriteMovieContract.MovieEntry.CONTENT_URI.buildUpon().appendPath(Integer.toString(mCurrentMovie.id)).build(),
                null,
                null
        );
    }

    private void getTrailers() {
        FetchTrailerTask fetchTrailerTask = new FetchTrailerTask(this);
        fetchTrailerTask.execute(Integer.toString(mCurrentMovie.id));
    }

    private void getReviews() {
        FetchReviewTask fetchReviewTask = new FetchReviewTask(this);
        fetchReviewTask.execute(Integer.toString(mCurrentMovie.id));
    }

    private String formatReleaseDate(String date) {
        String prefix = getString(R.string.release_date);
        return prefix + date;
    }

    private String formatRating(double rating) {
        String strRating = Double.toString(rating);
        String prefix = getString(R.string.rating);
        String suffix = getString(R.string.rating_suffix);
        return prefix + strRating + suffix;
    }

    @Override
    public void onTrailerFetchComplete(ArrayList<Trailer> trailers) {
        for (int i = 0; i < trailers.size(); i++) {
            View view = getLayoutInflater().inflate(R.layout.trailer, llTrailers, false);
            ImageView trailerThumbnail = (ImageView)view.findViewById(R.id.iv_trailer_thumbnail);
            TextView trailerLbl = (TextView)view.findViewById(R.id.tv_trailer_lbl);

            final Trailer trailer = trailers.get(i);

            if (i == 0) {
                mShareTrailerUrl = trailer.video.toString();
            }

            trailerThumbnail.setImageResource(R.drawable.ic_play);

            trailerThumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(Intent.ACTION_VIEW, trailer.video));
                }
            });

            trailerLbl.setText(getString(R.string.trailer) + (i+1));
            llTrailers.addView(view);
        }
    }

    @Override
    public void onFetchReviewComplete(ArrayList<Review> reviews) {

        int reviewsCount = reviews.size();
        int padding = getResources().getDimensionPixelSize(R.dimen.detail_padding);

        if (reviewsCount == 0) {
            TextView tvNoReviews = new TextView(this);
            tvNoReviews.setText(getString(R.string.no_reviews));
            tvNoReviews.setPadding(0, padding, 0, 0);
            tvNoReviews.setTypeface(null, Typeface.ITALIC);
            tvNoReviews.setGravity(Gravity.CENTER_HORIZONTAL);
            llReviews.addView(tvNoReviews);
        }
        else {

            for (int i = 0; i < reviews.size(); i++) {
                final Review review = reviews.get(i);

                TextView tvReview = new TextView(this);
                TextView tvAuthor = new TextView(this);

                tvReview.setText(review.mReview);

                tvReview.setPadding(0, padding, 0, 0);
                llReviews.addView(tvReview);

                tvAuthor.setText(review.mAuthor);
                tvAuthor.setGravity(Gravity.RIGHT);
                tvAuthor.setTypeface(null, Typeface.BOLD_ITALIC);
                tvAuthor.setPadding(0, padding, 0, padding);
                llReviews.addView(tvAuthor);

                // Add divider after all but last review
                if (i != reviews.size() - 1) {
                    getLayoutInflater().inflate(R.layout.divider, llReviews, true);
                }
            }
        }
    }
}
