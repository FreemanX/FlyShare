package com.freeman.flyshare;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.LinkedList;

import dji.sdk.MissionManager.DJIWaypoint;

/**
 * Created by freeman on 4/7/2016.
 */
public class MyWaypoint {
    final static float DEFAULT_ALTITUDE = 120f;
    final static short DEFAULT_HEADING = 0;
    final static short DEFAULT_GIMBALPITCH = 0;
    final static float DEFAULT_CORNERRADIUSINMETERS = 0.5f;

    private int id;
    private MarkerOptions waypointMarkerOptions;
    private DJIWaypoint djiWaypoint;

    private LatLng location;
    private float altitude;
    private LinkedList<DJIWaypoint.DJIWaypointAction> actionLinkedList;
    private short gimbalPitch; // used when DJIWaypointMission.rotateGimbalPitch is true
    private short heading; // only work with headingMode is set to DJIWaypointMissionHeadingUsingWaypointHeading

    public MyWaypoint(int ID, LatLng Location) {
        this.id = ID;
        this.location = Location;
        this.altitude = DEFAULT_ALTITUDE;
        this.waypointMarkerOptions = new MarkerOptions().position(location).title("Point " + Integer.toString(id));
        this.actionLinkedList = new LinkedList<>();
        this.heading = DEFAULT_HEADING;
        this.gimbalPitch = DEFAULT_GIMBALPITCH;
    }

    public void setAltitude(float inAltitude) {
        this.altitude = inAltitude;
    }

    public float getAltitude() {
        return this.altitude;
    }

    public short getGimbalPitch() {
        return gimbalPitch;
    }

    public void setGimbalPitch(short gimbalPitch) {
        this.gimbalPitch = gimbalPitch;
    }

    public short getHeading() {
        return heading;
    }

    public void setHeading(short heading) {
        this.heading = heading;
    }

    public int getId() {
        return id;
    }

    public LinkedList<DJIWaypoint.DJIWaypointAction> getActionLinkedList() {
        return actionLinkedList;
    }

    public boolean addAction(DJIWaypoint.DJIWaypointAction action) {
        if (this.actionLinkedList.size() > 14) {
            return false;
        }
        this.actionLinkedList.add(action);
        return true;
    }

    public void removeAction(int index) {
        this.actionLinkedList.remove(index);
    }

    public void removeAllActions() {
        this.actionLinkedList = new LinkedList<>();
    }

    public MarkerOptions getWaypointMarkerOptions() {
        return waypointMarkerOptions;
    }

    public DJIWaypoint initDjiWaypoint() {
        this.djiWaypoint = new DJIWaypoint(location.latitude, location.longitude, altitude);
        if (actionLinkedList.size() > 0) {
            for (int i = 0; i < actionLinkedList.size(); i++)
                this.djiWaypoint.addAction(actionLinkedList.get(i));
        }
        this.djiWaypoint.heading = this.heading;
        this.djiWaypoint.gimbalPitch = this.gimbalPitch;
        this.djiWaypoint.cornerRadiusInMeters = DEFAULT_CORNERRADIUSINMETERS;
        return djiWaypoint;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setWaypointMarkerOptions(MarkerOptions waypointMarkerOptions) {
        this.waypointMarkerOptions = waypointMarkerOptions;
    }

    public void setDjiWaypoint(DJIWaypoint djiWaypoint) {
        this.djiWaypoint = djiWaypoint;
    }
}
