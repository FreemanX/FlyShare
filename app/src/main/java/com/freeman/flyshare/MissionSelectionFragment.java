package com.freeman.flyshare;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class MissionSelectionFragment extends Fragment implements View.OnClickListener {

    Button followMeButton, hotPointButton, panoramaButton, yourMissionButton;

    private OnFragmentInteractionListener mListener;

    public MissionSelectionFragment() {
        // Required empty public constructor
    }

    public static MissionSelectionFragment newInstance() {
        MissionSelectionFragment fragment = new MissionSelectionFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mission_selection, container, false);

        followMeButton = (Button) view.findViewById(R.id.follow_me_button);
        hotPointButton = (Button) view.findViewById(R.id.hot_point_button);
        panoramaButton = (Button) view.findViewById(R.id.panorama_button);
        yourMissionButton = (Button) view.findViewById(R.id.waypoint_button);
        followMeButton.setOnClickListener(this);
        hotPointButton.setOnClickListener(this);
        panoramaButton.setOnClickListener(this);
        yourMissionButton.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.follow_me_button:
                mListener.onMissionTypeSelected(1);
                break;
            case R.id.hot_point_button:
                mListener.onMissionTypeSelected(2);
                break;
            case R.id.panorama_button:
                mListener.onMissionTypeSelected(3);
                break;
            case R.id.waypoint_button:
                mListener.onMissionTypeSelected(4);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
//            Log.e("MissionSelection", "OnFragmentInteractListener not implemented! ");
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onMissionTypeSelected(int i);
    }
}
