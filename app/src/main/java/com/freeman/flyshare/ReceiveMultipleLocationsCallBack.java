package com.freeman.flyshare;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;

/**
 * Created by freeman on 4/3/2016.
 */
public interface ReceiveMultipleLocationsCallBack {
    void onLocationReceive(boolean isSuccessful, LinkedList<MyWaypoint> locations);
}
