package com.example.sample;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    int flag = 0;
    ListView listView = null;
    long count;
    //TODO:When Assign New Dustbin is clicked a list of workers should pop up
    Button showbtn, assignbtn;
    DatabaseReference myRef;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;


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

        if (ContextCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            enableMyLocation();
        }
        // mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
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
            //DatabaseReference myRef1 = myRef.child("WorkerInfo");
            //Retrieve data from firebase
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    int i = 0;
                    //for loop used to append items list with phone number of workers
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
//                        Users users = snapshot.getValue(Users.class);
                        Users users = snapshot.child("WorkerInfo").getValue(Users.class); //recently added in place of above statement n comments
                        String phone1 = users.getPhone();
                        items.add(phone1);
                        //Toast.makeText(MapsActivity.this, items[0] + " " + items[1], Toast.LENGTH_SHORT).show();
                    }

                    //ading the phone number to list that will be displayed.
                    ArrayAdapter<String> adapter = new ArrayAdapter <String>(MapsActivity.this, R.layout.list_item, R.id.txtItem, items);
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
//                                    hashMap.put("time",0.0);
//                                    hashMap.put("date",0.0);

                                    myRef.child(ph).child("Locations").child(child).updateChildren(hashMap)
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
                    Users users1 = snapshot.child("WorkerInfo").getValue(Users.class);
                    String phoneN = users1.getPhone();
                    //Toast.makeText(MapsActivity.this, "Child of phone:"+snapshot.getClass().getName(), Toast.LENGTH_LONG).show();
//                    if(snapshot.getChildren() != null){
                        for(DataSnapshot snapshot1 : snapshot.getChildren()){
                            if(snapshot1.exists()){
                                Toast.makeText(MapsActivity.this, "snapshot1 exists", Toast.LENGTH_SHORT).show();
                                if(snapshot1.getKey().equals("Locations")){
                                    for(DataSnapshot snapshot2 : snapshot1.getChildren()){
                                        for(DataSnapshot snapshot3 : snapshot2.getChildren()){
                                            Locations locations =  snapshot2.getValue(Locations.class);
                                            Double lat = locations.getLatitude();
                                            Double lon = locations.getLongitude();
                                            Toast.makeText(MapsActivity.this, "Location : "+lat + "  " + lon, Toast.LENGTH_SHORT).show();

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
                                        }
                                    }
                                }
                            //LatLng ltln = new LatLng(latitude,longitude);
//                            for(DataSnapshot snapshot2 : snapshot1.getChildren()){
//                                if(snapshot2.exists()){
//                                    Toast.makeText(MapsActivity.this, "snapshot2 exists", Toast.LENGTH_SHORT).show();
//                                    if(snapshot2.getKey().equals("Locations")){
//                                        for(DataSnapshot snapshot3 : snapshot2.getChildren()){
//
//                                            Locations locations =  snapshot2.getValue(Locations.class);
//                                            Double lat = locations.getLatitude();
//                                            Double lon = locations.getLongitude();
//                                            Toast.makeText(MapsActivity.this, "Location : "+lat + "  " + lon, Toast.LENGTH_SHORT).show();
//                                        }
//                                    }
//                                }

//                                    Locations location = snapshot2.getValue(Locations.class);
//                                    Double lat = location.getLatitude();
//                                    Double lon = location.getLongitude();
//


//                                Locations locations =  snapshot1.getValue(Locations.class);
//                                Double lat = locations.getLatitude();
//                                Double lon = locations.getLongitude();
//                                //Double lon = (Double) snapshot2.getValue();
//                                //Toast.makeText(MapsActivity.this, ""+ snapshot2.getRef(), Toast.LENGTH_SHORT).show();
//                                //Toast.makeText(MapsActivity.this, "Location : "+lat + "  " + lon, Toast.LENGTH_SHORT).show();
////                                Toast.makeText(MapsActivity.this, "Child of phone : "+lon, Toast.LENGTH_LONG).show();



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

    private void enableMyLocation()
    {
        ActivityCompat.requestPermissions(MapsActivity.this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "My Location button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if(requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();

                mMap.setMyLocationEnabled(true);

            }
            else
            {
                Toast.makeText(this, "Permission Not Granted", Toast.LENGTH_SHORT).show();
                mPermissionDenied = true;
            }
        }

    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {

        new AlertDialog.Builder(this)
                .setTitle("Permission Not Granted")
                .setNegativeButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
