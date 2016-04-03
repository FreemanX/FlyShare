package com.freeman.flyshare;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by freeman on 4/3/2016.
 */
public interface ReceiveSingleLocationCallBack {
    void onLocationReceive(boolean isSuccessful, LatLng location);
}