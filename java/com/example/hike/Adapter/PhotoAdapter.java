package com.example.hike.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.hike.Fragment.ShowPostFragment;
import com.example.hike.Model.Post;
import com.example.hike.R;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private Context context;
    private List<Post> mPosts;

    public PhotoAdapter(Context context, List<Post> mPosts) {
        this.context = context;
        this.mPosts = mPosts;
    }

    // Create a ViewHolder for the item view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item view
        View view = LayoutInflater.from(context).inflate(R.layout.photos_item, parent, false);
        return new PhotoAdapter.ViewHolder(view);
    }

    // Bind data to the ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int i) {
        // Get the current post from the list
        Post post = mPosts.get(i);

        // Load the image into the ImageView using Glide library
        Glide.with(context).load(post.getPostimage()).into(holder.post_image);

        // Set click listener for the image
        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Store the post ID in SharedPreferences for later use
                SharedPreferences.Editor editor = context.getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                // Replace the current fragment with the ShowPostFragment
                ((FragmentActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ShowPostFragment()).commit();
            }
        });

    }

    // Return the number of items in the list
    @Override
    public int getItemCount() {
        return mPosts.size();
    }

    // ViewHolder class to hold the item view's references
    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView post_image;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize the ImageView from the item view
            post_image = itemView.findViewById(R.id.post_image);
        }
    }

}