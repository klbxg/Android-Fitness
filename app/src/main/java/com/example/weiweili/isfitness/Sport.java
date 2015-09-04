package com.example.weiweili.isfitness;

import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class Sport extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mLocationClient;
    private LocationListener mListener;
    private LatLng lastLatLng;
    private Polyline line;
    Button bStartSport;
    TextView tTime;

    long startTime = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int hours = minutes / 60;
            minutes = minutes % 60;

            tTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));

            timerHandler.postDelayed(this, 500);
        }
    };

    @Override
    public void onConnected(Bundle bundle) {
        Toast.makeText(this, "Ready to map!", Toast.LENGTH_SHORT).show();
        Location currentLocation = LocationServices.FusedLocationApi
                .getLastLocation(mLocationClient);
        if (currentLocation == null) {
            Toast.makeText(this, "Couldn't connect!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            lastLatLng = latLng;
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                    latLng, 18
            );
            mMap.animateCamera(update);
        }

//        mListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(Location location) {
//                Toast.makeText(Sport.this, "Location changed: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
//                gotoLocation(location.getLatitude(), location.getLongitude(), 18);
//                LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
//                drawLine(curLatLng);
//                lastLatLng = curLatLng;
//            }
//        };
//        LocationRequest request = LocationRequest.create();
//        request.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
//        request.setInterval(5000);
//        request.setFastestInterval(1000);
//        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, mListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sport);
        bStartSport = (Button) findViewById(R.id.bStartSport);
        bStartSport.setOnClickListener(this);
        tTime = (TextView) findViewById(R.id.tTime);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        //Toast.makeText(this,"resumed", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, mListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bStartSport:
                if (bStartSport.getText().equals("stop")) {
                    timerHandler.removeCallbacks(timerRunnable);
                    bStartSport.setText("start");
                }
                else {
                    Toast.makeText(this, "startsport", Toast.LENGTH_SHORT).show();
                    mListener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Toast.makeText(Sport.this, "Location changed: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                            gotoLocation(location.getLatitude(), location.getLongitude(), 18);
                            LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            drawLine(curLatLng);
                            lastLatLng = curLatLng;
                        }
                    };
                    LocationRequest request = LocationRequest.create();
                    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    request.setInterval(5000);
                    request.setFastestInterval(1000);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, mListener);

                    startTime = System.currentTimeMillis();
                    timerHandler.postDelayed(timerRunnable, 0);
                    bStartSport.setText("stop");
                }
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void drawLine(LatLng latlng) {
        PolylineOptions lineOptions = new PolylineOptions()
                .add(latlng)
                .add(lastLatLng)
                .color(Color.GREEN);
        line = mMap.addPolyline(lineOptions);
    }
    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        mLocationClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationClient.connect();
        mMap.setMyLocationEnabled(true);
        //mMap.addMarker(new MarkerOptions().position(new LatLng(40.760167, -73.979988)).title("Marker"));
    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }
}
