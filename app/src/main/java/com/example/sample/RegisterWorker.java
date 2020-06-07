package com.example.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.regex.Pattern;

public class RegisterWorker extends AppCompatActivity {

    EditText fname,lname,phoneNo,emailId,pass,cnfrmPass;
    Button registerBtn;
    DatabaseReference myRef;
    String Name,firstname,lastname,phone,email,password,confirmPassword;
    ProgressDialog progressDialog;
    public final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9+._%-+]{1,256}" +
                    "@" +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,64}" +
                    "(" +
                    "." +
                    "[a-zA-Z0-9][a-zA-Z0-9-]{0,25}" +
                    ")+"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_worker);

        fname = findViewById(R.id.firstName);
        lname = findViewById(R.id.lastName);
        phoneNo = findViewById(R.id.phoneNumber);
        pass = findViewById(R.id.password);
        emailId = findViewById(R.id.email);
        cnfrmPass = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.register);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    void createAccount()
    {
        firstname = fname.getText().toString();
        lastname = lname.getText().toString();
        Name = firstname + " " + lastname;
        phone = phoneNo.getText().toString();
        email = emailId.getText().toString();
        password = pass.getText().toString();
        confirmPassword = cnfrmPass.getText().toString();

        //double ph = Double.parseDouble(phone);

        if(TextUtils.isEmpty(firstname))
            Toast.makeText(this, "Enter the first name", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(lastname))
            Toast.makeText(this, "Enter the last name", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(phone))
            Toast.makeText(this, "Enter the phone number", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(email))
            Toast.makeText(this, "Enter the email Id", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(password))
            Toast.makeText(this, "Enter the password", Toast.LENGTH_SHORT).show();
        else if(TextUtils.isEmpty(confirmPassword))
            Toast.makeText(this, "Enter the confirm password", Toast.LENGTH_SHORT).show();
        else if(!password.equals(confirmPassword))
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
//        else if(!EMAIL_ADDRESS_PATTERN.matcher(email).matches())
//            Toast.makeText(this, "Invalid Email Address", Toast.LENGTH_SHORT).show();
        else{
            progressDialog = new ProgressDialog(RegisterWorker.this);
            progressDialog.setTitle("Creating Account");
            progressDialog.setMessage("Please wait, while we are checking the credentials.");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            validatePhoneNumber(Name,email,phone,password);
        }
    }
    void validatePhoneNumber(final String Name, final String email, final String  phoneNumber, final String password)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference();

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.child("Users").child(phoneNumber).exists()){
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("phone",phoneNumber);
                    hashMap.put("name",Name);
                    hashMap.put("email",email);
                    hashMap.put("pass",password);

                    myRef.child("Users").child(phoneNumber).updateChildren(hashMap)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterWorker.this, "Worker Account has been created", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                    }
                                    else{
                                        progressDialog.dismiss();
                                        Toast.makeText(RegisterWorker.this, "Network Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                else{
                    Toast.makeText(RegisterWorker.this, "The phone number : "+phoneNumber+" already exists.", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}