package com.codepath.apps.restclienttemplate.models;

import android.icu.text.SimpleDateFormat;
import android.os.Build;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Parcel
public class Tweet {

    public String body;
    public String createdAt;
    public User user;
    public Entity entity;
    public String timeAgo;
    public String mediaUrl;
    public String id;
    public int rtCount;
    public int favCount;
    public boolean rt;
    public boolean fav;

    // empty constructor needed by the Parveler library
    public Tweet(){}

    public static Tweet fromJson(JSONObject jsonObject) throws JSONException {
        Tweet tweet = new Tweet();
        tweet.body = jsonObject.getString("text");
        tweet.createdAt = jsonObject.getString("created_at");
        tweet.user = User.fromJson(jsonObject.getJSONObject("user"));
        tweet.timeAgo = getRelativeTimeAgo(tweet.createdAt);
        tweet.id = jsonObject.getString("id_str");
        tweet.rtCount = jsonObject.getInt("retweet_count");
        tweet.favCount = jsonObject.getInt("favorite_count");
        tweet.rt = jsonObject.getBoolean("retweeted");
        tweet.fav = jsonObject.getBoolean("favorited");

        if (!jsonObject.isNull("extended_entities")) {
            //tweet.entity = Entity.fromJson(jsonObject.getJSONObject("entities"));
            JSONObject entities = jsonObject.getJSONObject("extended_entities");
            JSONArray array = entities.getJSONArray("media");
            JSONObject media = array.getJSONObject(0);
            tweet.mediaUrl = media.getString("media_url_https");
        } else {
            //tweet.entity = null;
            tweet.mediaUrl = "";
        }
        return tweet;
    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    // getRelativeTimeAgo("Mon Apr 01 21:16:23 +0000 2014");
    public static String getRelativeTimeAgo(String rawJsonDate) {
        String twitterFormat = "EEE MMM dd HH:mm:ss ZZZZZ yyyy";
        SimpleDateFormat sf = new SimpleDateFormat(twitterFormat, Locale.ENGLISH);
        sf.setLenient(true);

        try {
            long time = sf.parse(rawJsonDate).getTime();
            long now = System.currentTimeMillis();

            final long diff = now - time;
            if (diff < MINUTE_MILLIS) {
                return "just now";
            } else if (diff < 2 * MINUTE_MILLIS) {
                return "a minute ago";
            } else if (diff < 50 * MINUTE_MILLIS) {
                return diff / MINUTE_MILLIS + " m";
            } else if (diff < 90 * MINUTE_MILLIS) {
                return "an hour ago";
            } else if (diff < 24 * HOUR_MILLIS) {
                return diff / HOUR_MILLIS + " h";
            } else if (diff < 48 * HOUR_MILLIS) {
                return "yesterday";
            } else {
                return diff / DAY_MILLIS + " d";
            }
        } catch (ParseException e) {
            Log.i("Tweet Class", "getRelativeTimeAgo failed");
            e.printStackTrace();
        }

        return "";
    }

    public static List<Tweet> fromJsonArray(JSONArray jsonArray) throws JSONException {
        List<Tweet> tweets = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            tweets.add(fromJson(jsonArray.getJSONObject(i)));
        }
        return tweets;
    }

}
