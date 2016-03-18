package com.freeman.flyshare;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import dji.sdk.Products.DJIAircraft;
import dji.sdk.base.DJIBaseProduct;


public class DJIBaseActivity extends Activity {

    private static DJIBaseProduct mProduct = null;
    private Button confirm;
    private TextView typeTV;

    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("DJIBaseAcitvity", "==========================>>> receive broadcast message!");
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
                typeTV.setText("Connect to " + mProduct.getModel());
                connectToSth = true;
            } else {
                if (mProduct instanceof DJIAircraft) {
                    DJIAircraft aircraft = (DJIAircraft) mProduct;
                    if (aircraft.getRemoteController() != null && aircraft.getRemoteController().isConnected()) {
                        typeTV.setText("Connect to RC only");
                        connectToSth = true;
                    }
                }
            }
        }
        if (!connectToSth)
            typeTV.setText("Not connected...");

        Log.e("ChangeConnectionStatus", "=======>>> mProduct is null: " + productIsNull);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_djibase);


        final Intent FPVActivity = new Intent(this, com.freeman.flyshare.FPVActivity.class);


        confirm = (Button) findViewById(R.id.confirmTypeBtn);
        confirm.setVisibility(View.VISIBLE);
        typeTV = (TextView) findViewById(R.id.typeTV);

        changeConnectionStatus();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FlyShareApplication.FLAG_CONNECTION_CHANGE);
        if (mReceiver.isInitialStickyBroadcast() || mReceiver.isOrderedBroadcast())
            unregisterReceiver(mReceiver);
        registerReceiver(mReceiver, intentFilter);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProduct = FlyShareApplication.getProductInstance();
                if (true || mProduct != null) { // Debug: add always true condition here
                    startActivity(FPVActivity);
                    unregisterReceiver(mReceiver);
                    finish();
                } else
                    Toast.makeText(getApplicationContext(), "You are not connecting to drone!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
