package com.shilpasweth.android_bus_tracker;

import java.math.BigDecimal;

/**
 * Created by Lenovo on 4/8/2017.
 */

public class BusInfo {
    public String vehicle_id;

    private String vehicle_type;

    private String vehicle_license;

    private double latitude;

    private double longitude;

    public String last_updated;

    public String getVehicleId(){return vehicle_id;}

    public String getVehicleType(){return vehicle_type;}

    public String getVehicleLicense(){return vehicle_license;}

    public double getLatitude(){return latitude;}

    public double getLongitude(){return longitude;}

    public String getLastUpdated(){return last_updated;}

    public void putVehicleId(String v_id){vehicle_id=v_id;}

    public void putVehicleType(String v_type){vehicle_type=v_type;}

    public void putVehicleLicense(String v_license){vehicle_license=v_license;}

    public void putLatitude(double lat){
        latitude=lat;
    }

    public void putLongitude(double lon){
        longitude=lon;
    }

    public void putLastUpdated(String last_up){last_updated=last_up;}

}
