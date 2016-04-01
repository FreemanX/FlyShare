package com.freeman.flyshare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import dji.sdk.MissionManager.DJIFollowMeMission;
import dji.sdk.MissionManager.DJIMission;


public final class FollowMeMissionFragment extends MissionFragment {
    private OnFollowMeMissionFragmentInteractionListener mListener;
    private RadioGroup headingRG;
    private RadioButton headingObjectRB, controlledByRC_RB;
    private DJIFollowMeMission.DJIFollowMeHeading heading = DJIFollowMeMission.DJIFollowMeHeading.ControlledByRemoteController;

    @Override
    public void missionProgressStatus(DJIMission.DJIMissionProgressStatus djiMissionProgressStatus) {

    }

    public class updateFollowingObjectLocationThread extends Thread {

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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFollowMeMissionFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
