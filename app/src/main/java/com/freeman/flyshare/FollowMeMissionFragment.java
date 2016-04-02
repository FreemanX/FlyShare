package com.freeman.flyshare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import dji.sdk.MissionManager.DJIFollowMeMission;
import dji.sdk.MissionManager.DJIMission;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIError;


public final class FollowMeMissionFragment extends MissionFragment {
    private OnFollowMeMissionFragmentInteractionListener mListener;
    private RadioGroup headingRG;
    private RadioButton headingObjectRB, controlledByRC_RB;
    private DJIFollowMeMission.DJIFollowMeHeading heading = DJIFollowMeMission.DJIFollowMeHeading.ControlledByRemoteController;
    UpdateFollowingObjectLocationThread updateFollowingObjectLocationThread = null;

    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus djiMissionProgressStatus) {

    }

    @Override
    protected void startUpdateFollowedObjectLocation() {
        super.startUpdateFollowedObjectLocation();
        if (updateFollowingObjectLocationThread != null)
            updateFollowingObjectLocationThread.start();
    }

    @Override
    protected void stopUpdateFollowedObjectLocation() {
        super.stopUpdateFollowedObjectLocation();
        if (updateFollowingObjectLocationThread != null) {
            updateFollowingObjectLocationThread.stopRunning();
            updateFollowingObjectLocationThread = null;
        }
    }

    @Override
    protected void setPauseUpdateFollowedObjectLocation(boolean isPaused) {
        super.setPauseUpdateFollowedObjectLocation(isPaused);
        if (updateFollowingObjectLocationThread != null)
            updateFollowingObjectLocationThread.setIsPause(isPaused);
    }

    static class UpdateFollowingObjectLocationThread extends Thread {
        private DJIMission mDJIMission = null;
        private double mHomeLatitude = 181;
        private double mHomeLongitude = 181;

        private boolean mIsRunning = false;
        private boolean mIsPausing = false;

        float clock = 0;
        float radius = 6378137;
        private double tarPosLat;
        private double tarPosLon;
        private double tgtPosX;
        private double tgtPosY;

        public UpdateFollowingObjectLocationThread(DJIMission mission, double homeLat, double homeLng) {
            super();
            mDJIMission = mission;
            this.mHomeLatitude = homeLat;
            this.mHomeLongitude = homeLng;
        }

        @Override
        public void run() {
            if (
                    mDJIMission == null ||
                            mDJIMission.getMissionType() != DJIMission.DJIMissionType.Followme ||
                            !Utils.checkGpsCoordinate(mHomeLatitude, mHomeLongitude)
                    )
                return;
            mIsRunning = true;


            while (mIsRunning && clock < 1800) {
                tgtPosX = 5 * Math.sin(clock / 10 * 0.5);
                tgtPosY = 5 * Math.cos(clock / 10 * 0.5);
                tarPosLat = Utils.Radian(mHomeLatitude) + tgtPosX / radius;
                tarPosLon = Utils.Radian(mHomeLongitude) + tgtPosY / radius / Math.cos(Utils.Radian(mHomeLatitude));
                DJIFollowMeMission.updateFollowMeCoordinate(
                        Utils.Degree(tarPosLat), Utils.Degree(tarPosLon), new DJIBaseComponent.DJICompletionCallback() {

                            @Override
                            public void onResult(DJIError error) {
                            }
                        });
                if (!mIsPausing)
                    clock++;
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopRunning() {
            mIsRunning = false;
        }

        public void setIsPause(boolean isPaused) {
            mIsPausing = isPaused;
        }
    }

    public FollowMeMissionFragment() {
        // Required empty public constructor
    }

    @Override
    protected DJIMission initMission() {
        updateHomeLocation();
        if (!Utils.checkGpsCoordinate(homeLat, homeLng)) {
            Utils.setResultToToast(getContext(), "No home point!");
            return null;
        }
        DJIFollowMeMission djiFollowMeMission = new DJIFollowMeMission(homeLat, homeLng);
        djiFollowMeMission.heading = this.heading;
        return djiFollowMeMission;
    }

    @Override
    protected int getFragmentViewResource() {
        return R.layout.fragment_follow_me_mission;
    }

    @Override
    protected void initMissionOngoingUIComponents() {

    }

    @Override
    protected void initConfigMissionUIComponents() {
        this.headingRG = (RadioGroup) mView.findViewById(R.id.follow_me_heading);
        this.headingObjectRB = (RadioButton) mView.findViewById(R.id.headingObject);
        this.controlledByRC_RB = (RadioButton) mView.findViewById(R.id.headingFree);
        this.controlledByRC_RB.setChecked(true);
        headingRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.headingFree) {
                    heading = DJIFollowMeMission.DJIFollowMeHeading.ControlledByRemoteController;
                } else if (checkedId == R.id.headingObject) {
                    heading = DJIFollowMeMission.DJIFollowMeHeading.TowardFollowPosition;
                }
            }
        });
    }

    @Override
    protected void initMissionVariables() {

    }


    public static FollowMeMissionFragment newInstance() {
        FollowMeMissionFragment followMeMissionFragment = new FollowMeMissionFragment();
        return followMeMissionFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFollowMeMissionFragmentInteractionListener) {
            mListener = (OnFollowMeMissionFragmentInteractionListener) context;
        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFollowMeMissionFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFollowMeMissionFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
