package com.freeman.flyshare;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;

/**
 * Created by freeman on 4/3/2016.
 */
public interface MissionRequestMapHandler {
    void sendCancelSetMarkerRequestToMap();

    void sendDropSingleMarkerRequestToMap(LatLng markLocation);

    void sendDropMultipleMarkersRequestToMap(LinkedList<MyWaypoint> markLocations);

    void sendUpdateMultipleMarkerRequestToMap(LinkedList<MyWaypoint> newLocations);

    void sendUpdateSingleMakerRequestToMap(LatLng newLocation);

    void sendAddSingleMarkerRequestToMap(ReceiveSingleLocationCallBack receiveLocationCallBack);

    void sendAddMultipleMarkersRequestToMap(ReceiveMultipleLocationsCallBack receiveLocationsCallBack);

    void sendAlterMarkersRequestToMap(LinkedList<MyWaypoint> markerLocations, ReceiveMultipleLocationsCallBack receiveLocationsCallBack);

    void sendCleanMarkersToMap();
}
