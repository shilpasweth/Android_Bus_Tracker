package com.shilpasweth.android_bus_tracker;

import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

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

    private static List<BusInfo> mBuses= new ArrayList<>();;
    private MapsPresenter mapsPresenter;

    SupportMapFragment mapFragment;

    String url = "https://bustracker-nitt.000webhostapp.com/poll.php";

    int MY_PERMISSIONS_REQUEST_INTERNET=0;

    static int busno=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        fetchBuses();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

        final CountDownTimer mapRefresh = new CountDownTimer(5000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                //mTextField.setText("done!");

                fetchBuses();
                this.start();
            }
        };
        mapRefresh.start();

    }

    void setPresenterView(List<BusInfo> buses) {
        mapsPresenter = new MapsPresenterImpl();
        mapsPresenter.setView(this);
        mapsPresenter.onStart(buses);
    }

    @Override
    public void updateBuses(List<BusInfo> buses) {
        mBuses = buses;

    }

    public void busFocus(View view){
        if(mBuses.size()!=0) {
            busno = (busno + 1) %mBuses.size();
            LatLng sydney = new LatLng(mBuses.get(busno).getLatitude(), mBuses.get(busno).getLongitude());

            // mMap.setMinZoomPreference(18.0f);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney,18.0f));
        }
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
    public void fetchBuses(){
        StringRequest jsObjRequest = new StringRequest
                (Request.Method.GET, url, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        // mTxtDisplay.setText("Response: " + response.toString());
                        // Toast.makeText(MapsActivity.this,response,Toast.LENGTH_LONG).show();

                        // Toast.makeText(MapsActivity.this,dataJsonObj.toString(),Toast.LENGTH_LONG).show();
                        JSONArray dataJsonArr=null;
                        try {
                            dataJsonArr = new JSONArray(response);
                            //Toast.makeText(MapsActivity.this,"dataJsonArr success",Toast.LENGTH_LONG).show();

                            int no=dataJsonArr.length();

                            //Toast.makeText(MapsActivity.this,"Length "+no,Toast.LENGTH_LONG).show();
                            for(int i=0;i<no;i++){
                                JSONObject c = dataJsonArr.getJSONObject(i);
                                BusInfo bus= new BusInfo();
                                bus.putVehicleId(c.getString("vehicleId"));
                                bus.putVehicleType(c.getString("vehicleType"));
                                bus.putVehicleLicense(c.getString("licenseNumber"));
                                bus.putLatitude(Float.valueOf(c.getString("lat")));
                                bus.putLongitude(Float.valueOf(c.getString("lon")));
                                bus.putLastUpdated(c.getString("lastUpdatedAt"));

                                mBuses.add(bus);


                            }


                            onMapRefresh();
                            // Toast.makeText(MapsActivity.this,"Length "+mBuses.size(),Toast.LENGTH_LONG).show();
                            //  Toast.makeText(MapsActivity.this,c.toString(),Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            //Toast.makeText(MapsActivity.this,"dataJsonArr fail",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                        //Toast.makeText(MapsActivity.this,dataJsonArr.length(),Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        //Toast.makeText(MapsActivity.this,"Error",Toast.LENGTH_LONG).show();

                    }
                });

        Volley.newRequestQueue(this).add(jsObjRequest);
    }

    public void onMapRefresh(){

        // Add a marker in Sydney and move the camera
        //while(mBuses.size()==0){}
        mMap.clear();
        LatLng sydney = new LatLng(10.761, 78.816);
        //Toast.makeText(MapsActivity.this,"Out Loop",Toast.LENGTH_LONG).show();
        for(int i=0;i<mBuses.size();i++)
        {
            //Toast.makeText(MapsActivity.this,"In Loop",Toast.LENGTH_LONG).show();
            LatLng bus = new LatLng(mBuses.get(i).getLatitude(), mBuses.get(i).getLongitude());
            if(i==1)
                sydney=bus;
            if(mBuses.get(i).getVehicleType().equalsIgnoreCase("generalBus")){
                mMap.addMarker(new MarkerOptions().position(bus).title("General Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_general)));
            }
            if(mBuses.get(i).getVehicleType().equalsIgnoreCase("boysBus")){
                mMap.addMarker(new MarkerOptions().position(bus).title("Boy's Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_boys)));
            }
            if(mBuses.get(i).getVehicleType().equalsIgnoreCase("girlsBus")){
                mMap.addMarker(new MarkerOptions().position(bus).title("Girl's Bus").icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_marker_girls)));
            }

        }
       // mMap.setMinZoomPreference(18.0f);
       //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //while(mBuses.size()==0){}
        LatLng sydney = new LatLng(10.762, 78.816);
        //Toast.makeText(MapsActivity.this,"Out Loop",Toast.LENGTH_LONG).show();

        mMap.setMinZoomPreference(15.0f);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney,18.0f));
    }
}
