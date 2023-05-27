package com.example.hike.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hike.Adapter.PhotoAdapter;
import com.example.hike.EditProfileActivity;
import com.example.hike.Model.Post;
import com.example.hike.Model.User;
import com.example.hike.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProfileFragment extends Fragment {

    ImageView image_profile;
    TextView posts, followers, following, username, full_name, bio;
    Button edit_profile;

    FirebaseUser firebaseUser;
    String profileid;

    RecyclerView recyclerView;
    PhotoAdapter photoAdapter;
    List<Post> postList;

    private List<String> mySaves;

    RecyclerView recyclerView_saves;
    PhotoAdapter photoAdapter_saves;
    List<Post> postList_saves;

    ImageButton my_photos, saved_photos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Get the current user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get the profileid from SharedPreferences
        SharedPreferences prefs = /*Objects.requireNonNull(getContext())*/requireContext().getSharedPreferences("PREPS", Context.MODE_PRIVATE);
        profileid = prefs.getString("profileid","none");

        // Initialize UI elements
        image_profile = view.findViewById(R.id.image_profile);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        username = view.findViewById(R.id.username);
        full_name = view.findViewById(R.id.full_name);
        bio = view.findViewById(R.id.bio);
        edit_profile = view.findViewById(R.id.edit_profile);
        my_photos = view.findViewById(R.id.my_photos);
        saved_photos = view.findViewById(R.id.saved_photos);

        // Initialize RecyclerView for user's posts
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        photoAdapter = new PhotoAdapter(getContext(), postList);
        recyclerView.setAdapter(photoAdapter);

        // Initialize RecyclerView for saved posts
        recyclerView_saves = view.findViewById(R.id.recycler_view_save);
        recyclerView_saves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager_saves = new GridLayoutManager(getContext(), 3);
        recyclerView_saves.setLayoutManager(linearLayoutManager_saves);
        postList_saves = new ArrayList<>();
        photoAdapter_saves = new PhotoAdapter(getContext(), postList_saves);
        recyclerView_saves.setAdapter(photoAdapter_saves);

        recyclerView.setVisibility(View.VISIBLE);
        recyclerView_saves.setVisibility(View.GONE);

        // Load user information, follower count, post count, and posts
        userInfo();
        getFollowers();
        getNumPosts();
        myphotos();
        mysaves();

        // If the profile is of the current user, show "Edit Profile" button
        // Otherwise, check if the current user is following the profile user
        if (profileid.equals(firebaseUser.getUid())) {
            edit_profile.setText("Edit Profile");
        } else {
            // Makes it that the saved posts of a different user wont be shown
            checkFollow();
            saved_photos.setVisibility(View.GONE);
        }

        // if user is profile of current user Edit Profile text will appear
        // when clicked it'll redirect to EditProfileActivity
        // if a this is the profile of a different user show follow or following
        // when clicked it'll update the Database of both users
        edit_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String btn = edit_profile.getText().toString();

                if(btn.equals("Edit Profile")){
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                } else if(btn.equals("follow")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                } else if (btn.equals("following")) {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(profileid).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileid)
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        // Set click listeners for buttons to switch between my photos and saved photos
        my_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView_saves.setVisibility(View.GONE);
            }
        });

        saved_photos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recyclerView.setVisibility(View.GONE);
                recyclerView_saves.setVisibility(View.VISIBLE);
            }
        });

        return view;
    }

    private void userInfo(){
        // Get reference to the user's information in the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(getContext() == null) return;
                // Get the user object from the dataSnapshot and update UI elements
                User user = snapshot.getValue(User.class);
                assert user != null;
                // loads users profile picture, username, full name and bio
                Glide.with(getContext()).load(user.getImageurl()).into(image_profile);
                username.setText(user.getUsername());
                full_name.setText(user.getFull_name());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        });
    }
    private void checkFollow(){
        // Check if the current user logged in, is following the profile user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(profileid).exists()){
                    edit_profile.setText("following");
                } else{
                    edit_profile.setText("follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        });
    }

    private void getFollowers(){
        // Shows how many followers and how many followed accounts current user has

        // Get a reference to the "followers" node under the "Follow" node in the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("followers");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // onDataChange is called when the data at the specified database location changes
                // Get the number of children (followers) under the "followers" node
                followers.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        });

        // Get a reference to the "following" node under the "Follow" node in the database
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(profileid).child("following");

        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // onDataChange is called when the data at the specified database location changes
                // Get the number of children (following) under the "following" node
                following.setText(""+snapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        });
    }

    private void getNumPosts(){
        // Get the number of posts for the profile user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int i=0;
                for(DataSnapshot x:snapshot.getChildren()){
                    // Get each post from the snapshot and check if its publisher matches the profileid
                    Post post = x.getValue(Post.class);
                    assert post != null;
                    if(post.getPublisher().equals(profileid)) i++;
                }
                // Set the count of posts to the posts TextView
                posts.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        });
    }
    private void myphotos() {
        // Load the posts of user
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot x : snapshot.getChildren()) {
                    // Get each post from the snapshot and check if its publisher matches the profileid
                    Post post = x.getValue(Post.class);
                    assert post != null;
                    if (post.getPublisher().equals(profileid)) postList.add(post);
                }
                // Reverse the order of the postList to display the most recent posts first
                Collections.reverse(postList);
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        });
    }

    private void mysaves() {
        // Load the saved posts of the current user
        mySaves = new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves")
                .child(firebaseUser.getUid());

        reference.addValueEventListener((new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot x : snapshot.getChildren()){
                    // Add each key (postid) from the snapshot to the mySaves list
                    mySaves.add(x.getKey()) ;
                }
                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        }));
    }
    private void readSaves() {
        // Read the saved posts from the database and update the UI
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList_saves.clear();
                for (DataSnapshot x : snapshot.getChildren()) {
                    // Get each post from the snapshot and check if its postid is in mySaves list
                    Post post = x.getValue(Post.class);

                    for (String id : mySaves) {
                        if (post.getPostid().equals(id)) {
                            postList_saves.add(post);
                        }
                    }
                }
                photoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle any errors that occurred during the database operation
            }
        });
    }
}