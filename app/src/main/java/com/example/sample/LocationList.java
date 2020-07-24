package com.example.sample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class LocationList extends AppCompatActivity {

    ListView listView;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef;

    StorageReference islandRef;

    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);

        storageReference = FirebaseStorage.getInstance().getReference();

        listView = findViewById(R.id.listView);
        incomingIntent();
    }

    void incomingIntent()
    {
        if(getIntent().hasExtra("phone")){
            Bundle bundle = getIntent().getExtras();
            String id = bundle.getString("phone");
            Toast.makeText(this, "phone:" + id, Toast.LENGTH_SHORT).show();
            final ArrayList<String> items = new ArrayList<>();
            myRef = database.getReference("Users");
            DatabaseReference myRef1 = myRef.child(id);
            final DatabaseReference myRef2 = myRef1.child("Locations");

            myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {


                        //Toast.makeText(LocationList.this, snapshot.getKey(), Toast.LENGTH_SHORT).show();
                        items.add(snapshot.getKey());
                    }

                    ArrayAdapter<String> adapter1 = new ArrayAdapter <String>(LocationList.this, R.layout.list_item, R.id.txtItem, items);
                    listView.setAdapter(adapter1);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ViewGroup viewGroup = (ViewGroup) view;
                            TextView txt = viewGroup.findViewById(R.id.txtItem);
                            final String lid = txt.getText().toString();
                            //Toast.makeText(LocationList.this, "Loaction id: " + lid, Toast.LENGTH_SHORT).show();
                            //DatabaseReference myRef3 = myRef2.child("TimeAndDate");

                            myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                        String val = (String)snapshot.getKey();
                                        if(val.equals(lid)){
                                            //Toast.makeText(LocationList.this, snapshot.getKey(), Toast.LENGTH_SHORT).show();
                                            if(snapshot.child("TimeAndDate").exists()) {
                                                TimeAndDate timeAndDate = snapshot.child("TimeAndDate").getValue(TimeAndDate.class);
                                                String time = timeAndDate.getTime();
                                                final String date = timeAndDate.getDate();

                                                AlertDialog.Builder alert = new AlertDialog.Builder(LocationList.this);
                                                alert.setTitle("Time And date of last login");
                                                alert.setMessage("Date : "+ date + "\n\n" +"Time : "+ time);

                                                final TextView input = new TextView (LocationList.this);
                                                alert.setView(input);

                                                alert.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        downloadFile(lid);
                                                    }
                                                });

                                                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int whichButton) {
                                                        // Canceled.
                                                    }
                                                });
                                                alert.show();

                                                Toast.makeText(LocationList.this, "Time : " + time + " and Date : " + date, Toast.LENGTH_SHORT).show();
//                                            for(DataSnapshot snapshot2 : snapshot.getChildren()){
//                                                Toast.makeText(LocationList.this, snapshot2.getKey(), Toast.LENGTH_SHORT).show();
//                                                if(snapshot2.equals("TimeAndDate")){
//                                                    Toast.makeText(LocationList.this, "Bus ho gya bhai", Toast.LENGTH_SHORT).show();
//                                                }
//                                                for(DataSnapshot snapshot3 : snapshot2.getChildren()){
//                                                    //Toast.makeText(LocationList.this, snapshot3.getKey(), Toast.LENGTH_SHORT).show();
//                                                    TimeAndDate timeAndDate = snapshot2.getValue(TimeAndDate.class);
//                                                    String time = timeAndDate.getTime();
//                                                    String date = timeAndDate.getDate();
//
//                                                    Toast.makeText(LocationList.this, "Time : " + time + " and Date : " + date, Toast.LENGTH_SHORT).show();
//                                                }
//                                            }
//                                        Toast.makeText(LocationList.this, dataSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                                            }
                                            else{
                                                Toast.makeText(LocationList.this, "The worker hasn't started working yet", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    });

//                    AlertDialog.Builder builder = new AlertDialog.Builder(LocationList.this);
//                    builder.setCancelable(true);
//                    builder.setPositiveButton("OK", null);
//                    //builder.setView(listView);
//                    if(listView.getParent()!=null)
//                        ((ViewGroup)listView.getParent()).removeView(listView); // <- fix
//                    builder.setView(listView);
//                    AlertDialog alertDialog = builder.create();
//                    alertDialog.show();


                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    private void downloadFile(String id){


        islandRef = storageReference.child(id+".jpg");

        islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //Toast.makeText(MainActivity.this, "Successfully downloaded", Toast.LENGTH_SHORT).show();
                String url = uri.toString();
                downloadFile(LocationList.this,"image",".jpg",DIRECTORY_DOWNLOADS,url);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LocationList.this, "Failure!!", Toast.LENGTH_SHORT).show();
            }
        });

    }
    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {


        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        downloadmanager.enqueue(request);
    }
}