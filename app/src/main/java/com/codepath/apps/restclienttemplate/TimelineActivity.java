package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity {

    public static final String TAG = "TimelineActivity";
    public final int REQUEST_CODE = 20;

    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    Button btLogout;
    SwipeRefreshLayout swipeContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

   //     getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_launcher_twitter_round);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
    //    getSupportActionBar().setDisplayShowTitleEnabled(false);

        client = TwitterApp.getRestClient(this);

        btLogout = findViewById(R.id.btLogout);
        // Find the recycler view
        rvTweets = findViewById(R.id.rvTweets);
        tweets = new ArrayList<>();

        TweetsAdapter.OnClickListener onClickListener = new TweetsAdapter.OnClickListener() {
            @Override
            public void onReplyClicked(int position) {
                Log.d("TimelineActivity", "Reply click at " + position);
                // create the new activity
                Intent i = new Intent(TimelineActivity.this, ComposeActivity.class);
                Tweet tweet = tweets.get(position);
                i.putExtra("id", tweet.id);
                i.putExtra("author", tweet.user.screenName);
                //i.putExtra("from", "timeline");
                startActivityForResult(i, REQUEST_CODE);
            }
        };

        TweetsAdapter.TweetItemListener listener = new TweetsAdapter.TweetItemListener() {
            @Override
            public void onRetweetClicked(int position) {
                Log.d("TimelineActivity", "Retweet click at " + position);
                // create the new activity
                Tweet tweet = tweets.get(position);
                client.reTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i("TimelineActivity retweet", "onSuccess!" + json.toString());
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(TAG, "onFailure!" + response, throwable);
                    }
                });
            }

            @Override
            public void onRetweetUnclicked(int position) {
                Log.d("TimelineActivity", "Retweet unclick at " + position);
                // create the new activity
                Tweet tweet = tweets.get(position);
                client.unreTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(TAG, "onFailure!" + response, throwable);
                    }
                });
            }

            @Override
            public void onLikeClicked(int position) {
                Log.d("TimelineActivity", "Fav click at " + position);
                // create the new activity
                Tweet tweet = tweets.get(position);
                client.likeTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess!" + json.toString());

                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(TAG, "onFailure!" + response, throwable);
                    }
                });
            }
            @Override
            public void onLikeUnclicked(int position) {
                Log.d("TimelineActivity", "Fav unclick at " + position);
                // create the new activity
                Tweet tweet = tweets.get(position);
                client.unlikeTweet(tweet.id, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.i(TAG, "onFailure!" + response, throwable);
                    }
                });
            }
        };

        // init the list of tweets and adapter
        adapter = new TweetsAdapter(this, tweets, onClickListener, listener);// recycler view setup: layout manager and the adapter
        rvTweets.setLayoutManager(new LinearLayoutManager(this));
        rvTweets.setAdapter(adapter);
        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                fetchTimelineAsync(0);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        populateHomeTimeline();


    }
    public void onComposeAction(View view) {
            // navigate to the compose activity
            Intent intent = new Intent(this, ComposeActivity.class);
            intent.putExtra("id", "");
            startActivityForResult(intent, REQUEST_CODE);
    }

    public void fetchTimelineAsync(int page) {
        // Send the network request to fetch the updated data
        // `client` here is an instance of Android Async HTTP
        // getHomeTimeline is an example endpoint.
        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                // Remember to CLEAR OUT old items before appending in the new ones
                adapter.clear();
                // ...the data has come back, add new items to your adapter...
                try {
                    adapter.addAll(Tweet.fromJsonArray(json.jsonArray));
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
                // Now we call setRefreshing(false) to signal refresh has finished
                swipeContainer.setRefreshing(false);
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.d(TAG, "Fetch timeline error: " + response);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            // Get data from the intent (tweet)
            Tweet tweet = Parcels.unwrap(data.getParcelableExtra("tweet"));
            // Update the RV with the tweet
            // Modify data source of tweets
            tweets.add(0, tweet);
            // Update the adapter
            adapter.notifyItemInserted(0);
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void onLogoutButton(View view) {
        TwitterApp.getRestClient(this).clearAccessToken(); // forget who's logged in
        Intent i = new Intent(this, LoginActivity.class);

        // Clears the back stack
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(i);
        //finish(); // navigate backwards to Login screen
    }

    private void populateHomeTimeline() {
        client.getHomeTimeline(new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG, "onSuccess!" + json.toString());
                JSONArray jsonArray = json.jsonArray;
                try {
                    tweets.addAll(Tweet.fromJsonArray(jsonArray));
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    Log.e(TAG, "Json exception", e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.i(TAG, "onFailure!" + response, throwable);
            }
        });
    }
}