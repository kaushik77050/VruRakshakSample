package com.example.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Toast;

import com.example.sample.Model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class WorkerList extends AppCompatActivity {

    RecyclerView recyclerView;
    ArrayList<String> list = new ArrayList<>();
    Adapter adapter;
    DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        //DatabaseReference myRef1 = myRef.child("WorkerInfo");
        //Retrieve data from firebase
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                //int i = 0;
                //for loop used to append items list with phone number of workers
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Users users = snapshot.getValue(Users.class);
                    Users users = snapshot.child("WorkerInfo").getValue(Users.class); //recently added in place of above statement n comments
                    String phone1 = null;
                    if (users != null) {
                        phone1 = users.getPhone();
                    }
                    else{
                        Toast.makeText(WorkerList.this, "Null", Toast.LENGTH_SHORT).show();
                    }
                    list.add(phone1);
                    adapter = new Adapter(WorkerList.this,list);
                    recyclerView.setAdapter(adapter);
                    //Toast.makeText(MapsActivity.this, items[0] + " " + items[1], Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //list = new ArrayList<>();


    }
}