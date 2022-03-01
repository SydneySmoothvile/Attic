package com.example.attic;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageRegistrar;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

public class ProfileActivity extends AppCompatActivity {

    private EditText profUserName;
    private ImageButton imageButton;
    private Button doneBtn;

    //Declare an instance of firebase authentication
    private FirebaseAuth mAuth;
    //Declare an instance of database reference where we will be saving profile photo and custom display name
    private DatabaseReference mDatabaseUser;
    private StorageReference mStorageRef;

    private Uri profileImageUri = null;
     //Declare an initialize a private final static int that will serve as our request code
    private final static int GALLERY_REQ=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        //inflate toolbar
        Toolbar toolbar= findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        //Initialize the instances of the views
        profUserName = findViewById(R.id.profUserName);
        imageButton = findViewById(R.id.imagebutton);
        doneBtn = findViewById(R.id.doneBtn);
        //Initialize the instances of Firebase authentications
        mAuth = FirebaseAuth.getInstance();
        //We want to set profile for specific,hence get the user id of the current user an specific user reference using the user ID.
        final String userID = mAuth.getCurrentUser().getUid();
        //initialize database reference where you have your registered users and get the specidfic user reference using the user ID.
        mDatabaseUser= FirebaseDatabase.getInstance().getReference().child("Users").child(userID);
        //initialize firebase storage reference where you will store the profile photo images
        mStorageRef = FirebaseStorage.getInstance().getReference().child("profile_images");
        //set onClickListener on image buttton to allow user to pick their own profile photo from their gallery
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //create an implicit intent for getting the images
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                //set type to omages only
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQ);
            }
        });

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get the custom display name entered by the user
                final String name = profUserName.getText().toString().trim();
                //validate to ensure that the name and profile image are not noll
                if (!TextUtils.isEmpty(name) && profileImageUri != null){

                    //create Storage reference node, inside prof_image storage refence where you will save the profile image

                    StorageReference profileImagePath = mStorageRef.child("profile_images").child(profileImageUri.getLastPathSegment());
                    profileImagePath.putFile(profileImageUri ).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            if (taskSnapshot.getMetadata() !=null){
                                if (taskSnapshot.getMetadata().getReference() !=null){

                                    Task<Uri> result = taskSnapshot.getStorage().getDownloadUrl();

                                    result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            final String profileImage = uri.toString();
                                            mDatabaseUser.push();
                                            mDatabaseUser.addValueEventListener(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                                    mDatabaseUser.child("displayName").setValue(name);
                                                    mDatabaseUser.child("profilePhoto").setValue(profileImage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull @NotNull Task<Void> task) {

                                                            Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                                                            //Launch the login Activity
                                                            Intent login = new Intent(ProfileActivity.this, LoginActivity.class);
                                                            startActivity(login);

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

            if (requestCode ==GALLERY_REQ && resultCode == RESULT_OK){
                //get the image selected by the user
                profileImageUri = data.getData();
                imageButton.setImageURI(profileImageUri);
            }
    }
}