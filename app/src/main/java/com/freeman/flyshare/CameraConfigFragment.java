package com.freeman.flyshare;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

public class CameraConfigFragment extends Fragment {

    RadioGroup cameraModeRG;
    TextView ISOValueTV, shutterValueTV, EVValueTV;
    SeekBar ISOSeekBar, shutterSeekBar;
    Button addEVButton, minusEVButton;
    int defaultTextColor;

    public CameraConfigFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static CameraConfigFragment getCameraConfigFragment() {
        CameraConfigFragment fragment = new CameraConfigFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
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
        View cameraConfigView = inflater.inflate(R.layout.fragment_camera_config, container, false);
        this.cameraModeRG = (RadioGroup) cameraConfigView.findViewById(R.id.camera_mode_radioGroup);
        // Init ISO settings
        this.ISOValueTV = (TextView) cameraConfigView.findViewById(R.id.iso_value_TextView);
        this.ISOSeekBar = (SeekBar) cameraConfigView.findViewById(R.id.ISO_seekBar);
        //TODO init the seekbar value from DJI camera, convert the seekbar progress to ISO
        ISOValueTV.setText(ISOSeekBar.getProgress() + "/" + ISOSeekBar.getMax());
        defaultTextColor = ISOValueTV.getCurrentTextColor();
        ISOSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                ISOValueTV.setText(progress + "/" + ISOSeekBar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ISOValueTV.setTextColor(Color.RED);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ISOValueTV.setTextColor(defaultTextColor);
            }
        });


        // Init shutter speed settings
        this.shutterValueTV = (TextView) cameraConfigView.findViewById(R.id.shutter_value_TextView);
        this.shutterSeekBar = (SeekBar) cameraConfigView.findViewById(R.id.shutter_seekBar);


        // Init EV settings
        this.EVValueTV = (TextView) cameraConfigView.findViewById(R.id.EV_value_TextView);
        this.addEVButton = (Button) cameraConfigView.findViewById(R.id.EV_add_btn);
        this.minusEVButton = (Button) cameraConfigView.findViewById(R.id.EV_minus_btn);


        return cameraConfigView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
