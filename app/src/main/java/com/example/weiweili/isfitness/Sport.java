package com.example.weiweili.isfitness;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.AsyncTask;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Rect;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.app.AlertDialog;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class Sport extends FragmentActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener{

    private static final int SAVE_DIALOG_ID = 0;
    private static final int SHARE_DIALOG_ID = 1;
    ImageView iPhoto;
    TextView tusername;
    private static final String SERVER_ADDRESS = "http://isfitness.site50.net/";

    ArrayList<LatLng> allLatLng = new ArrayList<>();

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mLocationClient;
    private LocationListener mListener;
    private LatLng lastLatLng;
    private Location lastLocation;
    private float[] distance = new float[1];
    private float distanceSum;
    private Polyline line;
    Button bStartSport;
    Button bResume;
    Button bStop;
    TextView tTime;
    TextView tDistance;
    TextView tCalory;
    TextView tPace;
    TextView tSpeed;
    String averageSpeed;
    String thisDistance;
    String thisTime;
    Bitmap thisSport;
    UserLocalStore userLocalStore;

    long startTime = 0;
    long lastTime = 0;
    double timeTotal = 0;
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime + lastTime;
            timeTotal = millis / (1000.0 * 60.0 * 60.0);
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
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);

        if (currentLocation == null) {
            Toast.makeText(this, "Couldn't connect!", Toast.LENGTH_SHORT).show();
        } else {
            LatLng latLng = new LatLng(
                    currentLocation.getLatitude(),
                    currentLocation.getLongitude()
            );
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(
                    latLng, 18
            );
            mMap.animateCamera(update);
        }
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
        bResume = (Button) findViewById(R.id.bResume);
        bStop = (Button) findViewById(R.id.bStop);
        bResume.setOnClickListener(this);
        bStop.setOnClickListener(this);
        tTime = (TextView) findViewById(R.id.tTime);
        tDistance = (TextView) findViewById(R.id.tDistance);
        tCalory = (TextView) findViewById(R.id.tCalory);
        tPace = (TextView) findViewById(R.id.tPace);
        tSpeed = (TextView) findViewById(R.id.tSpeed);
        iPhoto = (ImageView) findViewById(R.id.iMyHead);
        tusername = (TextView) findViewById(R.id.username);
        String username;

        userLocalStore = new UserLocalStore(this);
        username = userLocalStore.getLoggedInUser().username;
        tusername.setText(username);
        new DownloadPhoto(username, iPhoto).execute();
        iPhoto.setAlpha(0x99);

        distanceSum = 0;
        mListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //Toast.makeText(Sport.this, "Location changed: " + location.getLatitude() + ", " + location.getLongitude(), Toast.LENGTH_SHORT).show();
                gotoLocation(location.getLatitude(), location.getLongitude(), 18);
                LatLng curLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                if (lastLatLng != null) {
                    drawLine(curLatLng);
                }
                lastLatLng = curLatLng;
                allLatLng.add(lastLatLng);
                if (lastLocation != null) {
                    Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), location.getLatitude(), location.getLongitude(), distance);
                    distanceSum += distance[0] / 1000;
                    tDistance.setText(String.format("%.02f", distanceSum) + "km");
                    if (timeTotal != 0) {
                        tSpeed.setText(String.format("%.02f", distanceSum / timeTotal));
                    }
                    else {
                        tSpeed.setText("--");
                    }
                    if (distanceSum != 0) {
                        tPace.setText(String.format("%.02f", timeTotal * 60.0 / distanceSum));
                    }
                    else {
                        tPace.setText("--");
                    }
                }
                lastLocation = location;
            }
        };
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
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
                if (bStartSport.getText().equals("Pause")) {
                    String tmp = tTime.getText().toString();
                    int h = Integer.valueOf(tmp.substring(0, 2));
                    int m = Integer.valueOf(tmp.substring(3, 5));
                    int s = Integer.valueOf(tmp.substring(6, 8));
                    lastTime = (h * 3600 + m * 60 + s) * 1000;
                    timerHandler.removeCallbacks(timerRunnable);
                    bStartSport.setText("Start");

                    // Adjust the map to show all the lines
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    if (allLatLng.size() != 0) {
                        for (LatLng location : allLatLng) {
                            builder.include(location);
                        }
                        LatLngBounds bounds = builder.build();
                        int padding = 20; // offset from edges of the map in pixels
                        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
                                padding);
                        mMap.moveCamera(cu);
                        mMap.animateCamera(cu);
                    }

                    LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, mListener);
                    bResume.setVisibility(View.VISIBLE);
                    bStop.setVisibility(View.VISIBLE);

                }
                else {
                    Toast.makeText(this, "startsport", Toast.LENGTH_SHORT).show();
                    //mMap.setMyLocationEnabled(true);
                    mMap.clear();

                    bResume.setVisibility(View.INVISIBLE);
                    bStop.setVisibility(View.INVISIBLE);
                    lastTime = 0;
                    lastLatLng = null;

                    LocationRequest request = LocationRequest.create();
                    request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    request.setInterval(5000);
                    request.setFastestInterval(1000);
                    LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, mListener);

                    startTime = System.currentTimeMillis();

                    distanceSum = 0;
                    lastLocation = null;
                    tDistance.setText(String.format("%.02f", 0.0));
                    tPace.setText("--");
                    tSpeed.setText("--");

                    timerHandler.postDelayed(timerRunnable, 0);
                    bStartSport.setText("Pause");
                }
                break;

            case R.id.bResume:
                bResume.setVisibility(View.INVISIBLE);
                bStop.setVisibility(View.INVISIBLE);
                bStartSport.setText("Pause");

                lastLocation = null;
                lastLatLng = null;

                LocationRequest request = LocationRequest.create();
                request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                request.setInterval(5000);
                request.setFastestInterval(1000);
                LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, request, mListener);

                startTime = System.currentTimeMillis();
                timerHandler.postDelayed(timerRunnable, 0);
                break;

            case R.id.bStop:
                //mMap.setMyLocationEnabled(false);
                TextView distance = (TextView) findViewById(R.id.tDistanceResult);
                TextView speed = (TextView) findViewById(R.id.tSpeedResult);
                distance.setText("Distance: " + tDistance.getText().toString());
                speed.setText("Speed: " + tSpeed.getText().toString() + "km/h");

                averageSpeed = tSpeed.getText().toString();
                thisDistance = tDistance.getText().toString();
                thisTime = tTime.getText().toString();
                bResume.setVisibility(View.INVISIBLE);
                bStop.setVisibility(View.INVISIBLE);
                bStartSport.setText("Start");
//                // Adjust the map to show all the lines
//                LatLngBounds.Builder builder = new LatLngBounds.Builder();
//                if (allLatLng.size() != 0) {
//                    for (LatLng location : allLatLng) {
//                        builder.include(location);
//                    }
//                    LatLngBounds bounds = builder.build();
//                    int padding = 20; // offset from edges of the map in pixels
//                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,
//                            padding);
//                    mMap.moveCamera(cu);
//                    mMap.animateCamera(cu);
//                }

                lastTime = 0;
                distanceSum = 0;
                lastLocation = null;
                tTime.setText(String.format("%02d:%02d:%02d", 0, 0, 0));
                View sportResult = findViewById(R.id.sportResult);
                sportResult.setVisibility(View.VISIBLE);
                captureMapScreen();
                sportResult.setVisibility(View.INVISIBLE);
                showDialog(SAVE_DIALOG_ID);
                break;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case SAVE_DIALOG_ID:
                AlertDialog.Builder saveDialogBuilder = new AlertDialog.Builder(this);
                saveDialogBuilder.setTitle(R.string.save_dialog_title);
                saveDialogBuilder.setIcon(android.R.drawable.btn_star);
                saveDialogBuilder.setMessage("Do you want to save this sport record?");
                saveDialogBuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),
                                        "Save OK!", Toast.LENGTH_SHORT).show();
                                showDialog(SHARE_DIALOG_ID);
                                User user = userLocalStore.getLoggedInUser();
                                if (thisSport == null) {
                                    Log.d("thisSport", "null");
                                }
                                new UploadImage(thisSport, user.username).execute();
                                return;
                            }
                        });

                saveDialogBuilder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),
                                        "Save Cancel!", Toast.LENGTH_SHORT)
                                        .show();
                                showDialog(SHARE_DIALOG_ID);
                                return;
                            }
                        });

                return saveDialogBuilder.create();
            case SHARE_DIALOG_ID:
                AlertDialog.Builder shareDialogBuilder = new AlertDialog.Builder(this);
                shareDialogBuilder.setTitle(R.string.share_dialog_title);
                shareDialogBuilder.setIcon(android.R.drawable.btn_star);
                shareDialogBuilder.setMessage("Do you want to share this sport record?");
                shareDialogBuilder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),
                                        "Share OK!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(Sport.this, SharingActivity.class);
                                ByteArrayOutputStream bs = new ByteArrayOutputStream();
                                thisSport.compress(Bitmap.CompressFormat.PNG, 50, bs);
                                intent.putExtra("sportResult", bs.toByteArray());
                                startActivity(intent);
                                return;
                            }
                        });

                shareDialogBuilder.setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),
                                        "Share Cancel!", Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                        });

                return shareDialogBuilder.create();
            default:
                return null;
        }
    }

    private class UploadImage extends AsyncTask<Void, Void, Void> {
        Bitmap image;
        String username;
        public UploadImage(Bitmap image, String username) {
            this.image = image;
            this.username = username;
        }
        @Override
        protected Void doInBackground(Void... params) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
            ArrayList<NameValuePair> dataToSend = new ArrayList<>();
            dataToSend.add(new BasicNameValuePair("activity", encodedImage));
            dataToSend.add(new BasicNameValuePair("username", username));
            dataToSend.add(new BasicNameValuePair("distance", thisDistance));
            dataToSend.add(new BasicNameValuePair("speed", averageSpeed));
            dataToSend.add(new BasicNameValuePair("time", thisTime));

            HttpParams httpRequestParams = getHttpRequestParams();
            HttpClient client = new DefaultHttpClient(httpRequestParams);
            HttpPost post = new HttpPost(SERVER_ADDRESS + "SaveActivity.php");

            try {
                post.setEntity(new UrlEncodedFormEntity(dataToSend));
                client.execute(post);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(getApplicationContext(), "Activity Uploaded", Toast.LENGTH_SHORT).show();
        }
    }

    private HttpParams getHttpRequestParams() {
        HttpParams httpRequestParams = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpRequestParams, 1000 * 30);
        HttpConnectionParams.setSoTimeout(httpRequestParams, 1000 * 30);
        return httpRequestParams;
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
    }

    private void gotoLocation(double lat, double lng, float zoom) {
        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, zoom);
        mMap.moveCamera(update);
    }

    private void drawLine(LatLng latlng) {
        PolylineOptions lineOptions = new PolylineOptions()
                .add(latlng)
                .add(lastLatLng)
                .color(Color.GREEN);
        line = mMap.addPolyline(lineOptions);
    }

    public void captureMapScreen() {
        SnapshotReadyCallback callback = new SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                try {
                    View sportResult = findViewById(R.id.sportResult);
                    sportResult.setDrawingCacheEnabled(true);

                    Bitmap backBitmap = sportResult.getDrawingCache();

                    Bitmap bmOverlay = Bitmap.createBitmap(
                            snapshot.getWidth(), snapshot.getHeight(),
                            snapshot.getConfig());
                    Canvas canvas = new Canvas(bmOverlay);
                    canvas.drawBitmap(snapshot, new Matrix(), null);
                    canvas.drawBitmap(backBitmap, 0, 0, null);

//                    FileOutputStream out = new FileOutputStream(
//                            Environment.getExternalStorageDirectory()
//                                    + "/MapScreenShot"
//                                    + System.currentTimeMillis() + ".png");
                    thisSport = bmOverlay;

                    //bmOverlay.compress(Bitmap.CompressFormat.PNG, 90, out);
                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        };

        mMap.snapshot(callback);
    }

    private class DownloadPhoto extends AsyncTask<Void, Void, Bitmap> {
        String name;
        ImageView user_image;

        public DownloadPhoto(String name, ImageView user_image) {
            this.name = name;
            this.user_image = user_image;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {

            String url = SERVER_ADDRESS + "photo/" + name + ".JPG";
            Log.d("url", url);
            try {
                URLConnection connection = new URL(url).openConnection();
                connection.setConnectTimeout(1000 * 30);
                connection.setReadTimeout(1000 * 30);
                return BitmapFactory.decodeStream((InputStream) connection.getContent(), null, null);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            if (bitmap != null) {
                user_image.setImageBitmap(getRoundedShape(bitmap));
            }
        }
    }

    private Bitmap getRoundedShape(Bitmap scaleBitmapImage) {
        int targetWidth = 100;
        int targetHeight = 70;
        Bitmap targetBitmap = Bitmap.createBitmap(targetWidth,
                targetHeight,Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(targetBitmap);
        Path path = new Path();
        path.addCircle(((float) targetWidth - 1) / 2,
                ((float) targetHeight - 1) / 2,
                (Math.min(((float) targetWidth),
                        ((float) targetHeight)) / 2),
                Path.Direction.CCW);

        canvas.clipPath(path);
        Bitmap sourceBitmap = scaleBitmapImage;
        canvas.drawBitmap(sourceBitmap,
                new Rect(0, 0, sourceBitmap.getWidth(),
                        sourceBitmap.getHeight()),
                new Rect(0, 0, targetWidth, targetHeight), null);
        return targetBitmap;
    }
}