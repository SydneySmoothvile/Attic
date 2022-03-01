package com.example.attic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jetbrains.annotations.NotNull;

public class RegisterActivity extends AppCompatActivity {

    private Button registerBtn;
    private EditText emailField, usernameField, passwordField;
    private TextView loginTxtview;
    //Declare an instance of Firebase Authentication
    private FirebaseAuth mAuth;
    //Declare an instance of Firebase Database
    private FirebaseDatabase database;
    //Declare an instance of Firebase Database Reference;
    //A Database reference is a node in our database, e.g the node users to store user details
    private DatabaseReference userDetailsReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register2);

        //Inflate Toolbar
        Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        //Initiate the views
        loginTxtview= findViewById(R.id.loginTxtView);
        registerBtn = findViewById(R.id.registerBtn);
        emailField = findViewById(R.id.emailField);
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);

        //Initialize on Instance on firebase Authentication by calling the getInstance() method
        mAuth = FirebaseAuth.getInstance();
        //Initialize on Instance on firebase Database by calling the getInstance() method
        database = FirebaseDatabase.getInstance();


        userDetailsReference = database.getReference().child("Users");

        loginTxtview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent logIntent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(logIntent);
            }
        });

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create toast
                Toast.makeText(RegisterActivity.this, "Loading....", Toast.LENGTH_LONG).show();
                //getUsername entered
                final  String username = usernameField.getText().toString().trim();
                //get email entered
                final String email = emailField.getText().toString().trim();
                //get password entered
                final String password = passwordField.getText().toString().trim();
                //Validate to ensure that the user has entered the email an username
                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)){

                    mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            String user_id = mAuth.getCurrentUser().getUid();

                            DatabaseReference current_user_db = userDetailsReference.child(user_id);

                                                current_user_db.child("Username").setValue(username);
                                                current_user_db.child("Image").setValue("Default");

                                                Toast.makeText(RegisterActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                            Intent profIntent = new Intent(RegisterActivity.this, ProfileActivity.class);
                                   profIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                   startActivity(profIntent);
                        }
                    });
                } else{
                    Toast.makeText(RegisterActivity.this, "Complete all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


}