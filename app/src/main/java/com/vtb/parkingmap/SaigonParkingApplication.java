package com.vtb.parkingmap;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.remotes.GoogleApiService;
import com.vtb.parkingmap.remotes.RetrofitBuilder;

import lombok.Getter;

/**
 * Entry point of android application
 * Will be run first before all Activity classes
 *
 * @author bht
 */
@Getter
public final class SaigonParkingApplication extends Application {

    private static Context applicationContext;

    private SaigonParkingDatabase saigonParkingDatabase = new SaigonParkingDatabase(this, "saigonparking.sqlite", null, 1);
    private SaigonParkingServiceStubs serviceStubs = new SaigonParkingServiceStubs(this);
    private GoogleApiService googleApiService = RetrofitBuilder.builder("https://maps.googleapis.com/").create(GoogleApiService.class);

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Log.d("BachMap", "onCreate: SaigonParkingApplication");

        /* Init all configurations for android mobile apps */
        saigonParkingDatabase.createDatabaseIfNotExist();
    }
}