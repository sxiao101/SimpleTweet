package com.codepath.apps.restclienttemplate;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.parceler.Parcels;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.Headers;

public class TweetDetailsActivity extends AppCompatActivity {
    ImageView ivProfileImage;
    TextView tvBody;
    TextView tvScreenName;
    TextView tvName;
    ImageView ivMedia;
    ImageButton btRetweet;
    ImageButton btFav;
    ImageButton btReply;
    TextView tvRetweet;
    TextView tvFav;
    Tweet currTweet;
    TwitterClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tweet_details);

        client = TwitterApp.getRestClient(this);

        ivProfileImage = findViewById(R.id.ivProfileImage);
        tvBody = findViewById(R.id.tvBody);
        tvName = findViewById(R.id.tvName);
        tvScreenName = findViewById(R.id.tvScreenName);
        ivMedia = findViewById(R.id.ivMedia);
        btRetweet = findViewById(R.id.btRetweet);
        btFav = findViewById(R.id.btFav);
        btReply = findViewById(R.id.btReply);
        tvRetweet = findViewById(R.id.tvRetweet);
        tvFav = findViewById(R.id.tvFav);

        currTweet = Parcels.unwrap(getIntent().getParcelableExtra(Tweet.class.getSimpleName()));

        tvBody.setText(currTweet.body);
        tvName.setText(currTweet.user.name);
        tvScreenName.setText(String.format("@%s", currTweet.user.screenName));
        tvFav.setText("" + currTweet.favCount);
        tvRetweet.setText("" + currTweet.rtCount);

        if (currTweet.rt) {
            btRetweet.setImageDrawable(getDrawable(R.drawable.ic_vector_retweet));
            btRetweet.setSelected(true);
        }
        if (currTweet.fav) {
            btFav.setImageDrawable(getDrawable(R.drawable.ic_vector_heart));
            btFav.setSelected(true);
        }
        // for rounded corners
        int radius = 30;
        int margin = 10;
        Glide.with(this).load(currTweet.user.profileImageUrl).fitCenter() // scale image to fill the entire ImageView
                .transform(new RoundedCornersTransformation(radius, margin)).into(ivProfileImage);
        if (!currTweet.mediaUrl.isEmpty()) {
            ivMedia.setVisibility(View.VISIBLE);
            Glide.with(this).load(currTweet.mediaUrl).fitCenter().into(ivMedia);
        }
        else{
            ivMedia.setVisibility(View.GONE);
        }

        btReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TweetDetailsActivity.this, ComposeActivity.class);
                i.putExtra("id", currTweet.id);
                i.putExtra("author", currTweet.user.screenName);
                startActivity(i);
            }
        });

        btRetweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!btRetweet.isSelected()) {
                    btRetweet.setImageDrawable(getDrawable(R.drawable.ic_vector_retweet));
                    btRetweet.setSelected(true);
                    client.reTweet(currTweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity retweet", "onSuccess!" + json.toString());
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("DetailsActivity retweet", "onFailure!" + response, throwable);
                        }
                    });
                    currTweet.rtCount++;
                    currTweet.rt = true;
                    tvRetweet.setText("" + currTweet.rtCount);
                } else {
                    btRetweet.setImageDrawable(getDrawable(R.drawable.ic_vector_retweet_stroke));
                    btRetweet.setSelected(false);
                    client.unreTweet(currTweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                            Log.i("DetailsActivity unretweet", "onSuccess!" + json.toString());
                        }

                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("DetailsActivity unretweet", "onFailure!" + response, throwable);
                        }
                    });
                    currTweet.rtCount--;
                    currTweet.rt = false;
                    tvRetweet.setText("" + currTweet.rtCount);
                }
            }
        });

        btFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!btFav.isSelected()) {
                    Log.d("Status", "liking");
                    btFav.setImageDrawable(getDrawable(R.drawable.ic_vector_heart));
                    btFav.setSelected(true);
                    client.likeTweet(currTweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("DetailsActivity like", "onFailure!" + response, throwable);
                        }
                    });
                    currTweet.favCount++;
                    currTweet.fav = true;
                    tvFav.setText("" + currTweet.favCount);
                } else {
                    Log.d("Status", "unliking");
                    btFav.setImageDrawable(getDrawable(R.drawable.ic_vector_heart_stroke));
                    btFav.setSelected(false);
                    client.unlikeTweet(currTweet.id, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Headers headers, JSON json) {
                        }
                        @Override
                        public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                            Log.i("DetailsActivity unlike", "onFailure!" + response, throwable);
                        }
                    });
                    currTweet.favCount--;
                    currTweet.fav = false;
                    tvFav.setText("" + currTweet.favCount);
                }
            }
        });

    }
}