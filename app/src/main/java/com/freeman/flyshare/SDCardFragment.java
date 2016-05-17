package com.freeman.flyshare;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import dji.sdk.Camera.DJICamera;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIError;


public class SDCardFragment extends Fragment {

    private Activity fragmentActivity;
    private View mView;
    private TextView statusTextView, emptySpaceTextView, spaceTextView, numOfPhotoTextView, timeOfViewTextView;
    private Button formatSDCardButton;

    public SDCardFragment() {
        // Required empty public constructor
    }

    public static SDCardFragment newInstance() {
        SDCardFragment fragment = new SDCardFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        fragmentActivity = getActivity();
        mView = inflater.inflate(R.layout.fragment_sdcard, container, false);
        numOfPhotoTextView = (TextView) mView.findViewById(R.id.numOfPhotos_textView);
        timeOfViewTextView = (TextView) mView.findViewById(R.id.timeOfVideo_textView);
        statusTextView = (TextView) mView.findViewById(R.id.sd_card_status_textView);
        emptySpaceTextView = (TextView) mView.findViewById(R.id.sd_card_empty_space_textView);
        spaceTextView = (TextView) mView.findViewById(R.id.sd_card_space_textView);
        formatSDCardButton = (Button) mView.findViewById(R.id.format_sd_card_button);
        formatSDCardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(fragmentActivity)
                        .setTitle("Are you sure to format the SD card?")
                        .setPositiveButton("Format", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (isReady()) {
                                    FlyShareApplication.getProductInstance().getCamera()
                                            .formatSDCard(new DJIBaseComponent.DJICompletionCallback() {
                                                @Override
                                                public void onResult(DJIError djiError) {
                                                    if (djiError != null) {
                                                        Utils.setResultToToast(fragmentActivity, "Format failed: " + djiError.getDescription());
                                                    } else
                                                        Utils.setResultToToast(fragmentActivity, "SD card formatted!");
                                                }
                                            });
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        }).create().show();
            }
        });
        if (isReady()) {
            FlyShareApplication.getProductInstance().getCamera().setDJIUpdateCameraSDCardStateCallBack(new DJICamera.CameraUpdatedSDCardStateCallback() {
                @Override
                public void onResult(final DJICamera.CameraSDCardState cameraSDCardState) {
                    fragmentActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusTextView.setTextColor(Color.RED);
                            if (!cameraSDCardState.isInserted()) {
                                statusTextView.setText("No SD card inserted!");
                                return;
                            }
                            numOfPhotoTextView.setText(Long.toString(cameraSDCardState.getAvailableCaptureCount()) + " photos");
                            timeOfViewTextView.setText(Integer.toString(cameraSDCardState.getAvailableRecordingTime()/60) + " minutes video");
                            emptySpaceTextView.setText(Integer.toString(cameraSDCardState.getRemainingSpaceInMegaBytes()));
                            spaceTextView.setText(Integer.toString(cameraSDCardState.getTotalSpaceInMegaBytes()));
                            if (cameraSDCardState.hasError()) {
                                statusTextView.setText("SD card has error");
                            } else if (cameraSDCardState.isFull()) {
                                statusTextView.setText("SD card is full");
                            } else if (cameraSDCardState.isInvalidFormat()) {
                                statusTextView.setText("Invalid SD card format");
                            } else if (cameraSDCardState.isReadOnly()) {
                                statusTextView.setText("The SD card is read only");
                            } else if (cameraSDCardState.isFormatting())
                            {
                                statusTextView.setText("Formatting SD card...");
                            }
                            else {
                                statusTextView.setTextColor(Color.GREEN);
                                statusTextView.setText("Normal");
                            }
                        }
                    });
                }
            });
        }
        return mView;
    }

    public static boolean isReady() {
        return FlyShareApplication.getProductInstance() != null
                && FlyShareApplication.getProductInstance().getCamera() != null
                && FlyShareApplication.getProductInstance().getCamera().isConnected();
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
