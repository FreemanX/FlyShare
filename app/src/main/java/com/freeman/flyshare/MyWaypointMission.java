package com.freeman.flyshare;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.LinkedList;

import dji.sdk.MissionManager.DJIWaypointMission;

/**
 * Created by freeman on 4/7/2016.
 */
public class MyWaypointMission implements Serializable {
    public String missionName;
    public String missionDescription;
    private LinkedList<MyWaypoint> missionPointLinkedList;

    private float autoFlightSpeed = 10;
    private DJIWaypointMission.DJIWaypointMissionFinishedAction finishedAction = DJIWaypointMission.DJIWaypointMissionFinishedAction.NoAction;
    private DJIWaypointMission.DJIWaypointMissionFlightPathMode flightPathMode = DJIWaypointMission.DJIWaypointMissionFlightPathMode.Normal;
    private DJIWaypointMission.DJIWaypointMissionGotoWaypointMode gotoWaypointMode = DJIWaypointMission.DJIWaypointMissionGotoWaypointMode.Safely;
    private DJIWaypointMission.DJIWaypointMissionHeadingMode headingMode = DJIWaypointMission.DJIWaypointMissionHeadingMode.ControlByRemoteController;
    private float maxFlightSpeed = 15f; //range [2,15]
    private boolean needExitMissionOnRCSignalLost = false; //warn the user about this function
    private boolean needRotateGimbalPitch = false;
    private int repeatNum = 1; //[0, 255]
    private transient DJIWaypointMission mWaypointMission;

    private boolean isAllSameAltitude = false;
    private float sameAltitude = 120f;


    public MyWaypointMission(String MissionName, String MissionDescription) {
        this.missionPointLinkedList = new LinkedList<>();
        this.missionName = MissionName;
        this.missionDescription = MissionDescription;
    }

    public SavedWaypointMission prepareSavedMission() {
        SavedWaypointMission savedWaypointMission
                = new SavedWaypointMission(this.missionName, this.missionDescription,
                this.autoFlightSpeed, this.finishedAction, this.flightPathMode, this.gotoWaypointMode, this.headingMode,
                this.maxFlightSpeed, this.needExitMissionOnRCSignalLost, this.needRotateGimbalPitch, this.repeatNum, this.isAllSameAltitude,
                this.sameAltitude);
        for (MyWaypoint myWaypoint : missionPointLinkedList) {
            savedWaypointMission.missionPointAltLinkedList.add(myWaypoint.getAltitude());
            savedWaypointMission.missionPointLatLinkedList.add(myWaypoint.getLocation().latitude);
            savedWaypointMission.missionPointLngLinkedList.add(myWaypoint.getLocation().longitude);
            savedWaypointMission.gimbalPitchLinkedList.add(myWaypoint.getGimbalPitch());
            savedWaypointMission.headingLinkedList.add(myWaypoint.getHeading());
        }
        return savedWaypointMission;
    }

    public static MyWaypointMission restoreWayPointMission(SavedWaypointMission savedWaypointMission) {
        MyWaypointMission restoredMission = new MyWaypointMission(savedWaypointMission.missionName, savedWaypointMission.missionDescription);
        restoredMission.setAutoFlightSpeed(savedWaypointMission.autoFlightSpeed);
        restoredMission.setFinishedAction(savedWaypointMission.finishedAction);
        restoredMission.setFlightPathMode(savedWaypointMission.flightPathMode);
        restoredMission.setGotoWaypointMode(savedWaypointMission.gotoWaypointMode);
        restoredMission.setHeadingMode(savedWaypointMission.headingMode);
        restoredMission.setMaxFlightSpeed(savedWaypointMission.maxFlightSpeed);
        restoredMission.setNeedExitMissionOnRCSignalLost(savedWaypointMission.needExitMissionOnRCSignalLost);
        restoredMission.setNeedRotateGimbalPitch(savedWaypointMission.needRotateGimbalPitch);
        restoredMission.setRepeatNum(savedWaypointMission.repeatNum);
        restoredMission.setIsAllSameAltitude(savedWaypointMission.isAllSameAltitude);
        restoredMission.setSameAltitude(savedWaypointMission.sameAltitude);
        int i;
        for (i = 0; i < savedWaypointMission.headingLinkedList.size(); i++) {
            LatLng position = new LatLng(savedWaypointMission.missionPointLatLinkedList.get(i), savedWaypointMission.missionPointLngLinkedList.get(i));
            MyWaypoint waypoint = new MyWaypoint(i, position);
            waypoint.setGimbalPitch(savedWaypointMission.gimbalPitchLinkedList.get(i));
            waypoint.setHeading(savedWaypointMission.headingLinkedList.get(i));
            waypoint.setAltitude(savedWaypointMission.missionPointAltLinkedList.get(i));
            restoredMission.addWaypoint(waypoint);
            Log.e("MyWaypointMission", "Waypoint(" + Double.toString(position.latitude) + Double.toString(position.longitude) + Float.toString(savedWaypointMission.missionPointAltLinkedList.get(i)) + "):");
        }
        Log.e("MyWaypointMission", "Mission " + savedWaypointMission.missionName + " loaded: " + Integer.toString(i) + " waypoints, ");
        return restoredMission;
    }

    public DJIWaypointMission initDJIWaypointMission() {
        mWaypointMission = new DJIWaypointMission();
        mWaypointMission.autoFlightSpeed = autoFlightSpeed;
        mWaypointMission.finishedAction = this.finishedAction;
        mWaypointMission.flightPathMode = this.flightPathMode;
        mWaypointMission.goFirstWaypointMode = this.gotoWaypointMode;
        mWaypointMission.headingMode = this.headingMode;
        mWaypointMission.maxFlightSpeed = this.maxFlightSpeed;
        mWaypointMission.needExitMissionOnRCSignalLost = this.needExitMissionOnRCSignalLost;
        mWaypointMission.needRotateGimbalPitch = this.needRotateGimbalPitch;
        mWaypointMission.repeatNum = this.repeatNum;
        mWaypointMission.flightPathMode = DJIWaypointMission.DJIWaypointMissionFlightPathMode.Curved;
        for (MyWaypoint myWaypoint : missionPointLinkedList) {
            if (isAllSameAltitude)
                myWaypoint.setAltitude(sameAltitude);
            mWaypointMission.addWaypoint(myWaypoint.initDjiWaypoint());
        }

        return mWaypointMission;
    }

    public LinkedList<MyWaypoint> getMissionPoints() {
        Log.e("MyWaypointMission", "Mission " + missionName + " loaded: " + Integer.toString(missionPointLinkedList.size()) + " waypoints");
        return this.missionPointLinkedList;
    }

    public void updateLocations(LinkedList<MyWaypoint> newMissionPoints) {
        this.missionPointLinkedList = newMissionPoints;
    }

    public float getFirstAltitude() {
        return this.missionPointLinkedList.getFirst().getAltitude();
    }

    public boolean addWaypoint(MyWaypoint waypoint) {
        if (this.missionPointLinkedList.size() > 98)
            return false;
        this.missionPointLinkedList.add(waypoint);
        return true;
    }

    public void deleteWaypoint(int index) {
        this.missionPointLinkedList.remove(index);
    }

    public LinkedList changeLocationForAll(LatLng newFirstPosition) {
        assignNewPositions(newFirstPosition);
        return missionPointLinkedList;
    }

    private void clearAllPositions() {
        if (this.missionPointLinkedList.size() > 0) {
            LatLng oldFirstPoint = missionPointLinkedList.getFirst().getLocation();
            for (MyWaypoint myWaypoint : missionPointLinkedList) {
                LatLng oldPosition = myWaypoint.getLocation();
                LatLng newPosition = new LatLng(oldPosition.latitude - oldFirstPoint.latitude, oldPosition.longitude - oldFirstPoint.latitude);
                myWaypoint.setLocation(newPosition);
            }
        }
    }

    private void assignNewPositions(LatLng newFirstPosition) {
        if (this.missionPointLinkedList.size() > 0) {
            LatLng oldFirstPoint = missionPointLinkedList.getFirst().getLocation();
            for (MyWaypoint myWaypoint : missionPointLinkedList) {
                LatLng oldPosition = myWaypoint.getLocation();
                LatLng newPosition = new LatLng(
                        oldPosition.latitude - oldFirstPoint.latitude + newFirstPosition.latitude,
                        oldPosition.longitude - oldFirstPoint.latitude + newFirstPosition.latitude);
                myWaypoint.setLocation(newPosition);
            }
        }
    }

    /**
     * ----------------------------Getters and setters------------------------------
     **/
    public float getAutoFlightSpeed() {
        return autoFlightSpeed;
    }

    public void setAutoFlightSpeed(float autoFlightSpeed) {
        this.autoFlightSpeed = autoFlightSpeed;
    }

    public DJIWaypointMission.DJIWaypointMissionFinishedAction getFinishedAction() {
        return finishedAction;
    }

    public void setFinishedAction(DJIWaypointMission.DJIWaypointMissionFinishedAction finishedAction) {
        this.finishedAction = finishedAction;
    }

    public DJIWaypointMission.DJIWaypointMissionFlightPathMode getFlightPathMode() {
        return flightPathMode;
    }

    public void setFlightPathMode(DJIWaypointMission.DJIWaypointMissionFlightPathMode flightPathMode) {
        this.flightPathMode = flightPathMode;
    }

    public DJIWaypointMission.DJIWaypointMissionGotoWaypointMode getGotoWaypointMode() {
        return gotoWaypointMode;
    }

    public void setGotoWaypointMode(DJIWaypointMission.DJIWaypointMissionGotoWaypointMode gotoWaypointMode) {
        this.gotoWaypointMode = gotoWaypointMode;
    }

    public DJIWaypointMission.DJIWaypointMissionHeadingMode getHeadingMode() {
        return headingMode;
    }

    public void setHeadingMode(DJIWaypointMission.DJIWaypointMissionHeadingMode headingMode) {
        this.headingMode = headingMode;
    }

    public float getMaxFlightSpeed() {
        return maxFlightSpeed;
    }

    public void setMaxFlightSpeed(float maxFlightSpeed) {
        this.maxFlightSpeed = maxFlightSpeed;
    }

    public boolean isNeedExitMissionOnRCSignalLost() {
        return needExitMissionOnRCSignalLost;
    }

    public void setNeedExitMissionOnRCSignalLost(boolean needExitMissionOnRCSignalLost) {
        this.needExitMissionOnRCSignalLost = needExitMissionOnRCSignalLost;
    }

    public boolean isNeedRotateGimbalPitch() {
        return needRotateGimbalPitch;
    }

    public void setNeedRotateGimbalPitch(boolean needRotateGimbalPitch) {
        this.needRotateGimbalPitch = needRotateGimbalPitch;
    }

    public int getRepeatNum() {
        return repeatNum;
    }

    public void setRepeatNum(int repeatNum) {
        this.repeatNum = repeatNum;
    }

    public boolean isAllSameAltitude() {
        return isAllSameAltitude;
    }

    public void setIsAllSameAltitude(boolean isAllSameAltitude) {
        this.isAllSameAltitude = isAllSameAltitude;
    }

    public float getSameAltitude() {
        return sameAltitude;
    }

    public void setSameAltitude(float sameAltitude) {
        this.sameAltitude = sameAltitude;
    }
}
