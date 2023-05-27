package com.example.hike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class Register extends AppCompatActivity {

    // Declare all your views and variables at the top
    EditText username,full_name,email,password;
    Button register;
    TextView txtLogin;

    // For Firebase Authentication
    FirebaseAuth auth;

    // For Firebase Database
    DatabaseReference reference;

    // For Progress Dialog
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize your views
        username = findViewById(R.id.username);
        full_name = findViewById(R.id.full_name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        txtLogin = findViewById(R.id.txt_login);

        // Initialize Firebase Authentication instance
        auth = FirebaseAuth.getInstance();

        // Set OnClickListener for the "Login" TextView
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Register.this,Login.class));
            }
        });

        // Set OnClickListener for the "Register" Button
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(Register.this);
                progressDialog.setMessage("Please wait...");
                progressDialog.show();

                // Get all values entered by the user in the EditText fields
                String str_username = username.getText().toString();
                String str_full_name = full_name.getText().toString();
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                // Checks if any of the fields are empty
                if(TextUtils.isEmpty(str_username) || TextUtils.isEmpty(str_email) || TextUtils.isEmpty(str_full_name)
                        || TextUtils.isEmpty(str_password))
                {
                    // Show a toast message if any of the fields are empty
                    Toast.makeText(Register.this,"All fields must be filled",Toast.LENGTH_SHORT).show();
                }
                // Check if the password is less than 6 characters long
                else if(str_password.length()<6)
                {
                    // Show a toast message if the password is too short
                    Toast.makeText(Register.this,"Password must have at least 6 characters",Toast.LENGTH_SHORT).show();
                } else {
                    // Calls the register method if all input is valid
                    register(str_username,str_full_name,str_email,str_password);
                }

            }
        });
    }

    // Define the register method
    private void register(final String username, final String full_name, String email, String password){
        // Create a new user in Firebase Authentication with the provided email and password
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(Register.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            // If the user is successfully created, get their unique user ID (Uid)
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userid = firebaseUser.getUid();

                            // Create a new reference to the database location where user's data will be stored
                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userid);

                            // Create a HashMap to store the user's information (Uid, username, full name, bio, image URL)
                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id",userid);
                            hashMap.put("username",username.toLowerCase()); // store username in lowercase to allow for case-insensitive searches
                            hashMap.put("full_name",full_name);
                            hashMap.put("bio","");
                            hashMap.put("imageurl","https://firebasestorage.googleapis.com/v0/b/hike-3397d.appspot.com/o/blank-profile-picture.jpg?alt=media&token=7185e737-7355-49e1-b1cd-14f852713e8e");

                            // Set the value of the user's reference in the database to the HashMap of their information
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    // If setting the user's information in the database is successful, dismiss the progress dialog
                                    if(task.isSuccessful()){
                                        progressDialog.dismiss();


                                        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
                                            NotificationChannel channel = new NotificationChannel("Hello!!!", "Hello!!!", NotificationManager.IMPORTANCE_DEFAULT);
                                            NotificationManager manager = getSystemService(NotificationManager.class);
                                            manager.createNotificationChannel(channel);
                                        }

                                        // build notification
                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(Register.this, "Hello!!!");
                                        builder.setContentTitle("Hello!!!");
                                        builder.setContentText("You are the newest hiker!!!");
                                        builder.setSmallIcon(R.drawable.logo_nobgpng);
                                        builder.setAutoCancel(true);

                                        // Notify user
                                        NotificationManagerCompat managerCompat =NotificationManagerCompat.from(Register.this);
                                        managerCompat.notify(1, builder.build());

                                        // Create a new intent to start the MainActivity and clear any previous activities on the stack
                                        Intent intent = new Intent(Register.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                    }
                                }
                            });

                        } else {
                            // If there was an error creating the user, dismiss the progress dialog and display an error message to the user
                            progressDialog.dismiss();
                            Toast.makeText(Register.this,"You can't register with this email or password",Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }



    private static final int PERMISSION_REQUEST_CODE = 1;
    AirplaneBroadcastReceiver airplaneBroadcastReceiver = new AirplaneBroadcastReceiver();

    @Override
    protected void onStart(){
        super.onStart();
        // Check and request the necessary permission
        if (ContextCompat.checkSelfPermission(Register.this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, register the receiver
            registerReceiver(airplaneBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        } else {
            // Permission is not granted, request the permission from the user
            ActivityCompat.requestPermissions(Register.this,
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
