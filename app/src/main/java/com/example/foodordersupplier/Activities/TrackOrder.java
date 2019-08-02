package com.example.foodordersupplier.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.foodordersupplier.Common.Common;
import com.example.foodordersupplier.Common.DirectionJsonParser;
import com.example.foodordersupplier.Model.Request;
import com.example.foodordersupplier.R;
import com.example.foodordersupplier.Remote.IGeoCoordinates;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TrackOrder extends FragmentActivity implements OnMapReadyCallback {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationCallback locationCallback;
    LocationRequest locationRequest;

    private GoogleMap mMap;
    Location mLastLocation;
    Marker currentMarker;
    private IGeoCoordinates mService;
    Polyline polyline;
    Button btnTrack,btnCall;
    private static final String TAG = "TrackOrder";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_order);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mService = Common.getGeoCodeService();
        btnCall = findViewById(R.id.btnCall);
        btnTrack = findViewById(R.id.btnTrack);

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Log.i(TAG, "onClick: Current Request"+ Common.currentRequest.getPhone());
                Intent intentCall = new Intent(Intent.ACTION_DIAL);


                intentCall.setData(Uri.parse("tel:"+Common.currentRequest.getPhone()));
                startActivity(intentCall);
            }
        });

        btnTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippedOrder();
            }
        });


        buildLocationRequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void shippedOrder() {
        FirebaseDatabase.getInstance().getReference("pendingShipment")
                .child(Common.currentSupplier.getEmail().replace(".","_"))
                .child(Common.currentKey)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> map = new HashMap<>();
                        map.put("status","3");

                        FirebaseDatabase.getInstance()
                                .getReference("Requests")
                                .updateChildren(map)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                FirebaseDatabase.getInstance().getReference(Common.SUPPLIER_ORDERS)
                                        .child(Common.currentKey)
                                        .removeValue()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(TrackOrder.this, "Shipped", Toast.LENGTH_SHORT).show();
                                                finish();

                                            }
                                        });
                            }
                        })
                        ;
                    }
                });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                mLastLocation = location;
                LatLng yourLoction = new LatLng(location.getLatitude(), location.getLongitude());
                currentMarker = mMap.addMarker(new MarkerOptions().position(yourLoction).title("Your Location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(yourLoction));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));

            }
        });
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                mLastLocation = locationResult.getLastLocation();

                if (currentMarker != null)
                    currentMarker.setPosition(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

                Common.updateShippingInfo(Common.currentKey,mLastLocation);

                mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude())));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15.0f));
                addRequestMarkerRoute(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()),Common.currentRequest);



            }
        };

    }

    private void addRequestMarkerRoute(final LatLng shipLocation, Request request) {
        if (polyline !=  null)
            polyline.remove();

        if (request.getAddress() !=  null && !request.getAddress().isEmpty()){
            mService.getGeoCode(request.getAddress(),"false","AIzaSyB95eskfrIUWbvkxqIxnhWWIsjsQpUysUw").enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    try {
                        Log.i(TAG, "onResponse: This is getGeo" + response.body());
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        String lat = ((JSONArray)jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lat").toString();


                        String lng = ((JSONArray)jsonObject.get("results"))
                                .getJSONObject(0)
                                .getJSONObject("geometry")
                                .getJSONObject("location")
                                .get("lng").toString();

                        LatLng orderLocation = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.add_to_cart);

                        bitmap = Common.scaleBitmap(bitmap,70,70);

                        MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                                .title("Order of " + Common.currentRequest.getEmail())
                                .position(orderLocation);
                        mMap.addMarker(marker);

                        mService.getDirections(shipLocation.latitude+ ","+shipLocation.longitude,
                                orderLocation.latitude+","+orderLocation.longitude,"false","AIzaSyB95eskfrIUWbvkxqIxnhWWIsjsQpUysUw")
                                .enqueue(new Callback<String>() {
                                    @Override
                                    public void onResponse(Call<String> call, Response<String> response) {
                                        Log.i(TAG, "onResponse: " + response.body());
                                        new ParserTask().execute(response.body().toString());

                                    }

                                    @Override
                                    public void onFailure(Call<String> call, Throwable t) {

                                    }
                                });




                    }
                    catch (JSONException e){

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });

        }

        else {
            if (request.getLatLng() != null && !request.getLatLng().isEmpty()){
                String[] latLng = request.getLatLng().split(",");
                LatLng orderLocation = new LatLng(Double.parseDouble(latLng[0]),Double.parseDouble(latLng[1]));

                Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.add_to_cart);

                bitmap = Common.scaleBitmap(bitmap,70,70);

                MarkerOptions marker = new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(bitmap))
                        .title("Order of " + Common.currentRequest.getEmail())
                        .position(orderLocation);
                mMap.addMarker(marker);
                mService.getDirections(mLastLocation.getLatitude()+","+mLastLocation.getLongitude(),
                        orderLocation.latitude +","+orderLocation.longitude,"false","AIzaSyB95eskfrIUWbvkxqIxnhWWIsjsQpUysUw")
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {
                                new ParserTask().execute(response.body().toString());
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {
                                Log.i(TAG, "onFailure: Failed to locate" + t.getMessage());

                            }
                        });
            }
        }


    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);


    }

    @Override
    protected void onStop() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onStop();
    }

    private  class ParserTask extends AsyncTask<String,Integer,List<List<HashMap<String,String>>>> {
        ProgressDialog dialog = new ProgressDialog(TrackOrder.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Hang On...");
            dialog.show();
        }

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject object;
            List<List<HashMap<String,String>>> routes = null;
            try {
                object = new JSONObject(strings[0]);
                DirectionJsonParser parser = new DirectionJsonParser();
                routes = parser.parse(object);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            dialog.dismiss();

            ArrayList points = null;
            PolylineOptions polylineOptions = null;

            for (int i=0;i<lists.size();i++){
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                List<HashMap<String,String>> path = lists.get(i);

                for (int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));

                    LatLng pos = new LatLng(lat,lng);

                    points.add(pos);
                }

                polylineOptions.addAll(points);
                polylineOptions.width(12);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);

            }
            mMap.addPolyline(polylineOptions);


        }
    }
}
