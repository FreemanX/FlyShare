package com.freeman.flyshare;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import dji.sdk.Camera.DJICamera;
import dji.sdk.Camera.DJICameraParameters;
import dji.sdk.Camera.DJICameraSettingsDef;
import dji.sdk.Camera.DJICameraSettingsDef.CameraPhotoIntervalParam;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;


public class CameraSettingFragment extends Fragment {

    final String[] PHOTO_FORMAT = {"JPEG", "RAW", "RAW + JPEG", "TIFF(14 Bit)"};

    private DJICameraParameters.VideoResolutionFps[] videoResolutionFpses;
    private DJIBaseProduct mProduct;
    private DJICamera mCamera;

    private View mView;
    private LinearLayout photoSettingLinearLayout, videoSettingLinearLayout, intervalLinearLayout, AEBLinearLayout;
    private RadioGroup cameraModeRadioGroup;
    private RadioButton singleRadioButton, HDRRadioButton, intervalRadioButton, AEBRadioButton;
    private TextView intervalTextView, AEBTextView;
    private SeekBar intervalSeekBar, AEBSeekBar;
    private Spinner photoFormatSpinner;

    private FPVActivity fpvActivity;

    private void initSettingVariables() {
        isCameraReady();
        videoResolutionFpses = DJICameraParameters.getInstance().supportedCameraVideoResolutionAndFrameRateRange();
        fpvActivity = (FPVActivity) getActivity();
    }

    public CameraSettingFragment() {
        // Required empty public constructor
    }

    public static CameraSettingFragment newInstance() {
        CameraSettingFragment fragment = new CameraSettingFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initSettingVariables();
        mView = inflater.inflate(R.layout.fragment_camera_setting, container, false);

        photoSettingLinearLayout = (LinearLayout) mView.findViewById(R.id.photoSettingsLinearLayout);
        cameraModeRadioGroup = (RadioGroup) mView.findViewById(R.id.camera_mode_radioGroup);
        cameraModeRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                intervalLinearLayout.setVisibility(View.GONE);
                AEBLinearLayout.setVisibility(View.GONE);
                switch (checkedId) {
                    case R.id.single_mode_radioButton:
                        fpvActivity.setShootPhotoMode(DJICameraSettingsDef.CameraShootPhotoMode.Single);
                        break;
                    case R.id.HDR_radioButton:
                        fpvActivity.setShootPhotoMode(DJICameraSettingsDef.CameraShootPhotoMode.HDR);
                        break;
                    case R.id.interval_radioButton:
                        fpvActivity.setShootPhotoMode(DJICameraSettingsDef.CameraShootPhotoMode.Interval);
                        intervalLinearLayout.setVisibility(View.VISIBLE);
                        //TODO init param
                        break;
                    case R.id.AEB_radioButton:
                        fpvActivity.setShootPhotoMode(DJICameraSettingsDef.CameraShootPhotoMode.AEBCapture);
                        AEBLinearLayout.setVisibility(View.VISIBLE);
                        //TODO init param
                        break;
                    default:
                        break;
                }
            }
        });
        singleRadioButton = (RadioButton) mView.findViewById(R.id.single_mode_radioButton);
        HDRRadioButton = (RadioButton) mView.findViewById(R.id.HDR_radioButton);

        intervalRadioButton = (RadioButton) mView.findViewById(R.id.interval_radioButton);
        intervalLinearLayout = (LinearLayout) mView.findViewById(R.id.interval_linearLayout);
        intervalLinearLayout.setVisibility(View.GONE);
        intervalTextView = (TextView) mView.findViewById(R.id.interval_TextView);
        intervalTextView.setText(" seconds/photo");
        intervalSeekBar = (SeekBar) mView.findViewById(R.id.interval_seekBar);
        intervalSeekBar.setMax(590);
        intervalSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                CameraPhotoIntervalParam param = new CameraPhotoIntervalParam();
                param.captureCount = 255;
                param.timeIntervalInSeconds = progress + 10;
                intervalTextView.setText(Integer.toString(param.timeIntervalInSeconds) + " seconds/photo");
                if (mCamera == null) return;
                mCamera.setPhotoIntervalParam(param, new DJIBaseComponent.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Utils.setResultToToast(fpvActivity, djiError.getDescription());
                        }
                    }
                });
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        AEBRadioButton = (RadioButton) mView.findViewById(R.id.AEB_radioButton);
        AEBLinearLayout = (LinearLayout) mView.findViewById(R.id.AEB_linearLayout);
        AEBLinearLayout.setVisibility(View.GONE);
        AEBTextView = (TextView) mView.findViewById(R.id.AEB_TextView);
        AEBSeekBar = (SeekBar) mView.findViewById(R.id.AEB_seekBar);
        AEBTextView.setText("Exposure offset:  EV");
        AEBSeekBar.setMax(3);
        AEBSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                DJICameraSettingsDef.CameraPhotoAEBParam photoAEBParam = new DJICameraSettingsDef.CameraPhotoAEBParam();
                photoAEBParam.captureCount = 5;
                photoAEBParam.exposureOffset = progress + 2;
                String EVValue = "";
                switch (photoAEBParam.exposureOffset)
                {
                    case 2:
                        EVValue = "0.3EV";
                    break;
                    default:
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        photoFormatSpinner = (Spinner) mView.findViewById(R.id.photo_format_spinner);
        photoFormatSpinner.setAdapter(new ArrayAdapter<String>(fpvActivity, R.layout.spinner_item, PHOTO_FORMAT));

        return mView;
    }


    private boolean isCameraReady() {
        mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null)
            mCamera = mProduct.getCamera();
        return mProduct != null && mCamera != null && mCamera.isConnected();
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
