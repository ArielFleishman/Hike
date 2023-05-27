package com.example.hike;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;
import java.util.Objects;

import android.os.Bundle;

public class PostActivity extends AppCompatActivity {

    Uri imageUri;
    String myUrl="";
    StorageTask uploadTask;
    StorageReference storageReference;

    ImageView close,image_added;
    TextView post;
    EditText description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        // Initializing UI elements
        close = findViewById(R.id.close);
        image_added = findViewById(R.id.image_added);
        post = findViewById(R.id.post);
        description = findViewById(R.id.description);

        // Initializing storage reference from Firebase Storage, posts
        storageReference = FirebaseStorage.getInstance().getReference("posts");

        // Setting click listener for close button to redirect back to MainActivity
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PostActivity.this,MainActivity.class));
                finish();
            }
        });

        // Setting click listener for posting images button
        post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
        // Starting crop image activity with a 1:1 aspect ratio, because then it looks good in gui
        CropImage.activity()
                .setAspectRatio(1,1)
                .start(PostActivity.this);

    }

    // Helper method to get file extension from a Uri
    private String getFileExtention(Uri uri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    // Method that uploads the image to Firebase Storage and adds post data to Firebase Realtime Database
    private void uploadImage(){
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Posting");
        progressDialog.show();

        if(imageUri!=null){
            // Creating a new storage reference for the image file
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()+
                    "."+getFileExtention(imageUri));

            // Upload the image file to Firebase Storage
            uploadTask = fileReference.putFile(imageUri);
            // If upload is successful, get the image URL and add post data to Firebase Realtime Database
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw task.getException();
                    }
                    // Continue with the task to get the download URL
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {

                @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if(task.isSuccessful()){
                        // this if statement adds post to Real Time Database

                        // get the download URL of the uploaded image
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        myUrl = downloadUri.toString();

                        // save post data to the Firebase database
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

                        // gets a unique key for the new post
                        String postid = reference.push().getKey();

                        // create a HashMap to hold the post data
                        HashMap<String,Object> hashMap = new HashMap<>();
                        hashMap.put("postid",postid);
                        hashMap.put("postimage",myUrl);
                        hashMap.put("description",description.getText().toString());
                        hashMap.put("publisher", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

                        // add the new post to the database
                        assert postid != null;
                        reference.child(postid).setValue(hashMap);

                        progressDialog.dismiss();

                        // redirect the user to the MainActivity after the post is uploaded
                        startActivity(new Intent(PostActivity.this,MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(PostActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // if the upload fails, show a toast message with the error message
                    Toast.makeText(PostActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // if no image is selected, show a toast message indicating so
            Toast.makeText(this, "No Image Selected!", Toast.LENGTH_SHORT).show();
        }
    }

    // method that handles the result of image selection/cropping activity
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // The requestCode parameter is used to identify which activity returned a result.
        // For example, if multiple activities are started for different purposes, the requestCode can be used
        // to determine which activity returned a result.
        // The resultCode parameter indicates the result of the activity that was started.
        // RESULT_OK is returned if the activity was successful.


        // check if the request code and result code match with the ones for the image selection/cropping activity
        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK){
            // get the cropped image URI from the result
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            imageUri = result.getUri();
            // set the image view to display the selected/cropped image
            image_added.setImageURI(imageUri);
        } else{
            // if something goes wrong during the image selection/cropping process, show a toast message and return to the main activity
            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(PostActivity.this,MainActivity.class));
            finish();
        }

    }

    private static final int PERMISSION_REQUEST_CODE = 1;
    AirplaneBroadcastReceiver airplaneBroadcastReceiver = new AirplaneBroadcastReceiver();

    @Override
    protected void onStart(){
        super.onStart();
        // Check and request the necessary permission
        if (ContextCompat.checkSelfPermission(PostActivity.this,
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // Permission is granted, register the receiver
            registerReceiver(airplaneBroadcastReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));
        } else {
            // Permission is not granted, request the permission from the user
            ActivityCompat.requestPermissions(PostActivity.this,
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
