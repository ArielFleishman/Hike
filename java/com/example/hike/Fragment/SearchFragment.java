package com.example.hike.Fragment;

import android.app.DownloadManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.hike.Adapter.UserAdapter;
import com.example.hike.Model.User;
import com.example.hike.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> lUsers;

    EditText search_bar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        // Initialize views and variables
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        search_bar = view.findViewById(R.id.search_bar);

        lUsers = new ArrayList<>();

        userAdapter = new UserAdapter(getContext(),lUsers);
        recyclerView.setAdapter(userAdapter);

        // Read all users from the database
        readUsers();

        // Listen for changes in the search bar's text
        search_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called to notify that the text is about to change.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // This method is called when the text is changed.
                // Call the searchUsers method to filter the users based on the entered text.
                searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // This method is called after the text has been changed.
            }
        });

        return view;
    }

    private void searchUsers(String s){
        // Search for users in the database based on the entered text
        /*
        https://stackoverflow.com/questions/38618953/how-to-do-a-simple-search-in-string-in-firebase-database

        The character \uf8ff used in the query is a very high code point in the Unicode range (it is a Private Usage Area [PUA] code).
        Because it is after most regular characters in Unicode, the query matches all values that start with queryText.

        In this way, searching by "Fre" I could get the records having "Fred, Freddy, Frey" as value in _searchLastName property from the database.
        */
        Query query = FirebaseDatabase.getInstance().getReference("Users").orderByChild("username")
                .startAt(s)
                .endAt(s+"\uf8ff");

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Clear the current list of users
                lUsers.clear();
                // Iterate through the search results and add users to the list
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    lUsers.add(user);
                }

                // Notify the adapter that the data has changed
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // This method is called when the database operation is canceled or encounters an error
                // Handle any errors that occurred during the database operation
            }
        });
    }

    private void readUsers(){
        // Read all users from the database
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Check if the search bar is empty
                if(search_bar.getText().toString().equals("")){
                    // Clear the current list of users
                    lUsers.clear();
                    // Iterate through the user data and add users to the list
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        User user = snapshot.getValue(User.class);
                        lUsers.add(user);
                    }

                    // Notify the adapter that the data has changed
                    userAdapter.notifyDataSetChanged();
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