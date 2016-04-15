package com.freeman.flyshare;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.LinkedList;

import dji.sdk.Camera.DJICamera;
import dji.sdk.Camera.DJICameraParameters;
import dji.sdk.Camera.DJICameraSettingsDef;
import dji.sdk.Camera.DJICameraSettingsDef.CameraPhotoIntervalParam;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;


public class CameraSettingFragment extends Fragment {

    final String[] PHOTO_FORMAT = {"JPEG", "RAW", "RAW + JPEG"};//, "TIFF(14 Bit)"};

    private DJICameraParameters.VideoResolutionFps[] videoResolutionFpses;
    private LinkedList<String> resFpsLinkedList = new LinkedList<>();
    private DJIBaseProduct mProduct;
    private DJICamera mCamera;

    private View mView;

    private LinearLayout photoSettingLinearLayout, videoSettingLinearLayout, intervalLinearLayout, AEBLinearLayout;
    private RadioGroup cameraModeRadioGroup, videoFormatRadioGroup, videoStandardRadioGroup;
    private RadioButton singleRadioButton, HDRRadioButton, intervalRadioButton, AEBRadioButton, MP4RadioButton, MOVRadioButton, NTSCRadioButton, PALRadioButton;
    private TextView intervalTextView, AEBTextView;
    private SeekBar intervalSeekBar, AEBSeekBar;
    private Spinner photoFormatSpinner, videoResSpinner;

    private FPVActivity fpvActivity;

    private void initSettingVariables() {
        fpvActivity = (FPVActivity) getActivity();

        if (isCameraReady()) {
//            videoResolutionFpses = DJICameraParameters.getInstance().supportedCameraVideoResolutionAndFrameRateRange();
//
//            for (DJICameraParameters.VideoResolutionFps resolutionFps : DJICameraParameters.getInstance().supportedCameraVideoResolutionAndFrameRateRange()) {
//                resFpsLinkedList.add(resolutionFps.toString());
//            }
        }

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
        videoResolutionFpses = DJICameraParameters.getInstance().supportedCameraVideoResolutionAndFrameRateRange();
    }

    private void initPhotoSettingUI() {
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
                        if (mCamera == null) return;
                        mCamera.getPhotoIntervalParam(new DJIBaseComponent.DJICompletionCallbackWith<CameraPhotoIntervalParam>() {
                            @Override
                            public void onSuccess(final CameraPhotoIntervalParam cameraPhotoIntervalParam) {
                                fpvActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        intervalSeekBar.setProgress(cameraPhotoIntervalParam.timeIntervalInSeconds - 10);
                                        intervalTextView.setText(Integer.toString(cameraPhotoIntervalParam.timeIntervalInSeconds) + " seconds/photo");
                                    }
                                });
                            }

                            @Override
                            public void onFailure(DJIError djiError) {
                                Utils.setResultToToast(fpvActivity, djiError.getDescription());
                            }
                        });
                        break;
                    case R.id.AEB_radioButton:
                        fpvActivity.setShootPhotoMode(DJICameraSettingsDef.CameraShootPhotoMode.AEBCapture);
                        AEBLinearLayout.setVisibility(View.VISIBLE);
                        //TODO init param
                        if (mCamera == null) return;
                        mCamera.getPhotoAEBParam(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraPhotoAEBParam>() {
                            @Override
                            public void onSuccess(final DJICameraSettingsDef.CameraPhotoAEBParam cameraPhotoAEBParam) {
                                fpvActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        String EVValue = "";
                                        switch (cameraPhotoAEBParam.exposureOffset) {
                                            case 2:
                                                EVValue = "0.3EV";
                                                break;
                                            case 3:
                                                EVValue = "0.7EV";
                                                break;
                                            case 4:
                                                EVValue = "1.0EV";
                                                break;
                                            case 5:
                                                EVValue = "1.3EV";
                                                break;
                                            default:
                                                break;
                                        }
                                        AEBTextView.setText("Exposure offset: " + EVValue);
                                        AEBSeekBar.setProgress(cameraPhotoAEBParam.exposureOffset - 2);
                                    }
                                });

                            }

                            @Override
                            public void onFailure(DJIError djiError) {
                                Utils.setResultToToast(fpvActivity, djiError.getDescription());
                            }
                        });
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
                switch (photoAEBParam.exposureOffset) {
                    case 2:
                        EVValue = "0.3EV";
                        break;
                    case 3:
                        EVValue = "0.7EV";
                        break;
                    case 4:
                        EVValue = "1.0EV";
                        break;
                    case 5:
                        EVValue = "1.3EV";
                        break;
                    default:
                        break;
                }
                AEBTextView.setText("Exposure offset: " + EVValue);
                if (mCamera == null) return;
                mCamera.setPhotoAEBParam(photoAEBParam, new DJIBaseComponent.DJICompletionCallback() {
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

        photoFormatSpinner = (Spinner) mView.findViewById(R.id.photo_format_spinner);
        photoFormatSpinner.setAdapter(new ArrayAdapter<String>(fpvActivity, R.layout.spinner_item, PHOTO_FORMAT));
        photoFormatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Utils.setResultToToast(fpvActivity, "Position: " + Integer.toString(position)); // TODO debug only, delete when use
                if (mCamera == null) return;
                DJIBaseComponent.DJICompletionCallback callback = new DJIBaseComponent.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Utils.setResultToToast(fpvActivity, djiError.getDescription());
                        }
                    }
                };
                switch (position) {
                    case 0:
                        mCamera.setPhotoFileFormat(DJICameraSettingsDef.CameraPhotoFileFormat.JPEG, callback);
                        break;
                    case 1:
                        mCamera.setPhotoFileFormat(DJICameraSettingsDef.CameraPhotoFileFormat.RAW, callback);
                        break;
                    case 2:
                        mCamera.setPhotoFileFormat(DJICameraSettingsDef.CameraPhotoFileFormat.RAWAndJPEG, callback);
                        break;
                    case 3:
                        mCamera.setPhotoFileFormat(DJICameraSettingsDef.CameraPhotoFileFormat.TIFF14Bit, callback);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initVideoSettingUI() {
        videoSettingLinearLayout = (LinearLayout) mView.findViewById(R.id.videoSettingsLinearLayout);

        videoFormatRadioGroup = (RadioGroup) mView.findViewById(R.id.video_format_radioGroup);
        videoFormatRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (mCamera == null) return;
                DJICameraSettingsDef.CameraVideoFileFormat fileFormat;
                if (checkedId == R.id.MOV_radioButton) {
                    fileFormat = DJICameraSettingsDef.CameraVideoFileFormat.MOV;
                } else {
                    fileFormat = DJICameraSettingsDef.CameraVideoFileFormat.MP4;
                }
                mCamera.setVideoFileFormat(fileFormat, new DJIBaseComponent.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError != null) {
                            Utils.setResultToToast(fpvActivity, djiError.getDescription());
                        }
                    }
                });
            }
        });
        MP4RadioButton = (RadioButton) mView.findViewById(R.id.MP4_radioButton);
        MOVRadioButton = (RadioButton) mView.findViewById(R.id.MOV_radioButton);

        videoResSpinner = (Spinner) mView.findViewById(R.id.video_res_spinner);
        videoResSpinner.setVisibility(View.GONE);
        videoResSpinner.setAdapter(new ArrayAdapter<String>(fpvActivity, R.layout.spinner_item, resFpsLinkedList));
        videoResSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Utils.setResultToToast(fpvActivity, "ResFps: " + Integer.toString(position));
                if (mCamera != null) {
                    mCamera.setVideoResolutionAndFrameRate(videoResolutionFpses[position].resolution, videoResolutionFpses[position].fps, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                Utils.setResultToToast(fpvActivity, djiError.getDescription());
                            }
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        videoStandardRadioGroup = (RadioGroup) mView.findViewById(R.id.video_standard_radioGroup);
        videoStandardRadioGroup.setVisibility(View.GONE);
//        videoStandardRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                if (mCamera == null) return;
//                DJICameraSettingsDef.CameraVideoStandard cameraVideoStandard;
//                if (checkedId == R.id.PAL_radioButton) {
//                    cameraVideoStandard = DJICameraSettingsDef.CameraVideoStandard.PAL;
//                } else
//                    cameraVideoStandard = DJICameraSettingsDef.CameraVideoStandard.NTSC;
//                mCamera.setVideoStandard(cameraVideoStandard, new DJIBaseComponent.DJICompletionCallback() {
//                    @Override
//                    public void onResult(DJIError djiError) {
//                        if (djiError != null) {
//                            Utils.setResultToToast(fpvActivity, djiError.getDescription());
//                        }
//                    }
//                });
//            }
//        });
        NTSCRadioButton = (RadioButton) mView.findViewById(R.id.NTSC_radioButton);
        PALRadioButton = (RadioButton) mView.findViewById(R.id.PAL_radioButton);
    }

    private void initUIVariables() {
//        if (fpvActivity.isPhotoMode) {
//            photoSettingLinearLayout.setVisibility(View.VISIBLE);
//            videoSettingLinearLayout.setVisibility(View.GONE);
//        } else {
//            photoSettingLinearLayout.setVisibility(View.GONE);
//            videoSettingLinearLayout.setVisibility(View.VISIBLE);
//        }
        photoSettingLinearLayout.setVisibility(View.VISIBLE);
        videoSettingLinearLayout.setVisibility(View.VISIBLE);
        if (isCameraReady()) {
            mCamera.getPhotoFileFormat(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraPhotoFileFormat>() {
                @Override
                public void onSuccess(DJICameraSettingsDef.CameraPhotoFileFormat cameraPhotoFileFormat) {
                    if (cameraPhotoFileFormat == DJICameraSettingsDef.CameraPhotoFileFormat.JPEG) {
                        fpvActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                photoFormatSpinner.setSelection(0);
                            }
                        });
                    } else if (cameraPhotoFileFormat == DJICameraSettingsDef.CameraPhotoFileFormat.RAW) {
                        fpvActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                photoFormatSpinner.setSelection(1);
                            }
                        });
                    } else if (cameraPhotoFileFormat == DJICameraSettingsDef.CameraPhotoFileFormat.RAWAndJPEG) {
                        fpvActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                photoFormatSpinner.setSelection(2);
                            }
                        });
                    } else if (cameraPhotoFileFormat == DJICameraSettingsDef.CameraPhotoFileFormat.TIFF14Bit) {
                        fpvActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                photoFormatSpinner.setSelection(3);
                            }
                        });
                    }


                }

                @Override
                public void onFailure(DJIError djiError) {
                    if (djiError != null) {
                        Utils.setResultToToast(fpvActivity, djiError.getDescription());
                    }
                }
            });
            mCamera.getVideoFileFormat(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraVideoFileFormat>() {
                @Override
                public void onSuccess(DJICameraSettingsDef.CameraVideoFileFormat cameraVideoFileFormat) {
                    if (cameraVideoFileFormat == DJICameraSettingsDef.CameraVideoFileFormat.MOV) {
                        fpvActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MOVRadioButton.setChecked(true);
                            }
                        });

                    } else {
                        fpvActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MP4RadioButton.setChecked(true);
                            }
                        });

                    }
                }

                @Override
                public void onFailure(DJIError djiError) {
                    if (djiError != null) {
                        Utils.setResultToToast(fpvActivity, djiError.getDescription());
                    }
                }
            });
            mCamera.getVideoResolutionAndFrameRate(new DJIBaseComponent.DJICompletionCallbackWithTwoParam<DJICameraSettingsDef.CameraVideoResolution, DJICameraSettingsDef.CameraVideoFrameRate>() {
                @Override
                public void onSuccess(DJICameraSettingsDef.CameraVideoResolution cameraVideoResolution, DJICameraSettingsDef.CameraVideoFrameRate cameraVideoFrameRate) {
//                    int positioin = 0;
//                    for (int i = 0; i < videoResolutionFpses.length; i++) {
//                        if (videoResolutionFpses[i].fps == cameraVideoFrameRate && videoResolutionFpses[i].resolution == cameraVideoResolution) {
//                            positioin = i;
//                            break;
//                        }
//                    }
//                    videoResSpinner.setSelection(positioin);
                }

                @Override
                public void onFailure(DJIError djiError) {
                    if (djiError != null) {
                        Utils.setResultToToast(fpvActivity, djiError.getDescription());
                    }
                }
            });
//            mCamera.getVideoStandard(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraVideoStandard>() {
//                @Override
//                public void onSuccess(DJICameraSettingsDef.CameraVideoStandard cameraVideoStandard) {
//                    if (cameraVideoStandard == DJICameraSettingsDef.CameraVideoStandard.NTSC) {
//                        NTSCRadioButton.setChecked(true);
//                    } else {
//                        PALRadioButton.setChecked(true);
//                    }
//                }
//
//                @Override
//                public void onFailure(DJIError djiError) {
//                    if (djiError != null) {
//                        Utils.setResultToToast(fpvActivity, djiError.getDescription());
//                    }
//                }
//            });
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initSettingVariables();
        mView = inflater.inflate(R.layout.fragment_camera_setting, container, false);
        initPhotoSettingUI();
        initVideoSettingUI();
        return mView;
    }

    @Override
    public void onResume() {
        initUIVariables();
        super.onResume();
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
