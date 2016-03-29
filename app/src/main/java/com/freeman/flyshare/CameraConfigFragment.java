package com.freeman.flyshare;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.Camera.DJICamera;
import dji.sdk.Camera.DJICameraSettingsDef;
import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseComponent;
import dji.sdk.base.DJIBaseProduct;
import dji.sdk.base.DJIError;

import static com.google.android.gms.internal.zzir.runOnUiThread;

public class CameraConfigFragment extends Fragment {

    private final DJICameraSettingsDef.CameraExposureCompensation[] cameraExposureCompensations = DJIValueArrays.CAMERA_EXPOSURE_COMPENSATIONS;
    int EVIndex = cameraExposureCompensations.length / 2;
    private final DJICameraSettingsDef.CameraISO[] cameraISOs = DJIValueArrays.CAMERA_ISOS;
    private final DJICameraSettingsDef.CameraShutterSpeed[] cameraShutterSpeeds = DJIValueArrays.CAMERA_SHUTTER_SPEEDS;

    RadioButton auto, manual;
    LinearLayout autoLayout, manualLayout;
    RadioGroup cameraModeRG;
    TextView ISOValueTV, shutterValueTV, EVValueTV;
    SeekBar ISOSeekBar, shutterSeekBar;
    Button addEVButton, minusEVButton;
    int defaultTextColor;

    DJICameraSettingsDef.CameraExposureMode currentExposureMode = null;
    DJICameraSettingsDef.CameraISO currentISO = null;
    DJICameraSettingsDef.CameraShutterSpeed currentShutterSpeed = null;
    DJICameraSettingsDef.CameraExposureCompensation currentExposureCompensation = null;


    private void setCurrentExposureMode(DJICameraSettingsDef.CameraExposureMode exposureMode) {
        this.currentExposureMode = exposureMode;
    }

    private void setCurrentISO(DJICameraSettingsDef.CameraISO ISO) {
        this.currentISO = ISO;
        if (currentISO != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ISOSeekBar.setProgress(ISOToProgress(currentISO));
                    ISOValueTV.setText(ISOToString(currentISO));
                }
            });
        }
    }

    private void setCurrentShutterSpeed(DJICameraSettingsDef.CameraShutterSpeed shutterSpeed) {
        this.currentShutterSpeed = shutterSpeed;
        if (currentShutterSpeed != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    shutterValueTV.setText(shutterToString(currentShutterSpeed));
                    shutterSeekBar.setProgress(shutterToProgress(currentShutterSpeed));
                }
            });
        }
    }

    private void setCurrentExposureCompensation(final DJICameraSettingsDef.CameraExposureCompensation cameraExposureCompensation) {
        this.currentExposureCompensation = cameraExposureCompensation;
        Log.e("setEC", "===================>> setCurrentExposureCompensation : " + currentExposureCompensation.toString());
        if (EVValueTV != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    EVValueTV.setText(EVToString(cameraExposureCompensation));
                }
            });
        }
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

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

    private int updateCurrentExposureMode() {
        DJIBaseProduct mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null && mProduct instanceof DJIAircraft) {
            DJICamera djiCamera = mProduct.getCamera();
            if (djiCamera != null && djiCamera.isConnected()) {
                djiCamera.getExposureMode(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraExposureMode>() {
                    @Override
                    public void onSuccess(DJICameraSettingsDef.CameraExposureMode cameraExposureMode) {
                        Log.e("updateEM", "===================>> updateCurrentExposureMode success: " + cameraExposureMode.toString());
                        setCurrentExposureMode(cameraExposureMode);
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        setCurrentExposureMode(null);
                        Log.e("updateISO", "===================>> updateCurrentExposureMode failed, " + djiError.getDescription());
                    }
                });
            }
        }
        return 0;
    }

    private void updateCurrentISO() {
        DJIBaseProduct mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null && mProduct instanceof DJIAircraft) {
            DJICamera djiCamera = mProduct.getCamera();
            if (djiCamera != null && djiCamera.isConnected()) {
                djiCamera.getISO(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraISO>() {
                    @Override
                    public void onSuccess(DJICameraSettingsDef.CameraISO cameraISO) {
                        Log.e("updateISO", "===================>> updateCurrentISO success: " + cameraISO.toString());
                        setCurrentISO(cameraISO);
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        setCurrentISO(null);
                        Log.e("updateISO", "===================>> updateCurrentISO failed, " + djiError.getDescription());
                    }
                });
            }
        }
    }

    private void updateCurrentShutterSpeed() {
        DJIBaseProduct mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null && mProduct instanceof DJIAircraft) {
            DJICamera djiCamera = mProduct.getCamera();
            if (djiCamera != null && djiCamera.isConnected()) {
                updateCurrentExposureMode();
                if (currentExposureMode.equals(DJICameraSettingsDef.CameraExposureMode.Manual)) {
                    djiCamera.getShutterSpeed(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraShutterSpeed>() {
                        @Override
                        public void onSuccess(DJICameraSettingsDef.CameraShutterSpeed cameraShutterSpeed) {
                            Log.e("updateSS", "===================>> updateCurrentShutterSpeed success: " + cameraShutterSpeed.toString());
                            setCurrentShutterSpeed(cameraShutterSpeed);
                        }

                        @Override
                        public void onFailure(DJIError djiError) {
                            setCurrentShutterSpeed(null);
                            Log.e("updateSS", "===================>> updateCurrentShutterSpeed failed, " + djiError.getDescription());
                        }
                    });
                } else {
                    Log.e("updateSS", "===================>> updateCurrentShutterSpeed not in manual mode: " + currentExposureMode.toString());
                }
            }
        }
    }

    private void updateCurrentExposureCompensation() {
        DJIBaseProduct mProduct = FlyShareApplication.getProductInstance();
        if (mProduct != null && mProduct instanceof DJIAircraft) {
            DJICamera djiCamera = mProduct.getCamera();
            if (djiCamera != null && djiCamera.isConnected()) {
                djiCamera.getExposureCompensation(new DJIBaseComponent.DJICompletionCallbackWith<DJICameraSettingsDef.CameraExposureCompensation>() {

                    @Override
                    public void onSuccess(DJICameraSettingsDef.CameraExposureCompensation cameraExposureCompensation) {
                        setCurrentExposureCompensation(cameraExposureCompensation);
                        Log.e("updateEC", "===================>> updateCurrentExposureCompensation success " + cameraExposureCompensation.toString());
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                        setCurrentExposureCompensation(null);
                        Log.e("updateEC", "===================>> updateCurrentExposureCompensation failed, " + djiError.getDescription());
                    }
                });
            }
        } else {
            Log.e("updateEC", "===================>> mProduct is null or is not drone");
        }
    }

    private String shutterToString(DJICameraSettingsDef.CameraShutterSpeed cameraShutterSpeed) {
        String shutterSpeedStr = cameraShutterSpeed.toString().substring(12);
        if (shutterSpeedStr.contains("_"))
            shutterSpeedStr = shutterSpeedStr.replace("_", "/");
        if (shutterSpeedStr.contains("p"))
            shutterSpeedStr = shutterSpeedStr.replace("p", ".");
        return shutterSpeedStr + "s";
    }

    private int shutterToProgress(DJICameraSettingsDef.CameraShutterSpeed cameraShutterSpeed) {
        int i;
        for (i = 0; i < cameraShutterSpeeds.length; i++) {
            if (cameraShutterSpeed.equals(cameraShutterSpeeds[i]))
                break;
        }
        return i;
    }

    private int ISOToProgress(DJICameraSettingsDef.CameraISO cameraISO) {
        int i;
        for (i = 0; i < cameraISOs.length; i++) {
            if (cameraISO.equals(cameraISOs[i]))
                break;
        }
        return i;
    }

    private String ISOToString(DJICameraSettingsDef.CameraISO cameraISO) {
        if (!cameraISO.toString().contains("_"))
            return cameraISO.toString();
        else
            return cameraISO.toString().split("_")[1];
    }

    private String EVToString(DJICameraSettingsDef.CameraExposureCompensation EV) {
        String EVDouble = "Unknown";
        if (EV.toString().toLowerCase().equals("unknown")) return EVDouble;

        String[] evStr = EV.toString().split("_");
        if (EV.toString().toLowerCase().contains("n")) {
            if (evStr[1].equals("0") && evStr[1].equals(evStr[2])) {
                EVDouble = "0.0";
            } else {
                EVDouble = "-" + evStr[1] + "." + evStr[2];
            }
        } else if (EV.toString().toLowerCase().contains("p")) {
            EVDouble = "+" + evStr[1] + "." + evStr[2];
        }

        return EVDouble + " ev";
    }

    private void setToAutoMode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                auto.setChecked(true);
                manual.setChecked(false);
                autoLayout.setVisibility(View.VISIBLE);
                manualLayout.setVisibility(View.GONE);
            }
        });

        if (FlyShareApplication.getProductInstance() == null
                || !(FlyShareApplication.getProductInstance() instanceof DJIAircraft)
                || !FlyShareApplication.getProductInstance().getCamera().isConnected())
            return;
        updateCurrentExposureCompensation();
        while (currentExposureCompensation == null) {
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentExposureCompensation != null) {
                    EVValueTV.setText(EVToString(currentExposureCompensation));
                }
            }
        });

    }

    private void setToManualMode() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                auto.setChecked(false);
                manual.setChecked(true);
                autoLayout.setVisibility(View.GONE);
                manualLayout.setVisibility(View.VISIBLE);
            }
        });

        if (FlyShareApplication.getProductInstance() == null
                || !(FlyShareApplication.getProductInstance() instanceof DJIAircraft)
                || !FlyShareApplication.getProductInstance().getCamera().isConnected())
            return;
        updateCurrentISO();
        updateCurrentShutterSpeed();
        updateCurrentShutterSpeed();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (currentISO != null)
                    ISOValueTV.setText(ISOToString(currentISO));
                if (currentShutterSpeed != null)
                    shutterValueTV.setText(shutterToString(currentShutterSpeed));
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View cameraConfigView = inflater.inflate(R.layout.fragment_camera_config, container, false);

        autoLayout = (LinearLayout) cameraConfigView.findViewById(R.id.auto_layout);
        manualLayout = (LinearLayout) cameraConfigView.findViewById(R.id.manual_layout);
        this.cameraModeRG = (RadioGroup) cameraConfigView.findViewById(R.id.camera_mode_radioGroup);
        auto = (RadioButton) cameraConfigView.findViewById(R.id.camera_mode_auto_radioButton);
        manual = (RadioButton) cameraConfigView.findViewById(R.id.camera_mode_manual_radioButton);

        this.ISOValueTV = (TextView) cameraConfigView.findViewById(R.id.iso_value_TextView);
        this.ISOSeekBar = (SeekBar) cameraConfigView.findViewById(R.id.ISO_seekBar);

        this.shutterValueTV = (TextView) cameraConfigView.findViewById(R.id.shutter_value_TextView);
        this.shutterSeekBar = (SeekBar) cameraConfigView.findViewById(R.id.shutter_seekBar);

        this.EVValueTV = (TextView) cameraConfigView.findViewById(R.id.EV_value_TextView);
        this.addEVButton = (Button) cameraConfigView.findViewById(R.id.EV_add_btn);
        this.minusEVButton = (Button) cameraConfigView.findViewById(R.id.EV_minus_btn);

        initValues();

        cameraModeRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.camera_mode_auto_radioButton) {
                    getDJICamera().setExposureMode(DJICameraSettingsDef.CameraExposureMode.Program, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null)
                                setToAutoMode();
                            else
                                Log.e("onCheckedChanged", "============> Set auto failed");
                        }
                    });

                } else if (checkedId == R.id.camera_mode_manual_radioButton) {
                    getDJICamera().setExposureMode(DJICameraSettingsDef.CameraExposureMode.Manual, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                setCurrentExposureMode(DJICameraSettingsDef.CameraExposureMode.Manual);
                                Log.e("onCheckedChanged", "============> Set manual success: " + currentExposureMode);
                                setToManualMode();
                            } else
                                Log.e("onCheckedChanged", "============> Set manual failed: " + djiError.getDescription());
                        }
                    });
                }
            }
        });
        // Init ISO settings

        //TODO init the seek bar value from DJI camera, convert the seek bar progress to ISO
        int ISORange = 6;
        ISOSeekBar.setMax(ISORange);
        defaultTextColor = ISOValueTV.getCurrentTextColor();
        ISOSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentISO = cameraISOs[progress];
                ISOValueTV.setText(ISOToString(currentISO));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                ISOValueTV.setTextColor(Color.RED);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                ISOValueTV.setTextColor(defaultTextColor);

                if (currentISO.equals(cameraISOs[0]) || seekBar.getProgress() == 0) {
                    getDJICamera().setExposureMode(DJICameraSettingsDef.CameraExposureMode.ShutterPriority, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null)
                                Log.e("onStopTrackingTouch", "setExposureMode to ShutterPriority failed " + djiError.getDescription());
                            else {
                                getDJICamera().setISO(currentISO, new DJIBaseComponent.DJICompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            updateCurrentISO();
                                            updateCurrentShutterSpeed();
                                        } else
                                            Log.e("onProgressChanged", "Set ISO failed: " + djiError.getDescription());
                                        updateCurrentISO();
                                    }
                                });
                            }
                        }
                    });
                } else {
                    getDJICamera().setExposureMode(DJICameraSettingsDef.CameraExposureMode.Manual, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null)
                                Log.e("onStopTrackingTouch", "setExposureMode to Manual failed " + djiError.getDescription());
                            else {
                                getDJICamera().setISO(currentISO, new DJIBaseComponent.DJICompletionCallback() {
                                    @Override
                                    public void onResult(DJIError djiError) {
                                        if (djiError == null) {
                                            updateCurrentISO();
                                            updateCurrentShutterSpeed();
                                        } else
                                            Log.e("onProgressChanged", "Set ISO failed: " + djiError.getDescription());
                                        updateCurrentISO();
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        // Init shutter speed settings
        shutterSeekBar.setMax(cameraShutterSpeeds.length - 1);
        shutterSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentShutterSpeed = cameraShutterSpeeds[progress];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        shutterValueTV.setText(shutterToString(currentShutterSpeed));
                    }
                });

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                shutterValueTV.setTextColor(Color.RED);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                shutterValueTV.setTextColor(defaultTextColor);
                getDJICamera().setShutterSpeed(currentShutterSpeed, new DJIBaseComponent.DJICompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                        if (djiError == null)
                            updateCurrentShutterSpeed();
                        else
                            Log.e("onStopTrackingTouch", "setShutterSpeed failed: " + djiError.getDescription());
                        updateCurrentShutterSpeed();
                    }
                });
            }
        });

        // Init EV settings
        addEVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraIsReady() && EVIndex < cameraExposureCompensations.length - 1) {
                    DJICamera djiCamera = getDJICamera();
                    EVIndex += 1;
                    Log.e("Add EV1", "============>> EVIndex: " + EVIndex + " EV:  " + EVToString(cameraExposureCompensations[EVIndex]));
                    djiCamera.setExposureCompensation(cameraExposureCompensations[EVIndex], new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                Log.e("Add EV2", "============>> EV added success!");
                                updateCurrentExposureCompensation();
                            } else
                                Log.e("Add EV2", "Error: " + djiError.getDescription());
                            updateCurrentExposureCompensation();
                        }
                    });

                }
            }
        });

        minusEVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraIsReady() && EVIndex > 0) {
                    DJICamera djiCamera = getDJICamera();
                    EVIndex -= 1;
                    Log.e("- EV1", "============>> EVIndex: " + EVIndex + " EV:  " + EVToString(cameraExposureCompensations[EVIndex]));
                    djiCamera.setExposureCompensation(cameraExposureCompensations[EVIndex], new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError == null) {
                                Log.e("- EV2", "============>> EV - success!");
                                updateCurrentExposureCompensation();
                            } else
                                Log.e("- EV2", "Error: " + djiError.getDescription());
                            updateCurrentExposureCompensation();
                        }
                    });
                }
            }
        });

        EVValueTV.setClickable(true);
        EVValueTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cameraIsReady()) {
                    final DJICamera djiCamera = FlyShareApplication.getProductInstance().getCamera();
                    djiCamera.setExposureCompensation(DJICameraSettingsDef.CameraExposureCompensation.N_0_0, new DJIBaseComponent.DJICompletionCallback() {
                        @Override
                        public void onResult(DJIError djiError) {
                            if (djiError != null) {
                                Log.e("EV Setting", "===================> EV value reset failed!" + djiError.getDescription());
                            } else {
                                EVIndex = cameraExposureCompensations.length / 2;
                                showToast("EV reset!");
                                updateCurrentExposureCompensation();
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        EVValueTV.setText(EVToString(currentExposureCompensation));
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });

        return cameraConfigView;
    }

    private void initValues() {
        this.updateCurrentExposureMode();
        if (this.currentExposureMode != null) {
            Log.e("initValues", "=============================> Exposure Mode: " + currentExposureMode.toString());
            if (this.currentExposureMode.equals(DJICameraSettingsDef.CameraExposureMode.Program)) {
                setToAutoMode();
            } else if (currentExposureMode.equals(DJICameraSettingsDef.CameraExposureMode.Manual)) {
                setToManualMode();
            } else {
                showToast("Current Exposure Mode: " + this.currentExposureMode.toString());
            }
        } else {
            Log.e("initValues", "Enter Else! ");
            setToAutoMode();
        }
    }

    private DJICamera getDJICamera() {
        return FlyShareApplication.getProductInstance().getCamera();
    }

    private boolean cameraIsReady() {
        return FlyShareApplication.getProductInstance() != null
                && FlyShareApplication.getProductInstance() instanceof DJIAircraft
                && FlyShareApplication.getProductInstance().getCamera().isConnected();
    }

    @Override
    public void onStart() {
        super.onStart();
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
