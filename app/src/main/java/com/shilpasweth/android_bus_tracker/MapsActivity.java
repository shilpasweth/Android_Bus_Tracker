package com.shilpasweth.android_bus_tracker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.CountDownTimer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, MapsPresenterImpl.View {

    private GoogleMap mMap;

    private static List<BusInfo> mBuses = new ArrayList<>();
    ;
    private MapsPresenter mapsPresenter;

    SupportMapFragment mapFragment;

    String url = "https://bustracker-nitt.000webhostapp.com/poll.php";

    int first_time = 0;

    int wait=0;


    static int busno = 1;

    ProgressDialog pDialog = null;

    TextView gen_txt;
    TextView boy_txt;
    TextView girl_txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("MainActivity","Entered onCreate()");
        first_time = 0;

        fetchBuses();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        gen_txt=(TextView)findViewById(R.id.general_text);
        boy_txt=(TextView)findViewById(R.id.boy_text);
        girl_txt=(TextView)findViewById(R.id.girl_text);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);


        //Timer to update bus positions
        final CountDownTimer mapRefresh = new CountDownTimer(1000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                //mTextField.setText("done!");
                Log.d("MainActivity","Entered CountDownTimer-onFinish()");
                if(wait==0) {
                    wait = 1;
                    fetchBuses();
                    onMapRefresh();
                    wait = 0;
                    this.start();
                }
                Log.d("MainActivity","Exited CountDownTimer-onFinish()");
            }
        };



        mapRefresh.start();
        Log.d("MainActivity","Exited onCreate()");

    }

    void setPresenterView(List<BusInfo> buses) {
        mapsPresenter = new MapsPresenterImpl();
        mapsPresenter.setView(this);
        mapsPresenter.onStart(buses);
    }

    //MYP stuff not done yet
    @Override
    public void updateBuses(List<BusInfo> buses) {
        Log.d("MainActivity","Entered updateBuses()");
        mBuses = buses;
        Log.d("MainActivity","Exited updateBuses()");

    }

    //Move focus to a bus
    public void busFocus(View view) {
        Log.d("MainActivity","Entered busFocus()");
        if (mBuses.size() != 0) {
            busno = (busno + 1) % mBuses.size();
            LatLng sydney = new LatLng(mBuses.get(busno).getLatitude(), mBuses.get(busno).getLongitude());

            mMap.setMinZoomPreference(16.0f);//upper bound
            mMap.setMaxZoomPreference(18.0f);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 17.0f));
        }
        Log.d("MainActivity","Exited busFocus()");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     *
     *
     */
    public void fetchBuses() {
        Log.d("MainActivity","Entered fetchBuses()");
        if (first_time == 0) {
            pDialog = new ProgressDialog(this);
            pDialog.setMessage("Loading...");
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(true);
            pDialog.show();
        }
        StringRequest jsObjRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        JSONArray dataJsonArr = null;
                        try {
                            dataJsonArr = new JSONArray(response);
                            //Toast.makeText(MapsActivity.this,"dataJsonArr success",Toast.LENGTH_LONG).show();

                            int no = dataJsonArr.length();

                            //Toast.makeText(MapsActivity.this,"Length "+no,Toast.LENGTH_LONG).show();
                            mBuses.clear();

                            for (int i = 0; i < no; i++) {
                                JSONObject c = dataJsonArr.getJSONObject(i);
                                BusInfo bus = new BusInfo();
                                bus.putVehicleId(c.getString("vehicleId"));
                                bus.putVehicleType(c.getString("vehicleType"));
                                bus.putVehicleLicense(c.getString("licenseNumber"));
                                bus.putLatitude(Double.valueOf(c.getString("lat")));
                                bus.putLongitude(Double.valueOf(c.getString("lon")));
                                bus.putLastUpdated(c.getString("lastUpdatedAt"));
                                Log.d("MainActivity","Entered fetchBuses()-lastUpdateTime "+bus.getLastUpdated() +" "+bus.getLatitude()+", "+bus.getLongitude());
                                mBuses.add(bus);


                            }


                            onMapRefresh();

                            pDialog.dismiss();
                            first_time++;

                            // Toast.makeText(MapsActivity.this,"Length "+mBuses.size(),Toast.LENGTH_LONG).show();
                            //  Toast.makeText(MapsActivity.this,c.toString(),Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            //Toast.makeText(MapsActivity.this,"dataJsonArr fail",Toast.LENGTH_LONG).show();
                            e.printStackTrace();

                            pDialog.dismiss();
                            first_time++;

                        }

                        //Toast.makeText(MapsActivity.this,dataJsonArr.length(),Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        if (first_time == 0) {
                            Toast.makeText(MapsActivity.this, "Please check the Internet connection", Toast.LENGTH_LONG).show();
                        }
                        pDialog.dismiss();
                        first_time++;
                        //Toast.makeText(MapsActivity.this,"Error",Toast.LENGTH_LONG).show();

                    }
                });

        Volley.newRequestQueue(this).add(jsObjRequest);
        Log.d("MainActivity","Exited fetchBuses()");
    }

    public void onMapRefresh() {
        Log.d("MainActivity","Entered onMapRefresh()");
        mMap.clear();
        LatLng sydney = new LatLng(10.761, 78.816);

        int general=0;
        int boy=0;
        int girl=0;

        for (int i = 0; i < mBuses.size(); i++) {
            //Toast.makeText(MapsActivity.this,"In Loop",Toast.LENGTH_LONG).show();
            Log.d("MainActivity","Entered onMapRefresh()-lastUpdatedTime "+mBuses.get(i).getLastUpdated()+" "+mBuses.get(i).getLatitude()+", "+mBuses.get(i).getLongitude());
            LatLng bus = new LatLng(mBuses.get(i).getLatitude(), mBuses.get(i).getLongitude());
            if(i==1)
                sydney=bus;
            if(mBuses.get(i).getVehicleType().equalsIgnoreCase("busNormal")){
                general++;
                mMap.addMarker(new MarkerOptions().position(bus).title("General Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_general)));
            }
            if(mBuses.get(i).getVehicleType().equalsIgnoreCase("boysBus")){
                boy++;
                mMap.addMarker(new MarkerOptions().position(bus).title("Boy's Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_boys)));
            }
            if(mBuses.get(i).getVehicleType().equalsIgnoreCase("girlsBus")){
                girl++;
                mMap.addMarker(new MarkerOptions().position(bus).title("Girl's Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_girls)));
            }




        }
        gen_txt.setText(String.valueOf(general));
        boy_txt.setText(""+boy);
        girl_txt.setText(""+girl);
        Log.d("MainActivity","Exited onMapRefresh()");
    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MainActivity","Entered onMapReady()");
        mMap = googleMap;

        int MY_PERMISSIONS_REQUEST = 0;

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);


        } else {
            Toast.makeText(MapsActivity.this, "Permission not given", Toast.LENGTH_LONG).show();// Show rationale and request permission.
        }

        // Add a marker in Sydney and move the camera
        //while(mBuses.size()==0){}
        LatLng sydney = new LatLng(10.762, 78.816);
        //Toast.makeText(MapsActivity.this,"Out Loop",Toast.LENGTH_LONG).show();

        //mMap.setMinZoomPreference(15.0f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,17.0f));
        Log.d("MainActivity","Exited onMapReady()");
    }
}
