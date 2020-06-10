package com.example.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sample.Model.Users;
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

public class WorkerLogin extends AppCompatActivity {

    //private FirebaseAuth mAuth;
    DatabaseReference myRef;
    //private FirebaseAuth.AuthStateListener mAuthStateListener;
    TextView email,password,phone,textView;
    Button login;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.pass);
        login = findViewById(R.id.login);
        phone = findViewById(R.id.phoneNum);
        textView = findViewById(R.id.textView);

//        mAuth = FirebaseAuth.getInstance();
//
//        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
//
//            @Override
//            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
//                FirebaseUser mFirebaseUser = mAuth.getCurrentUser();
//                if (mFirebaseUser != null) {
//                    Toast.makeText(WorkerLogin.this, "You are logged in", Toast.LENGTH_SHORT);
//                    Intent i = new Intent(WorkerLogin.this, HomeActivity.class);
//                    startActivity(i);
//                } else {
//                    Toast.makeText(WorkerLogin.this, "Please Login", Toast.LENGTH_SHORT).show();
//                }
//            }
//        };

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailId = email.getText().toString();
                String pwd = password.getText().toString();
                String ph = phone.getText().toString();
                if (emailId.isEmpty()) {
                    email.setError("Please enter email id");
                    email.requestFocus();
                } else if (pwd.isEmpty()) {
                    password.setError("Please enter your password");
                    password.requestFocus();
                } else if (ph.isEmpty()){
                    phone.setError("Please enter registered phone number");
                    phone.requestFocus();
                } else{
                    progressDialog = new ProgressDialog(WorkerLogin.this);
                    progressDialog.setTitle("Creating Account");
                    progressDialog.setMessage("Please wait, while we are checking the credentials.");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    AllowAcesstoWorker(ph,pwd,emailId);
                }
            }
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WorkerLogin.this,MainActivity.class);
                startActivity(i);
            }
        });
    }
//    @Override
//    protected void onStart() {
//        super.onStart();
//        mAuth.addAuthStateListener(mAuthStateListener);
//    }

    void AllowAcesstoWorker(final String ph, final String pwd,final String emailId)
    {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(ph).exists()){
                    Toast.makeText(WorkerLogin.this, ph + "Exists", Toast.LENGTH_SHORT).show();
                    DataSnapshot snapshot = dataSnapshot.child(ph);
                    DataSnapshot snapshot1 = snapshot.child("WorkerInfo");
                            Users users = snapshot1.getValue(Users.class);
                            String phone = users.getPhone();
                            String pass = users.getPass();
                            String email1 = users.getEmail();
                            //Toast.makeText(WorkerLogin.this, "email" + email1, Toast.LENGTH_SHORT).show();
                            if(!phone.equals(ph)){
                                progressDialog.dismiss();
                                Toast.makeText(WorkerLogin.this, "Phone number is incorrect", Toast.LENGTH_SHORT).show();
                            }
                            else if(!pass.equals(pwd)){
                                progressDialog.dismiss();
                                //Toast.makeText(WorkerLogin.this, "Password is incorrect", Toast.LENGTH_SHORT).show();
                                password.setError("Password Incorrect");
                                password.requestFocus();
                            }
                            else if(!email1.equals(emailId)){
                                progressDialog.dismiss();
                                //Toast.makeText(WorkerLogin.this, "Email Id is incorrect", Toast.LENGTH_SHORT).show();
                                email.setError("Password Incorrect");
                                email.requestFocus();
                            }
                            else{
                                progressDialog.dismiss();
                                Intent intent = new Intent(WorkerLogin.this,WorkerActivity.class);
                                startActivity(intent);
                            }

                        //Toast.makeText(WorkerLogin.this, "email:"+email1, Toast.LENGTH_SHORT).show();
                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(WorkerLogin.this, "You have not been registered by admin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}