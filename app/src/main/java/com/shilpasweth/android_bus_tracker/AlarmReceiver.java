package com.shilpasweth.android_bus_tracker;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Calendar;
import java.util.Date;

import static com.shilpasweth.android_bus_tracker.MapsActivity.mBuses;




public class AlarmReceiver extends BroadcastReceiver {





    @Override
    public void onReceive(Context context, Intent intent) {
        // For our recurring task, we'll just display a message
        Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

// Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                //makeUseOfNewLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            return;
        }

        String locationProvider = LocationManager.NETWORK_PROVIDER;
// Or, use GPS location data:
// String locationProvider = LocationManager.GPS_PROVIDER;

        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);


// Or use LocationManager.GPS_PROVIDER

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);

        locationManager.removeUpdates(locationListener);

        String hostel_name=new String("Empty");


        for (int i = 0; i < mBuses.size(); i++) {
            //Toast.makeText(MapsActivity.this,"In Loop",Toast.LENGTH_LONG).show();
            double mindist=-1;

            int min_ind=-1;

            boolean near_condition=false;
            boolean hostel_condition=false;
            LatLng buscoord = new LatLng(mBuses.get(i).getLatitude(), mBuses.get(i).getLongitude());

            String jsonString=new String();
            Log.d("AlarmReceiver","Entered bus loop "+mBuses.get(i).getLastUpdated()+" "+mBuses.get(i).getLatitude()+", "+mBuses.get(i).getLongitude());

            InputStream is = context.getResources().openRawResource(R.raw.hostel_locations);
            Writer writer = new StringWriter();
            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
                jsonString = writer.toString();
                jsonString = jsonString.replaceAll("\t", "");
                jsonString = jsonString.replaceAll("\n", "");
                jsonString = jsonString.replaceAll(" ", "");
                JSONArray obj = new JSONArray(jsonString.toString());



                for(int j=0;j<obj.length();j++){
                    //Toast.makeText(context, "JSON "+obj.getJSONObject(j).get("name"), Toast.LENGTH_SHORT).show();



                    double currdist;

                    LatLng hostelcoord = new LatLng((double)obj.getJSONObject(j).get("latitude"), (double)obj.getJSONObject(j).get("longitude"));

                    currdist=distance(buscoord.latitude,buscoord.longitude,hostelcoord.latitude,hostelcoord.longitude);
                    if(currdist<0.2&&(currdist<mindist||min_ind==-1)){
                        mindist=currdist;
                        min_ind=j;
                        hostel_condition=true;
                        hostel_name=(String)obj.getJSONObject(j).get("name");
                    }
                }
            } catch (Throwable t) {
                Log.e("My App", "Could not parse malformed JSON: \"" + jsonString + "\"");
            }

            double userdist=0.0;

            if(isLocationEnabled(context)){
                userdist=distance(buscoord.latitude,buscoord.longitude,lastKnownLocation.getLatitude(),lastKnownLocation.getLatitude());

                if(userdist<0.2){
                    near_condition=true;
                }
            }

            SharedPreferences sharedPref = context.getSharedPreferences("myPrefs",Context.MODE_PRIVATE);
            Log.e("My App", "Check:" +sharedPref.getString(mBuses.get(i).getVehicleId(),null));
            if (hostel_name.compareToIgnoreCase(sharedPref.getString(mBuses.get(i).getVehicleId(),null))==0){
                hostel_condition=false;
            }
            else{
                if(sharedPref.getString(mBuses.get(i).getVehicleId(),null).compareToIgnoreCase("Empty")!=0
                        ||sharedPref.getString(mBuses.get(i).getVehicleId(),null).compareToIgnoreCase("You")!=0){
                    createHostelNotification(context,lastKnownLocation,sharedPref.getString(mBuses.get(i).getVehicleId(),null),i);
                }
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(mBuses.get(i).getVehicleId(), hostel_name);

                editor.apply();
                editor.commit();
            }


            if(hostel_condition){
                Toast.makeText(context, "Hostel Name: "+hostel_name, Toast.LENGTH_SHORT).show();




                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                String hostel_select=prefs.getString("hostel_preference", null);
                //Toast.makeText(context, "Preference: "+hostel_select, Toast.LENGTH_SHORT).show();
                if(hostel_select.compareToIgnoreCase(hostel_name)==0){
                    createHostelNotification(context,lastKnownLocation,hostel_name,i);
                }

            }
            else if(near_condition){
                hostel_name="You";
                if (hostel_name.compareToIgnoreCase(sharedPref.getString(mBuses.get(i).getVehicleId(),null))!=0){
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString(mBuses.get(i).getVehicleId(), hostel_name);

                    editor.apply();
                    createNearNotification(context,lastKnownLocation,i);
                }

            }

        }






    }

    public void createNearNotification(Context context,Location location,int bus_index){
        String CHANNEL_ID="C1";

        Date currentTime = Calendar.getInstance().getTime();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bus_marker)
                .setContentTitle("NITT Bus Notification")
                .setContentText("There is a bus near you")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "NITT Bus Tracker";
            String description = "Channel for bus tracker notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, mBuilder.build());
        }
    }

    public void createHostelNotification(Context context,Location location,String hostel,int bus_index){
        String CHANNEL_ID="C1";

        Date currentTime = Calendar.getInstance().getTime();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bus_marker)
                .setContentTitle("NITT Bus Notification")
                .setContentText("Bus is now near"+" "+hostel)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "NITT Bus Tracker";
            String description = "Channel for bus tracker notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(Integer.parseInt(mBuses.get(bus_index).getVehicleId()), mBuilder.build());
        }
    }

    public void createLeftNotification(Context context,Location location,String hostel,int bus_index){
        String CHANNEL_ID="C1";

        Date currentTime = Calendar.getInstance().getTime();

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.bus_marker)
                .setContentTitle("NITT Bus Notification")
                .setContentText("Bus has left"+" "+hostel)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            CharSequence name = "NITT Bus Tracker";
            String description = "Channel for bus tracker notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(channel);

// notificationId is a unique int for each notification that you must define
            notificationManager.notify(Integer.parseInt(mBuses.get(bus_index).getVehicleId()), mBuilder.build());
        }
    }


    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        }else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

}


