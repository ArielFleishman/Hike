package com.example.hike.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.hike.Adapter.PostAdapter;
import com.example.hike.Login;
import com.example.hike.MainActivity;
import com.example.hike.Model.Post;
import com.example.hike.R;
import com.example.hike.Register;
import com.example.hike.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;


public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postLists;

    private List<String> followingList;

    TextView logout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize RecyclerView and its properties
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        // Create lists to store posts and their corresponding following users
        postLists = new ArrayList<>();
        followingList = new ArrayList<>();//יכול להיות שיש למחוק את שורה זו

        // Create an instance of the PostAdapter and set it to the RecyclerView
        postAdapter = new PostAdapter(getContext(), postLists);
        recyclerView.setAdapter(postAdapter);

        // Call the method to check the following users
        checkFollowing();

        // Initialize the logout TextView and set an OnClickListener to handle the click event
        logout = view.findViewById(R.id.logout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Start the Login activity when the logout TextView is clicked
                startActivity(new Intent(getActivity(),Login.class));
            }
        });

        return view;
    }


    // Method that checks the users the current user is following
    private void checkFollowing() {
        followingList = new ArrayList<>();

        // Get the reference to the "following" node of the current user in the "Follow" node in the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Follow")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the followingList before populating it again
                followingList.clear();
                for (DataSnapshot x:snapshot.getChildren()) {
                    // Add the user IDs of the followed users to the followingList
                    followingList.add(x.getKey());
                }

                // Call the method to read the posts
                readPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur during the database operation
            }
        });
    }

    // Method to read the posts
    private void readPosts(){
        // Get the reference to the "Posts" node in the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Clear the postLists before populating it again
                postLists.clear();
                for (DataSnapshot x : snapshot.getChildren()) {
                    // Get each post from the snapshot

                    Post post = x.getValue(Post.class);
                    for (String id : followingList) {
                        assert post != null;
                        // Add the post to the postLists if its publisher ID is in the followingList
                        if(post.getPublisher().equals(id)) {
                            postLists.add(post);
                        }
                    }
                }
                // Notify the adapter that the data has changed
                // PostAdapter class is responsible for binding the postLists data to the RecyclerView and displaying the posts.
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occur during the database operation
            }
        });
    }
}