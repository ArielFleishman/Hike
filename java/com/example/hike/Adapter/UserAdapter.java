package com.example.hike.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.hike.Fragment.ProfileFragment;
import com.example.hike.Model.User;
import com.example.hike.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>{

    private Context mContext;
    private List<User> mUsers;

    private FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUsers) {
        this.mContext = mContext;
        this.mUsers = mUsers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Inflate the user_item layout and create a new ViewHolder
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item,viewGroup,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        // Get the currently logged-in Firebase user
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get the user at the current position (i)
        final User user = mUsers.get(i);

        // Set the visibility and padding of the follow button
        viewHolder.btn_follow.setVisibility(View.VISIBLE);
        viewHolder.btn_follow.setPaddingRelative(0,0,0,10);

        // Set the username, full name, and profile image of the user
        viewHolder.username.setText(user.getUsername());
        viewHolder.full_name.setText(user.getFull_name());
        Glide.with(mContext).load(user.getImageurl()).into(viewHolder.image_profile);

        // Check if the user is being followed by the current user and update the follow button text accordingly
        isFollowing(user.getId(),viewHolder.btn_follow);

        // Hide the follow button if the user is viewing their own profile
        if(user.getId().equals(firebaseUser.getUid())){
            viewHolder.btn_follow.setVisibility(View.GONE);
        }

        // Handle click events on the user item
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Store the profile ID of the clicked user in SharedPreferences
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREPS",Context.MODE_PRIVATE).edit();
                editor.putString("profileid",user.getId());
                editor.apply();

                // Replace the current fragment with the ProfileFragment to display the clicked user's profile
                ((FragmentActivity)mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new ProfileFragment()).commit();
            }
        });

        // Handle click events on the follow button
        viewHolder.btn_follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Check the current state of the follow button and perform the appropriate action
                if(viewHolder.btn_follow.getText().toString().equals("follow")){
                    // Follow the user by adding the appropriate entries in the database to each user
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).setValue(true);
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).setValue(true);
                } else{
                    // Unfollow the user by removing the appropriate entries from the database to each user
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                            .child("following").child(user.getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId())
                            .child("followers").child(firebaseUser.getUid()).removeValue();
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        // Return the number of users in the list
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public TextView full_name;
        public CircleImageView image_profile;
        public Button btn_follow;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            // Initialize the views in the ViewHolder
            username = itemView.findViewById(R.id.username);
            full_name = itemView.findViewById(R.id.full_name);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);

        }
    }

    private void isFollowing(final String userid,final Button button){
        // Check if the current user is following the user with the given ID
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("following");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Update the follow button text to show if following or not
                if(dataSnapshot.child(userid).exists()){
                    button.setText("following");
                } else{
                    button.setText("follow");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method is called when the database operation is canceled or encounters an error
                // Handle any errors that occurred during the database operation
            }
        });
    }

}