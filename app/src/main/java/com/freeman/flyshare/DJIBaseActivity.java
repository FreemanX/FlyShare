package com.freeman.flyshare;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;


public class DJIBaseActivity extends AppCompatActivity {

    private BatteryFragment batteryFragment;
    private SDCardFragment mSDCardFragment;
    private RemoteControllerFragment mRCFragment;
    private static DJIBaseProduct mProduct = null;
    private Button confirm;
    private TextView typeTV;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            changeConnectionStatus();
        }
    };

    private void changeConnectionStatus() {
        mProduct = FlyShareApplication.getProductInstance();
        boolean connectToSth = false;
        boolean productIsNull = mProduct == null;
        if (!productIsNull) {
            Log.e("ChangeConnectionStatus", "=======>>> mProduct is connected " + mProduct.isConnected());
            if (mProduct.isConnected()) {
                typeTV.setTextColor(Color.GREEN);
                typeTV.setText("Connect to " + mProduct.getModel().toString().replace("_", " "));
                connectToSth = true;
                confirm.setVisibility(View.VISIBLE);
            } else {
                if (mProduct instanceof DJIAircraft) {
                    DJIAircraft aircraft = (DJIAircraft) mProduct;
                    if (aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                        typeTV.setTextColor(Color.BLUE);
                        typeTV.setText("Connect to RC only");
                        confirm.setVisibility(View.GONE);
                        connectToSth = true;
                    }
                }
            }
        }
        if (!connectToSth) {
            confirm.setVisibility(View.GONE);
            typeTV.setTextColor(Color.GRAY);
            typeTV.setText("Not connected...");
        }

        batteryFragment = BatteryFragment.newInstance();
        getSupportFragmentManager().beginTransaction().replace(R.id.battery_container_LinearLayout, batteryFragment, BatteryFragment.class.getName()).commit();
        mSDCardFragment = SDCardFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.SD_card_container_LinearLayout, mSDCardFragment, SDCardFragment.class.getName())
                .commit();
        mRCFragment = RemoteControllerFragment.newInstance();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.RC_container_LinearLayout, mRCFragment, RemoteControllerFragment.class.getName())
                .commit();

        Log.e("ChangeConnectionStatus", "=======>>> mProduct is null: " + productIsNull);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_djibase);


        final Intent FPVActivity = new Intent(this, com.freeman.flyshare.FPVActivity.class);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FlyShareApplication.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, intentFilter);

        confirm = (Button) findViewById(R.id.confirmTypeBtn);
        typeTV = (TextView) findViewById(R.id.typeTV);

        changeConnectionStatus();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProduct = FlyShareApplication.getProductInstance();
                if (true || mProduct != null) { // Debug: add always true condition here
                    startActivity(FPVActivity);
                    finish();
                } else
                    Toast.makeText(getApplicationContext(), "You are not connecting to drone!", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        LinearLayout appIntroLinearLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.app_introduction_layout, null);
        Rect displayRectangle = new Rect();
        Window window = this.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        appIntroLinearLayout.setMinimumWidth((int) (displayRectangle.width() * 0.9f));
        appIntroLinearLayout.setMinimumHeight((int) (displayRectangle.height() * 0.9f));
        new AlertDialog.Builder(this)
                .setView(appIntroLinearLayout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

}
