package com.example.sample;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sample.Model.Users;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    int flag = 0;
    ListView listView = null;
    long count;
    //TODO:When Assign New Dustbin is clicked a list of workers should pop up
    Button showbtn, assignbtn;
    DatabaseReference myRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        showbtn = findViewById(R.id.showDustbins);
        assignbtn = findViewById(R.id.assigDustbin);

        showbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDustbins();
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                //Creating Marker
                MarkerOptions markerOptions = new MarkerOptions();
                //Set Marker Position
                markerOptions.position(latLng);
                //Set Latitude And Longitude On Marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);
                //Zoom the Marker
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 100));
                //Add marker On Map
                mMap.addMarker(markerOptions);
                Circle circle = mMap.addCircle(new CircleOptions()
                        .center(latLng)
                        .radius(10)
                        .strokeColor(Color.RED)
                        .fillColor(Color.argb(50, 0, 0, 255)));
                flag = 1;
                assignbtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        callFunction(latLng);
                    }
                });
            }
        });


    }
    void callFunction(final LatLng latLng)
    {
        final ArrayList<String> items = new ArrayList<>();
        //final String[] items = new String[(int) count];
        if (flag == 1) {
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            myRef = database.getReference("Users");
            //Retrieve data from firebase
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    int i = 0;
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Users users = snapshot.getValue(Users.class);
                        String name = users.getPhone();
                        //items[i++] = name;
                        items.add(name);
                        //Toast.makeText(MapsActivity.this, items[0] + " " + items[1], Toast.LENGTH_SHORT).show();
                    }
                    //ading the data to list
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(MapsActivity.this, R.layout.list_item, R.id.txtItem, items);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            ViewGroup viewGroup = (ViewGroup) view;
                            TextView txt = viewGroup.findViewById(R.id.txtItem);
                            final String ph = txt.getText().toString();
//                            Double lt = latLng.latitude;
//                            Double ln = latLng.longitude;
//
//                            int lt_int = lt.intValue();
//                            int ln_int = ln.intValue();

                            //final String child = String.format("%.2f", latLng.latitude) + "_" +String.format("%.2f", latLng.longitude);
                            //final String child = String.valueOf(lt_int) + "_" + String.valueOf(ln_int);
                            final String child = myRef.push().getKey();
                            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("latitude",latLng.latitude);
                                    hashMap.put("longitude",latLng.longitude);

                                    myRef.child(ph).child(child).updateChildren(hashMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    Toast.makeText(MapsActivity.this, "Location Added", Toast.LENGTH_SHORT).show();
                                                    myRef = database.getReference("Users").child(ph);
                                                    Toast.makeText(MapsActivity.this, "Token:"+myRef.getKey(), Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                            Toast.makeText(MapsActivity.this, child, Toast.LENGTH_SHORT).show();
                        }
                    });
                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    builder.setCancelable(true);
                    builder.setPositiveButton("OK", null);
                    builder.setView(listView);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                    Toast.makeText(MapsActivity.this, "Button Pressed2", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            listView = new ListView(MapsActivity.this);
            //String[] items={"Facebook","Google+","Twitter","Instagram"};
            //Toast.makeText(MapsActivity.this, items[0] + "-" + items[1], Toast.LENGTH_SHORT).show();
            //count = 0;
            Toast.makeText(MapsActivity.this, "Button Pressed", Toast.LENGTH_SHORT).show();
            flag = 0;
        } else {
            //Toast.makeText(MapsActivity.this, "Select from map first", Toast.LENGTH_SHORT).show();
            Snackbar.make(getWindow().getDecorView().getRootView(), "Select a location to assign", Snackbar.LENGTH_LONG).show();
        }
    }

    void showDustbins()
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Users users1 = snapshot.getValue(Users.class);
                    String phoneN = users1.getPhone();
                    //Toast.makeText(MapsActivity.this, "Child of phone:"+snapshot.getClass().getName(), Toast.LENGTH_LONG).show();
//                    if(snapshot.getChildren() != null){
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){

                            //LatLng ltln = new LatLng(latitude,longitude);
                            for(DataSnapshot snapshot2 : snapshot1.getChildren()){

                                    Locations location = snapshot2.getValue(Locations.class);
                                    Double lat = location.getLatitude();
                                    Double lon = location.getLongitude();

                                    LatLng ltln = new LatLng(lat,lon);
                                    //LatLng ltln = locations1.getLl();
                                    //Toast.makeText(MapsActivity.this, "Location"+ltln.longitude, Toast.LENGTH_SHORT).show();
                                    //Creating Marker
                                    MarkerOptions markerOptions = new MarkerOptions();
                                    //Set Marker Position
                                    markerOptions.position(ltln);
                                    //Set Latitude And Longitude On Marker
                                    markerOptions.title(ltln.latitude+ " : " + ltln.longitude);
                                    //Zoom the Marker
                                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,100));
                                    //Add marker On Map
                                    mMap.addMarker(markerOptions);
                                    Circle circle = mMap.addCircle(new CircleOptions()
                                            .center(ltln)
                                            .radius(10)
                                            .strokeColor(Color.RED)
                                            .fillColor(Color.argb(50,0,0,255)));

                                //Double lat = (Double) snapshot2.getValue();
                                //Double lon = (Double) snapshot2.getValue();
                                //Toast.makeText(MapsActivity.this, ""+ snapshot2.getRef(), Toast.LENGTH_SHORT).show();
                                //Toast.makeText(MapsActivity.this, "Location : "+longitude + "  " + latitude, Toast.LENGTH_SHORT).show();
//                                Toast.makeText(MapsActivity.this, "Child of phone : "+lon, Toast.LENGTH_LONG).show();


                            }

                        }
//                    }

                    Toast.makeText(MapsActivity.this, "Phone Numbers:"+phoneN, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
