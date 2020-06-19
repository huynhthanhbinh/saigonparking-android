package com.vtb.parkingmap.base;

import android.support.v7.app.AppCompatActivity;

/**
 * customize Fragment Activity for Parking Map App only
 */
public abstract class BaseParkingMapFragmentActivity extends AppCompatActivity {

    public BaseParkingMapFragmentActivity() {
        super();
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        Toast.makeText(this, "onDestroy: " + getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onPause() {
//        super.onPause();
//        Toast.makeText(this, "onPause: " + getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        Toast.makeText(this, "OnResume: " + getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//        Common.CHANNEL.resetConnectBackoff();
//    }
//
//    @Override
//    protected void onStart() {
//        super.onStart();
//        Toast.makeText(this, "OnStart: " + getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//    }
//
//    @Override
//    protected void onStop() {
//        super.onStop();
//        Toast.makeText(this, "OnStop: " + getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
//    }
}