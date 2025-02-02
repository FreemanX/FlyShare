package com.freeman.flyshare;


import android.content.IntentFilter;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import dji.sdk.AirLink.DJILBAirLink;
import dji.sdk.Camera.DJICamera;
import dji.sdk.Codec.DJICodecManager;
import dji.sdk.base.DJIBaseProduct;

public class DJIFPVFragment extends Fragment implements TextureView.SurfaceTextureListener, FPVActivity.OnReceiverReceiveListener {
    private final String TAG = "DJIFPVFragment";
    protected TextureView mVideoSurface = null;
    protected DJICamera.CameraReceivedVideoDataCallback mReceivedVideoDataCallBack = null;
    protected DJILBAirLink.DJIOnReceivedVideoCallback mOnReceivedVideoCallback = null;

    protected DJICodecManager mCodecManager = null;
    private DJIBaseProduct mProduct = null;
    private DJICamera mCamera = null;

    public DJIFPVFragment() {
        // Required empty public constructor
    }

    public static DJIFPVFragment getDJIFPVFragment() {
        DJIFPVFragment fragment = new DJIFPVFragment();

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
        View FPVView = inflater.inflate(R.layout.fragment_djifpv, container, false);
        checkConnectionStatus();
        mVideoSurface = (TextureView) FPVView.findViewById(R.id.video_previewer_surface);
        if (mVideoSurface != null)
            mVideoSurface.setSurfaceTextureListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(FlyShareApplication.FLAG_CONNECTION_CHANGE);

        mReceivedVideoDataCallBack = new DJICamera.CameraReceivedVideoDataCallback() {
            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    if (videoBuffer != null && size > 0)
                        mCodecManager.sendDataToDecoder(videoBuffer, size);
                } else
                    Log.e("FPVFragment", "mCodecManager is null...");
            }
        };

        mOnReceivedVideoCallback = new DJILBAirLink.DJIOnReceivedVideoCallback() {
            @Override
            public void onResult(byte[] videoBuffer, int size) {
                if (mCodecManager != null) {
                    if (videoBuffer != null && size > 0)
                        mCodecManager.sendDataToDecoder(videoBuffer, size);
                }
            }
        };
        return FPVView;
    }

    private void initPreviewer() {
        try {
            mProduct = FlyShareApplication.getProductInstance();
        } catch (Exception exception) {
            mProduct = null;
        }

        if (null == mProduct || !mProduct.isConnected()) {
            mCamera = null;
        } else {
            if (null != mVideoSurface) {
                mVideoSurface.setSurfaceTextureListener(this);
            }

            if (!mProduct.getModel().equals(DJIBaseProduct.Model.UnknownAircraft)) {
                mCamera = mProduct.getCamera();
                if (mCamera != null) {
                    // Set the callback
                    mCamera.setDJICameraReceivedVideoDataCallback(mReceivedVideoDataCallBack);
                }
            } else {
                if (null != mProduct.getAirLink()) {
                    if (null != mProduct.getAirLink().getLBAirLink()) {
                        // Set the callback
                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(mOnReceivedVideoCallback);
                    }
                }
            }
        }
    }

    private void uninitPreviewer() {
        try {
            mProduct = FlyShareApplication.getProductInstance();
        } catch (Exception exception) {
            mProduct = null;
        }

        if (null == mProduct || !mProduct.isConnected()) {
            mCamera = null;
        } else {
            if (!mProduct.getModel().equals(DJIBaseProduct.Model.UnknownAircraft)) {
                mCamera = mProduct.getCamera();
                if (mCamera != null) {
                    // Set the callback
                    mCamera.setDJICameraReceivedVideoDataCallback(null);

                }
            } else {
                if (null != mProduct.getAirLink()) {
                    if (null != mProduct.getAirLink().getLBAirLink()) {
                        // Set the callback
                        mProduct.getAirLink().getLBAirLink().setDJIOnReceivedVideoCallback(null);
                    }
                }
            }
        }
    }

    private boolean checkConnectionStatus() {
        mProduct = FlyShareApplication.getProductInstance();
        boolean isConnected = false;
        if (mProduct != null) {
            if (mProduct.isConnected()) {
                isConnected = true;
            }
        }

        if (!isConnected) {
        }

        return isConnected;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mCodecManager == null) {
            Log.e("FPVFragment", "mCodecManager is null 2");
            mCodecManager = new DJICodecManager(getActivity(), surface, width, height);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (mCodecManager != null) {
            mCodecManager.cleanSurface();
            mCodecManager.destroyCodec();
            mCodecManager = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        initPreviewer();
        if (mVideoSurface == null) {
            Log.e(TAG, "mVideoSurface is null");
        }
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        uninitPreviewer();
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        uninitPreviewer();
        super.onDestroy();
    }

    @Override
    public void onReceiverReceive() {
        initPreviewer();
    }
}
