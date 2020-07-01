package com.vtb.parkingmap.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.database.SaigonParkingDatabase;

/**
 * customize Fragment Activity for Parking Map App only
 */
public abstract class BaseSaigonParkingFragmentActivity extends AppCompatActivity {

    protected SaigonParkingDatabase saigonParkingDatabase;

    public BaseSaigonParkingFragmentActivity() {
        super();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        saigonParkingDatabase = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingDatabase();
    }
}