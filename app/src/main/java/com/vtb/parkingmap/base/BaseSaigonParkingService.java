package com.vtb.parkingmap.base;

import android.app.Service;

import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.handler.SaigonParkingExceptionHandler;
import com.vtb.parkingmap.remotes.GoogleApiService;

/**
 * customize Service Class for Saigon Parking App only
 *
 * @author bht
 */
public abstract class BaseSaigonParkingService extends Service {

    protected SaigonParkingExceptionHandler saigonParkingExceptionHandler;
    protected SaigonParkingDatabase saigonParkingDatabase;
    protected SaigonParkingServiceStubs serviceStubs;
    protected GoogleApiService googleApiService;

    @Override
    public void onCreate() {
        super.onCreate();
        saigonParkingExceptionHandler = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingExceptionHandler();
        saigonParkingDatabase = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingDatabase();
        serviceStubs = ((SaigonParkingApplication) getApplicationContext()).getServiceStubs();
        googleApiService = ((SaigonParkingApplication) getApplicationContext()).getGoogleApiService();
    }
}