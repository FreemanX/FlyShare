package com.freeman.flyshare;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by freeman on 4/3/2016.
 */
public interface MissionRequestMapHandler {
    void sendCancelSetMarkerRequestToMap();

    void sendDropSingleMarkerRequestToMap(LatLng markLocation);

    void sendDropMultipleMarkersRequestToMap(LatLng[] markLocations);

    void sendUpdateMultipleMarkerRequestToMap(LatLng[] newLocations);

    void sendUpdateSingleMakerRequestToMap(LatLng newLocation);

    void sendAddSingleMarkerRequestToMap(ReceiveSingleLocationCallBack receiveLocationCallBack);

    void sendAddMultipleMarkersRequestToMap(ReceiveMultipleLocationsCallBack receiveLocationsCallBack);

    void sendAlterMarkersRequestToMap(LatLng[] markerLocations, ReceiveMultipleLocationsCallBack receiveLocationsCallBack);

    void sendCleanMarkersToMap();
}
