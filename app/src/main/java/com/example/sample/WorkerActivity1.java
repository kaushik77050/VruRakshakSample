package com.example.sample;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
//import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;

public class WorkerActivity1 extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private GoogleMap mMap;
    Button lgout,showDustbins;
    FloatingActionButton fingerprint;
    DatabaseReference myRef;
    Circle circle;
    int count = 0;

    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;


    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private boolean mPermissionDenied = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        lgout = findViewById(R.id.logout);
        showDustbins = findViewById(R.id.showDustbins);
        fingerprint = findViewById(R.id.floatingActionButton);

        lgout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WorkerActivity1.this,WorkerLogin.class);
                startActivity(intent);
            }
        });

        showDustbins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                //showDustbinsAssignedtome();
                CheckifWorkerisInsideCircle();
            }
        });


        fingerprint.setVisibility(View.GONE);
        fingerprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executor = ContextCompat.getMainExecutor(WorkerActivity1.this);
                biometricPrompt = new BiometricPrompt(WorkerActivity1.this,
                        executor, new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode,
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getApplicationContext(),
                                "Authentication error: " + errString, Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(getApplicationContext(),
                                "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                        sendTimeDateToAdmin();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getApplicationContext(), "Authentication failed",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });

                promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Biometric login for my app")
                        .setSubtitle("Log in using your biometric credential")
                        .setNegativeButtonText("Use account password")
                        .build();

                biometricPrompt.authenticate(promptInfo);

            }
        });

        Bundle bundle = getIntent().getExtras();
        String email = bundle.getString("email");
        String ph = bundle.getString("phoneNumber");
        String pass = bundle.getString("password");
        //Toast.makeText(WorkerActivity1.this, email + " " + ph + " " + pass, Toast.LENGTH_SHORT).show();

        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        DatabaseReference myRef1 = myRef.child(ph);
        DatabaseReference myRef2 = myRef1.child("Locations");
        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Locations locations = snapshot.getValue(Locations.class);
                    Double lt = locations.getLatitude();
                    Double ln = locations.getLongitude();
                    //Toast.makeText(WorkerActivity1.this, "Location : "+lt + " " + ln, Toast.LENGTH_SHORT).show();

                    LatLng ltln = new LatLng(lt,ln);
                    //LatLng ltln = locations1.getLl();
                    //Toast.makeText(WorkerActivity1.this, "Location"+ltln.longitude, Toast.LENGTH_SHORT).show();
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
                    if(count > 1){
                        circle.remove();
                    }
                    circle = mMap.addCircle(new CircleOptions()
                            .center(ltln)
                            .radius(10)
                            .strokeColor(Color.RED)
                            .fillColor(Color.argb(50,0,0,255)));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showDustbinsAssignedtome() {

//        Bundle bundle = getIntent().getExtras();
//        String email = bundle.getString("email");
//        String ph = bundle.getString("phoneNumber");
//        String pass = bundle.getString("password");
//        //Toast.makeText(WorkerActivity1.this, email + " " + ph + " " + pass, Toast.LENGTH_SHORT).show();
//
//        final FirebaseDatabase database = FirebaseDatabase.getInstance();
//        myRef = database.getReference("Users");
//        DatabaseReference myRef1 = myRef.child(ph);
//        DatabaseReference myRef2 = myRef1.child("Locations");
//        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Locations locations = snapshot.getValue(Locations.class);
//                    Double lt = locations.getLatitude();
//                    Double ln = locations.getLongitude();
//                    //Toast.makeText(WorkerActivity1.this, "Location : "+lt + " " + ln, Toast.LENGTH_SHORT).show();
//
//                    LatLng ltln = new LatLng(lt,ln);
//                    //LatLng ltln = locations1.getLl();
//                    //Toast.makeText(WorkerActivity1.this, "Location"+ltln.longitude, Toast.LENGTH_SHORT).show();
//                    //Creating Marker
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    //Set Marker Position
//                    markerOptions.position(ltln);
//                    //Set Latitude And Longitude On Marker
//                    markerOptions.title(ltln.latitude+ " : " + ltln.longitude);
//                    //Zoom the Marker
//                    //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng1,100));
//                    //Add marker On Map
//                    mMap.addMarker(markerOptions);
//                    if(count > 1){
//                        circle.remove();
//                    }
//                    circle = mMap.addCircle(new CircleOptions()
//                            .center(ltln)
//                            .radius(10)
//                            .strokeColor(Color.RED)
//                            .fillColor(Color.argb(50,0,0,255)));
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
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
    public void onMapReady(GoogleMap map) {
        mMap = map;

        if(ContextCompat.checkSelfPermission(WorkerActivity1.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mMap.setMyLocationEnabled(true);
            CheckifWorkerisInsideCircle();
        }
        else
        {
            enableMyLocation();
        }
        // mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);



    }

    void sendTimeDateToAdmin()
    {

    }

    private void CheckifWorkerisInsideCircle() {

        Bundle bundle = getIntent().getExtras();
        String ph = bundle.getString("phoneNumber");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("Users");
        DatabaseReference myRef1 = myRef.child(ph);
        DatabaseReference myRef2 = myRef1.child("Locations");
        myRef2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Locations locations = snapshot.getValue(Locations.class);
                    final Double lt = locations.getLatitude();
                    final Double ln = locations.getLongitude();

                    final LocationRequest locationRequest = new LocationRequest();
                    locationRequest.setInterval(10000);
                    locationRequest.setFastestInterval(3000);
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

                    LocationServices.getFusedLocationProviderClient(WorkerActivity1.this)
                            .requestLocationUpdates(locationRequest,new LocationCallback(){
                                @Override
                                public void onLocationResult(LocationResult locationResult) {
                                    super.onLocationResult(locationResult);
                                    LocationServices.getFusedLocationProviderClient(WorkerActivity1.this)
                                            .removeLocationUpdates(this);
                                    if(locationRequest != null && locationResult.getLocations().size() > 0){
                                        int latestLocationIndex = locationResult.getLocations().size() - 1;
                                        Double latitude =
                                                locationResult.getLocations().get(latestLocationIndex).getLatitude();
                                        Double longitude =
                                                locationResult.getLocations().get(latestLocationIndex).getLongitude();
                                        //Toast.makeText(WorkerActivity1.this, "Location" + latitude + " " + longitude, Toast.LENGTH_SHORT).show();
                                        float[] distance = new float[2];
                                        Location.distanceBetween(latitude,longitude,lt,ln,distance);
                                        if( distance[0] > 10 ){
                                            Toast.makeText(getBaseContext(), "Outside, distance from center: " + distance[0] + " radius: " + circle.getRadius(), Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getBaseContext(), "Inside, distance from center: " + distance[0] + " radius: " + circle.getRadius() , Toast.LENGTH_LONG).show();
                                            fingerprint.setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                            }, Looper.getMainLooper());


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
        private void enableMyLocation()
        {
        ActivityCompat.requestPermissions(WorkerActivity1.this,
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
                CheckifWorkerisInsideCircle();
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
