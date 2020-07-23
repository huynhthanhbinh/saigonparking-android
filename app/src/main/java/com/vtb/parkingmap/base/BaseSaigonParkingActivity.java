package com.vtb.parkingmap.base;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.vtb.parkingmap.MessageChatAdapter.MessageAdapter;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.handler.SaigonParkingExceptionHandler;
import com.vtb.parkingmap.remotes.GoogleApiService;

import okhttp3.WebSocket;

/**
 * customize Activity Class for Saigon Parking App only
 *
 * @author bht
 */
public abstract class BaseSaigonParkingActivity extends AppCompatActivity {

    protected SaigonParkingExceptionHandler saigonParkingExceptionHandler;
    protected SaigonParkingDatabase saigonParkingDatabase;
    protected SaigonParkingServiceStubs serviceStubs;
    protected GoogleApiService googleApiService;
    protected MessageAdapter messageAdapter;
    protected WebSocket webSocket;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((SaigonParkingApplication) getApplicationContext()).setCurrentActivity(this);
        saigonParkingExceptionHandler = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingExceptionHandler();
        saigonParkingDatabase = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingDatabase();
        serviceStubs = ((SaigonParkingApplication) getApplicationContext()).getServiceStubs();
        googleApiService = ((SaigonParkingApplication) getApplicationContext()).getGoogleApiService();
        webSocket = ((SaigonParkingApplication) getApplicationContext()).getWebSocket();
        messageAdapter = ((SaigonParkingApplication) getApplicationContext()).getMessageAdapter();
    }

    public void reload() {
        overridePendingTransition(0, 0);
        finish();
        overridePendingTransition(0, 0);
        startActivity(getIntent());
        overridePendingTransition(0, 0);
    }

    public void changeActivity(Class<? extends BaseSaigonParkingActivity> nextActivityClass) {
        startActivity(new Intent(this, nextActivityClass));
    }
}