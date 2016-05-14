package com.freeman.flyshare;


import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.LinkedList;
import java.util.List;

import dji.sdk.FlightController.DJIFlightControllerDataType;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.MissionManager.DJIMissionManager;
import dji.sdk.MissionManager.DJIWaypoint;
import dji.sdk.MissionManager.DJIWaypointMission;


public class PanoMissionFragment extends MissionFragment {
    private RadioGroup panoMissionRadioGroup, panoMissionPointRadioGroup;
    private RadioButton pano180RadioButton, pano360RadioButton, pano720RadioButton, currentLocationRadioButton;
    private int panoChoise;
    //    private ProgressBar progressBar;
    private double mAltitude, mLat, mLng;
    private SeekBar altitudeSeekBar;
    private TextView altitudeTextView;

    public PanoMissionFragment() {
        // Required empty public constructor
    }

    @Override
    protected DJIMission initMission() {
        if (!Utils.checkGpsCoordinate(homeLat, homeLng)) {
            Utils.setResultToToast(getContext(), "No home point!!!");
            return null;
        }

        DJIWaypointMission testMission = new DJIWaypointMission();
        testMission.maxFlightSpeed = 14;
        testMission.autoFlightSpeed = 6;
        double distanceShift = 5;
        List<DJIWaypoint> waypointsList = new LinkedList<>();
        DJIWaypoint preparePoint = new DJIWaypoint(mLat, mLng, (float) (mAltitude + distanceShift * 4));
        waypointsList.add(preparePoint);
        double droneHeading = mFlightController.getCompass().getHeading();
        switch (panoChoise) {
            case 0:
                DJIWaypoint waypoint0 = new DJIWaypoint(mLat, mLng, (float) mAltitude);
                waypoint0.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, 0));
                for (int i = 0; i < 7; i++) {
                    int missionHeading = (int) (droneHeading + i * 30);
                    if (missionHeading > 180)
                        missionHeading = missionHeading - 360;
                    waypoint0.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.RotateAircraft, missionHeading));
                    waypoint0.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 0));
                }
                waypoint0.actionTimeoutInSeconds = 900;
                waypointsList.add(waypoint0);
                break;
            case 1:
                DJIWaypoint waypoint1 = new DJIWaypoint(mLat, mLng, (float) mAltitude);
                waypoint1.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, 0));
                DJIWaypoint waypoint2 = new DJIWaypoint(mLat, mLng - distanceShift * Utils.ONE_METER_OFFSET, (float) (mAltitude));
                for (int i = 0; i < 13; i++) {
                    int missionHeading = -180 + i * 30;
                    if (i < 7) {
                        waypoint1.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.RotateAircraft, missionHeading));
                        waypoint1.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 0));
                    } else {
                        waypoint2.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.RotateAircraft, missionHeading));
                        waypoint2.addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 0));
                    }
                }
                waypoint1.actionTimeoutInSeconds = 900;
                waypointsList.add(waypoint1);
                waypoint2.actionTimeoutInSeconds = 900;
                waypointsList.add(waypoint2);
                break;
            case 2:
                DJIWaypoint panoWaypoint[] = new DJIWaypoint[13];
                panoWaypoint[0] = new DJIWaypoint(mLat, mLng, (float) mAltitude);
                panoWaypoint[1] = new DJIWaypoint(mLat, mLng, (float) (mAltitude - 2 * distanceShift));
                panoWaypoint[2] = new DJIWaypoint(mLat, mLng + 1 * distanceShift * Utils.ONE_METER_OFFSET, (float) mAltitude);
                panoWaypoint[3] = new DJIWaypoint(mLat, mLng + 2 * distanceShift * Utils.ONE_METER_OFFSET, (float) mAltitude);
                panoWaypoint[4] = new DJIWaypoint(mLat - 1 * distanceShift * Utils.ONE_METER_OFFSET, mLng, (float) mAltitude);
                panoWaypoint[5] = new DJIWaypoint(mLat, mLng, (float) (mAltitude - 1 * distanceShift));
                panoWaypoint[6] = new DJIWaypoint(mLat - 2 * distanceShift * Utils.ONE_METER_OFFSET, mLng, (float) mAltitude);
                panoWaypoint[7] = new DJIWaypoint(mLat, mLng, (float) (mAltitude + 2 * distanceShift));
                panoWaypoint[8] = new DJIWaypoint(mLat, mLng - 1 * distanceShift * Utils.ONE_METER_OFFSET, (float) mAltitude);
                panoWaypoint[9] = new DJIWaypoint(mLat, mLng - 2 * distanceShift * Utils.ONE_METER_OFFSET, (float) mAltitude);
                panoWaypoint[10] = new DJIWaypoint(mLat + 1 * distanceShift * Utils.ONE_METER_OFFSET, mLng, (float) mAltitude);
                panoWaypoint[11] = new DJIWaypoint(mLat, mLng, (float) (mAltitude + 1 * distanceShift));
                panoWaypoint[12] = new DJIWaypoint(mLat + 2 * distanceShift * Utils.ONE_METER_OFFSET, mLng, (float) mAltitude);
                for (int i = 0; i < 13; i++) {
                    int missionHeading = i * 30 - 180;
                    panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.RotateAircraft, missionHeading));
                    panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, 0));
                    panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 0));
                    panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, -30));
                    panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 0));
                    panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, -60));
                    panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 0));
                    if (i % 2 == 1) {
                        panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.GimbalPitch, -90));
                        panoWaypoint[i].addAction(new DJIWaypoint.DJIWaypointAction(DJIWaypoint.DJIWaypointActionType.StartTakePhoto, 0));
                    }
                    panoWaypoint[i].actionTimeoutInSeconds = 900;
                    waypointsList.add(panoWaypoint[i]);
                }
                break;
            default:
                break;
        }

        testMission.addWaypoints(waypointsList);
        return testMission;
    }

    @Override
    protected int getFragmentViewResource() {
        return R.layout.fragment_pano_mission;
    }

    @Override
    protected void initMissionOngoingUIComponents() {
    }

    @Override
    protected void initConfigMissionUIComponents() {
        initSetAltitudeUI();
        this.panoMissionRadioGroup = (RadioGroup) mView.findViewById(R.id.pano_mission_radio_group);
        this.pano180RadioButton = (RadioButton) mView.findViewById(R.id.pano180_radioButton);
        pano180RadioButton.setChecked(true);
        panoMissionRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.pano180_radioButton) {
                    panoChoise = 0;
                } else if (checkedId == R.id.pano360_radioButton) {
                    panoChoise = 1;
                } else if (checkedId == R.id.pano720_radioButton) {
                    panoChoise = 2;
                }
            }
        });

        this.panoMissionPointRadioGroup = (RadioGroup) mView.findViewById(R.id.pano_point_select_radio_group);
        this.currentLocationRadioButton = (RadioButton) mView.findViewById(R.id.current_location_radioButton);
        panoMissionPointRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.current_location_radioButton) {
                    if (mFlightController != null) {
                        DJIFlightControllerDataType.DJIFlightControllerCurrentState currentState = mFlightController.getCurrentState();
                        updatePanoPoint(currentState.getAircraftLocation().getLatitude(), currentState.getAircraftLocation().getLongitude());
                    } else
                        Utils.setResultToToast(getContext(), "FlightController is null");
                } else if (checkedId == R.id.set_on_map_radioButton) {
                    missionRequestMapHandler.sendDropSingleMarkerRequestToMap(new LatLng(mLat, mLng));
                    missionRequestMapHandler.sendAddSingleMarkerRequestToMap(new ReceiveSingleLocationCallBack() {
                        @Override
                        public void onLocationReceive(boolean isSuccessful, LatLng location) {
                            getActivity().getSupportFragmentManager().beginTransaction().show(getThis()).commit();
                            if (isSuccessful) {
                                updatePanoPoint(location.latitude, location.longitude);
                            } else {
                                Utils.setResultToToast(getContext(), "Add hop point on map failed!");
                            }
                        }
                    });
                    getActivity().getSupportFragmentManager().beginTransaction().hide(getThis()).commit();
                }
            }
        });

    }

    protected PanoMissionFragment getThis() {
        return this;
    }

    private void initSetAltitudeUI() {
        altitudeTextView = (TextView) mView.findViewById(R.id.altitude_textView);
        altitudeSeekBar = (SeekBar) mView.findViewById(R.id.altitude_seekBar);
        altitudeSeekBar.setMax(315);
        altitudeSeekBar.setProgress((int) Math.round(mAltitude));
        altitudeTextView.setText(Double.toString(mAltitude) + " m");
        altitudeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mAltitude = progress + 5;
                altitudeTextView.setText(Double.toString(mAltitude) + " m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void initMissionVariables() {
        panoChoise = 0;
        updatePanoPoint(homeLat, homeLng);
        mAltitude = 100;
    }

    // TODO: Rename and change types and number of parameters
    public static PanoMissionFragment newInstance() {
        PanoMissionFragment fragment = new PanoMissionFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    private void updatePanoPoint(double lat, double lng) {
        if (!DJIMissionManager.checkValidGPSCoordinate(lat, lng)) {
            Utils.setResultToToast(getContext(), "Invalid location(" + Double.toString(lat) + "," + Double.toString(lng) + ")");
            return;
        }
        this.mLat = lat;
        this.mLng = lng;
        missionRequestMapHandler.sendUpdateSingleMakerRequestToMap(new LatLng(mLat, mLng));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus djiMissionProgressStatus) {

    }

    @Override
    protected void onUploadClickOperationSuccess() {

        missionRequestMapHandler.sendDropSingleMarkerRequestToMap(new LatLng(mLat, mLng));
        super.onUploadClickOperationSuccess();
    }


    @Override
    protected void onStopClickOperationSuccess() {
        fragmentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                currentLocationRadioButton.setChecked(true);
            }
        });

        super.onStopClickOperationSuccess();
    }

    @Override
    protected void finishMission() {
        missionRequestMapHandler.sendCleanMarkersToMap();
        super.finishMission();
    }

}
