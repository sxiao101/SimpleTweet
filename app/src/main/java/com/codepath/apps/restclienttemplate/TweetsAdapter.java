package com.codepath.apps.restclienttemplate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;
import org.w3c.dom.Text;

import java.util.List;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class TweetsAdapter extends RecyclerView.Adapter<TweetsAdapter.ViewHolder> {

    public interface OnClickListener{
        void onReplyClicked(int position);
    }

    public interface TweetItemListener {
        public void onRetweetClicked(int position);
        public void onRetweetUnclicked(int position);
        public void onLikeClicked(int position);
        public void onLikeUnclicked(int position);
    }

    public static final String TAG = "TweetsAdapter";
    public final int REQUEST_CODE = 20;

    Context context;
    List<Tweet> tweets;
    OnClickListener clickListener;
    TweetItemListener listener;

    public TweetsAdapter(Context context, List<Tweet> tweets, OnClickListener clickListener, TweetItemListener listener) {
        this.context = context;
        this.tweets = tweets;
        this.clickListener = clickListener;
        this.listener = listener;
    }

    // For each row, inflate the layout
    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        Log.d(TAG, "OnCreate: ");
        View view = LayoutInflater.from(context).inflate(R.layout.item_tweet, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        Log.d(TAG, "Onbind: " + position);
        Tweet tweet = tweets.get(position);
        holder.bind(tweet);
    }

    @Override
    public int getItemCount() {
       return tweets.size();
    }

    // Clean all elements of the recycler
    public void clear() {
        tweets.clear();
        notifyDataSetChanged();
    }

    // Add a list of items -- change to type used
    public void addAll(List<Tweet> list) {
        tweets.addAll(list);
        notifyDataSetChanged();
    }

// Pass in the context and list of tweets

// For each row, inflate the layout

// Bind values based on the position of the element

    // Define a viewholder
    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivProfileImage;
        TextView tvBody;
        TextView tvScreenName;
        TextView tvName;
        TextView tvTime;
        ImageView ivMedia;
        ImageButton btRetweet;
        ImageButton btFav;
        ImageButton btReply;
        TextView tvRetweet;
        TextView tvFav;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfileImage = itemView.findViewById(R.id.ivProfileImage);
            tvBody = itemView.findViewById(R.id.tvBody);
            tvName = itemView.findViewById(R.id.tvName);
            tvScreenName = itemView.findViewById(R.id.tvScreenName);
            tvTime = itemView.findViewById(R.id.tvTime);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            btRetweet = itemView.findViewById(R.id.btRetweet);
            btFav = itemView.findViewById(R.id.btFav);
            btReply = itemView.findViewById(R.id.btReply);
            tvRetweet = itemView.findViewById(R.id.tvRetweet);
            tvFav = itemView.findViewById(R.id.tvFav);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Tweet tweet = tweets.get(position);
                    Intent intent = new Intent(context, TweetDetailsActivity.class);
                    intent.putExtra(Tweet.class.getSimpleName(), Parcels.wrap(tweet));
                    context.startActivity(intent);
                }
            });
        }

        public void bind(Tweet tweet) {
            tvBody.setText(tweet.body);
            tvName.setText(tweet.user.name);
            tvScreenName.setText(String.format("@%s", tweet.user.screenName));
            tvTime.setText(tweet.timeAgo);
            tvFav.setText("" + tweet.favCount);
            tvRetweet.setText("" + tweet.rtCount);

            if (tweet.rt) {
                btRetweet.setImageDrawable(context.getDrawable(R.drawable.ic_vector_retweet));
                btRetweet.setSelected(true);
            }
            if (tweet.fav) {
                btFav.setImageDrawable(context.getDrawable(R.drawable.ic_vector_heart));
                btFav.setSelected(true);
            }
            // for rounded corners
            int radius = 30;
            int margin = 10;
            Glide.with(context).load(tweet.user.profileImageUrl).fitCenter() // scale image to fill the entire ImageView
                    .transform(new RoundedCornersTransformation(radius, margin)).into(ivProfileImage);
            if (!tweet.mediaUrl.isEmpty()) {
                ivMedia.setVisibility(View.VISIBLE);
                Glide.with(context).load(tweet.mediaUrl).fitCenter().into(ivMedia);
            }
            else{
                ivMedia.setVisibility(View.GONE);
            }
            btReply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onReplyClicked(getAdapterPosition());
                }
            });
            btRetweet.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!btRetweet.isSelected()) {
                        btRetweet.setImageDrawable(context.getDrawable(R.drawable.ic_vector_retweet));
                        btRetweet.setSelected(true);
                        listener.onRetweetClicked(getAdapterPosition());
                        tweet.rtCount++;
                        tweet.rt = true;
                        tvRetweet.setText("" + tweet.rtCount);
                    } else {
                        btRetweet.setImageDrawable(context.getDrawable(R.drawable.ic_vector_retweet_stroke));
                        btRetweet.setSelected(false);
                        listener.onRetweetUnclicked(getAdapterPosition());
                        tweet.rtCount--;
                        tweet.rt = false;
                        tvRetweet.setText("" + tweet.rtCount);
                    }
                }
            });
            btFav.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!btFav.isSelected()) {
                        Log.d("Status", "liking");
                        btFav.setImageDrawable(context.getDrawable(R.drawable.ic_vector_heart));
                        btFav.setSelected(true);
                        listener.onLikeClicked(getAdapterPosition());
                        tweet.favCount++;
                        tweet.fav = true;
                        tvFav.setText("" + tweet.favCount);
                    } else {
                        Log.d("Status", "unliking");
                        btFav.setImageDrawable(context.getDrawable(R.drawable.ic_vector_heart_stroke));
                        btFav.setSelected(false);
                        listener.onLikeUnclicked(getAdapterPosition());
                        tweet.favCount--;
                        tweet.fav = false;
                        tvFav.setText("" + tweet.favCount);
                    }
                }
            });

        }
    }
}
