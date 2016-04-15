package com.freeman.flyshare;

import java.io.Serializable;
import java.util.LinkedList;

import dji.sdk.MissionManager.DJIWaypointMission;

/**
 * Created by freeman on 4/15/2016.
 */
public class SavedWaypointMission implements Serializable {
    public String missionName;
    public String missionDescription;

    public LinkedList<Float> missionPointAltLinkedList = new LinkedList<>();
    public LinkedList<Double> missionPointLatLinkedList = new LinkedList<>();
    public LinkedList<Double> missionPointLngLinkedList = new LinkedList<>();
    public LinkedList<Short> headingLinkedList = new LinkedList<>();
    public LinkedList<Short> gimbalPitchLinkedList = new LinkedList<>();

    public float autoFlightSpeed;
    public DJIWaypointMission.DJIWaypointMissionFinishedAction finishedAction;
    public DJIWaypointMission.DJIWaypointMissionFlightPathMode flightPathMode;
    public DJIWaypointMission.DJIWaypointMissionGotoWaypointMode gotoWaypointMode;
    public DJIWaypointMission.DJIWaypointMissionHeadingMode headingMode;
    public float maxFlightSpeed;
    public boolean needExitMissionOnRCSignalLost;
    public boolean needRotateGimbalPitch;
    public int repeatNum;

    public boolean isAllSameAltitude;
    public float sameAltitude;

    public SavedWaypointMission(String missionName, String missionDescription, float autoFlightSpeed,
                                DJIWaypointMission.DJIWaypointMissionFinishedAction finishedAction,
                                DJIWaypointMission.DJIWaypointMissionFlightPathMode flightPathMode,
                                DJIWaypointMission.DJIWaypointMissionGotoWaypointMode gotoWaypointMode,
                                DJIWaypointMission.DJIWaypointMissionHeadingMode headingMode, float maxFlightSpeed,
                                boolean needExitMissionOnRCSignalLost, boolean needRotateGimbalPitch, int repeatNum,
                                boolean isAllSameAltitude, float sameAltitude) {
        this.missionName = missionName;
        this.missionDescription = missionDescription;
        this.autoFlightSpeed = autoFlightSpeed;
        this.finishedAction = finishedAction;
        this.flightPathMode = flightPathMode;
        this.gotoWaypointMode = gotoWaypointMode;
        this.headingMode = headingMode;
        this.maxFlightSpeed = maxFlightSpeed;
        this.needExitMissionOnRCSignalLost = needExitMissionOnRCSignalLost;
        this.needRotateGimbalPitch = needRotateGimbalPitch;
        this.repeatNum = repeatNum;
        this.isAllSameAltitude = isAllSameAltitude;
        this.sameAltitude = sameAltitude;
    }
}
