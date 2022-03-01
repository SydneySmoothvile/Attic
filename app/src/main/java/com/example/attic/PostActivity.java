package com.example.attic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class PostActivity extends AppCompatActivity {

    private ImageButton imageBtn;
    private EditText textTitle,textDesc;
    private Button postBtn;

    private StorageReference mStorageRef;
    //Declare instance of the db reference where we will be saving the post details
    private DatabaseReference databaseRef;
    //Declare an Instance of firebase authentication
    private FirebaseAuth mAuth;
    //Declare an Instance of Db reference where we have the user details
    private DatabaseReference mDtabaseUsers;
    //Declare an instance of currently logged in user
    private FirebaseUser mCurrentUser;

    private static final int GALLERY_REQUEST_CODE = 2;
    //Declare an Instance of URI getting the image from our phone,initialize to null
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //initialize view objects
        postBtn =findViewById(R.id.postBtn);
        textDesc = findViewById(R.id.textDesc);
        textTitle = findViewById(R.id.textTitle);
        //Initialize the storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        //Initialize the database reference where you will be storing posts
        databaseRef = FirebaseDatabase.getInstance().getReference().child("Posts");
        //Initialize an Instance of Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        //Initialize instance of the firebase user
        mCurrentUser = mAuth.getCurrentUser();
        //Get currently logged in user
        mDtabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        //initialize the image button
        imageBtn = findViewById(R.id.imgBtn);
        //picking the image from gallery
        imageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
            }
        });
        //posting to Firebase
        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(PostActivity.this, "POSTING....", Toast.LENGTH_LONG).show();
                //get Title and desc from the edit texts
                final String PostTitle = textTitle.getText().toString().trim();
                final String PostDesc = textDesc.getText().toString().trim();
                //get the date and time of the post
                java.util.Calendar calendar = Calendar.getInstance();
                SimpleDateFormat currentDate = new SimpleDateFormat("HH:mm");
                final String saveCurrentDate = currentDate.format(calendar.getTime());

                java.util.Calendar calendar1 = Calendar.getInstance();
                SimpleDateFormat currentTime = new SimpleDateFormat("HH.mm");
                final String saveCurrentTime = currentTime.format(calendar1.getTime());
                //do a check for empty fields
                if(!TextUtils.isEmpty(PostDesc) && !TextUtils.isEmpty(PostTitle)){

                    StorageReference filepath = mStorageRef.child("post_images").child(uri.getLastPathSegment());
                    filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            if(taskSnapshot.getMetadata()!=null){
                                if(taskSnapshot.getMetadata().getReference() !=null){
                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();
                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String imageUrl = uri.toString();
                                            Toast.makeText(getApplicationContext(),"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                                            final DatabaseReference newPost = databaseRef.push();
                                            mDtabaseUsers.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    newPost.child("title").setValue(PostTitle);
                                                    newPost.child("desc").setValue(PostDesc);
                                                    newPost.child("postImage").setValue(imageUrl);
                                                    newPost.child("uid").setValue(mCurrentUser.getUid());
                                                    newPost.child("time").setValue(saveCurrentTime);
                                                    newPost.child("date").setValue(saveCurrentDate);

                                                    //get Prophile photo and display name of the person posting
                                                    newPost.child("profilePhoto").setValue(snapshot.child("profilePhoto").getValue());
                                                    newPost.child("displayName").setValue(snapshot.child("displayName").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                            if (task.isSuccessful()){
                                                                //launch the main Activity after posting
                                                                Intent intent = new Intent(PostActivity.this,MainActivity.class);
                                                                startActivity(intent);
                                                            }
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                }
                                            });
                                        }
                                    });
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK){
            //get the image selected by the user
            uri = data.getData();
            //set the image
            imageBtn.setImageURI(uri);
        }
    }
}