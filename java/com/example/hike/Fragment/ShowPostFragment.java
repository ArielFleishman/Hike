package com.example.hike.Fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.hike.Adapter.PostAdapter;
import com.example.hike.Model.Post;
import com.example.hike.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ShowPostFragment extends Fragment {

    String postid;

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_show_post, container, false);

        // Retrieve the post ID from SharedPreferences
        SharedPreferences prefs = getContext().getSharedPreferences("PREPS"/*"PREFS"*/, Context.MODE_PRIVATE);
        postid = prefs.getString("postid", "none");

        // Initialize the RecyclerView and its adapter
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);

        postList = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), postList);
        recyclerView.setAdapter(postAdapter);

        // Read the post data from the database
        readPost();

        return view;
    }

    private void readPost(){
        // Read the post data from the database using the post ID
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postid);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Clear the current list of posts
                postList.clear();
                // Retrieve the post object from the dataSnapshot
                Post post = dataSnapshot.getValue(Post.class);
                // Add the post to the list
                postList.add(post);
                // Notify the adapter that the data has changed
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // This method is called when the database operation is canceled or encounters an error
                // Handle any errors that occurred during the database operation
            }
        });
    }
}