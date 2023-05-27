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
import android.provider.Settings;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {

    EditText email,password;
    Button login;
    TextView txt_register;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        email = findViewById((R.id.email));
        login = findViewById((R.id.login));
        txt_register = findViewById((R.id.txt_register));
        password = findViewById((R.id.password));

        // Initialize FirebaseAuth instance
        auth = FirebaseAuth.getInstance();

        // Set click listener for registration link
        txt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,Register.class));
            }
        });

        // Set click listener for login button
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Show progress dialog during login process
                final ProgressDialog pd = new ProgressDialog(Login.this);
                pd.setMessage("please wait...");
                pd.show();

                // Get entered email and password
                String str_email = email.getText().toString();
                String str_password = password.getText().toString();

                // Check if email or password fields are empty
                // if empty alert user
                if(TextUtils.isEmpty((str_email)) || TextUtils.isEmpty((str_password))){
                    Toast.makeText(Login.this,"All fields must be filled",Toast.LENGTH_SHORT).show();
                }
                else {
                    // Authenticate user with email and password
                    auth.signInWithEmailAndPassword(str_email,str_password)
                            .addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()){
                                        // Retrieve user data from Firebase Realtime Database
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users")
                                                .child(auth.getCurrentUser().getUid());
                                        // Add listener to retrieve data changes
                                        reference.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                // Dismiss progress dialog
                                                pd.dismiss();

                                                // Redirect to MainActivity
                                                Intent intent = new Intent(Login.this,MainActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                pd.dismiss();
                                            }
                                        });
                                    }
                                    else {
                                        // Dismiss progress dialog and display authentication failure message
                                        pd.dismiss();
                                        Toast.makeText(Login.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
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
        if (ContextCompat.checkSelfPermission(Login.this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, register the receiver
            registerReceiver(airplaneBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        } else {
            // Permission is not granted, request the permission from the user
            ActivityCompat.requestPermissions(Login.this,
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