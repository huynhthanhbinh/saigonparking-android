package com.vtb.parkingmap.base;

import android.app.Service;

import com.vtb.parkingmap.MessageChatAdapter.MessageAdapter;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.handler.SaigonParkingExceptionHandler;
import com.vtb.parkingmap.remotes.GoogleApiService;

import okhttp3.WebSocket;

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
    protected MessageAdapter messageAdapter;
    protected WebSocket webSocket;

    @Override
    public void onCreate() {
        super.onCreate();
        saigonParkingExceptionHandler = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingExceptionHandler();
        saigonParkingDatabase = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingDatabase();
        serviceStubs = ((SaigonParkingApplication) getApplicationContext()).getServiceStubs();
        googleApiService = ((SaigonParkingApplication) getApplicationContext()).getGoogleApiService();
        webSocket = ((SaigonParkingApplication) getApplicationContext()).getWebSocket();
        messageAdapter = ((SaigonParkingApplication) getApplicationContext()).getMessageAdapter();
    }
}