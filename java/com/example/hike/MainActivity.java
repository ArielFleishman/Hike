package com.example.hike;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.hike.Fragment.HomeFragment;
import com.example.hike.Fragment.ProfileFragment;
import com.example.hike.Fragment.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;
    Fragment selectedFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Find the BottomNavigationView in the activity_main layout and set a listener on it
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        // Check if the activity was started with an intent that has a "publisherid"
        Bundle intent = getIntent().getExtras();
        if (intent != null) {

            // If "publisherid" extra is found, get the value and save it to SharedPreferences
            String publisher = intent.getString("publisherid");
            SharedPreferences.Editor editor = getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit();
            editor.putString("profileid", publisher);
            editor.apply();

            // Replace the fragment container with a ProfileFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                    ,new ProfileFragment()).commit();
        } else {
            // If "publisherid" is not found, replace the fragment container with a HomeFragment
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                    ,new HomeFragment()).commit();
        }

    }

    // Create a listener for the BottomNavigationView
    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {


                // Requests that the method will be exicuted only on API level 19 (KITKAT) or higher
                // Because of the Objects.requireNonNull() method, which runs on API 19+.
                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                // Override the onNavigationItemSelected method to handle clicks on the navigation items
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    // Switch case to handle clicks on different navigation items
                    switch (menuItem.getItemId()) {
                        case R.id.nav_add:
                            // If "Add" item is clicked, start the PostActivity
                            selectedFragment = null;
                            startActivity(new Intent(MainActivity.this, PostActivity.class));
                            break;
                        case R.id.nav_search:
                            // If "Search" item is clicked, replace the fragment container with a SearchFragment
                            selectedFragment = new SearchFragment();
                            break;
                        case R.id.nav_home:
                            // If "Home" item is clicked, replace the fragment container with a HomeFragment
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_profile:
                            // If "Profile" item is clicked, save the current user ID to SharedPreferences
                            SharedPreferences.Editor editor = getSharedPreferences("PREPS", Context.MODE_PRIVATE).edit();
                            editor.putString("profileid", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
                            editor.apply();
                            // Replace the fragment container with a ProfileFragment
                            selectedFragment = new ProfileFragment();
                            break;
                    }

                    // If a fragment is selected, replace the fragment container with it
                    if (selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container
                                , selectedFragment).commit();
                    }

                    return true;
                }

            };


    private static final int PERMISSION_REQUEST_CODE = 1;
    AirplaneBroadcastReceiver airplaneBroadcastReceiver = new AirplaneBroadcastReceiver();

    @Override
    protected void onStart(){
        super.onStart();
        // Check and request the necessary permission
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, register the receiver
            registerReceiver(airplaneBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        } else {
            // Permission is not granted, request the permission from the user
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.READ_PHONE_STATE},
                    PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unregister the receiver to avoid leaks
        unregisterReceiver(airplaneBroadcastReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult( requestCode,  permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, register the receiver
                registerReceiver(airplaneBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
            } else {
                // Permission is denied, display a message or handle it accordingly
            }
        }
    }
}