package com.shilpasweth.android_bus_tracker;

/**
 * Created by Lenovo on 4/8/2017.
 */

public class BusInfo {
    public String vehicle_id;

    private String vehicle_type;

    private String vehicle_license;

    private float latitude;

    private float longitude;

    public String last_updated;

    public String getVehicleId(){return vehicle_id;}

    public String getVehicleType(){return vehicle_type;}

    public String getVehicleLicense(){return vehicle_license;}

    public float getLatitude(){return latitude;}

    public float getLongitude(){return longitude;}

    public String getLastUpdated(){return last_updated;}

}
