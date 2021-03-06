package com.vtb.parkingmap.base;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.adapter.MessageAdapter;
import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.handler.SaigonParkingExceptionHandler;
import com.vtb.parkingmap.remotes.GoogleApiService;

import io.grpc.StatusRuntimeException;
import lombok.NonNull;
import okhttp3.WebSocket;
import okio.ByteString;

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
    protected ProgressDialog progressDialog;

    /**
     * websocket will be private
     * so as to any child of this base class cannot call websocket directly !!!!
     * if any child class want to use websocket to send message
     * they must call method inherit from their parentc
     * for example sendMessage: sendWebSocketBinaryMessage/TextMessage(msg)
     */
    private WebSocket webSocket;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initProgressDialog();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((SaigonParkingApplication) getApplicationContext()).setCurrentActivity(this);
        saigonParkingExceptionHandler = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingExceptionHandler();
        saigonParkingDatabase = ((SaigonParkingApplication) getApplicationContext()).getSaigonParkingDatabase();
        serviceStubs = ((SaigonParkingApplication) getApplicationContext()).getServiceStubs();
        googleApiService = ((SaigonParkingApplication) getApplicationContext()).getGoogleApiService();
        webSocket = ((SaigonParkingApplication) getApplicationContext()).getWebSocket();
        messageAdapter = ((SaigonParkingApplication) getApplicationContext()).getMessageAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        closeCurrentProgressDialog();
        ((SaigonParkingApplication) getApplicationContext()).setCurrentActivity(this);
        Log.d("BachMap", String.format("onResume: WebSocket is null: %b",
                ((SaigonParkingApplication) getApplicationContext()).getWebSocket() == null));
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);
        super.onBackPressed();
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

    public void startActivityWithLoading(Intent intent) {
        showProgressDialog();
        startActivity(intent);
    }

    public void startActivityWithLoadingAndOption(Intent intent, ActivityOptions options) {
        showProgressDialog();
        startActivity(intent, options.toBundle());
    }

    private void showProgressDialog() {
        ((SaigonParkingApplication) getApplicationContext()).setCurrentProgressDialog(progressDialog);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    private void closeCurrentProgressDialog() {
        ProgressDialog current = ((SaigonParkingApplication) getApplicationContext()).getCurrentProgressDialog();
        if (current != null && current.isShowing()) {
            current.dismiss();
        }
    }

    protected final void sendWebSocketBinaryMessage(@NonNull SaigonParkingMessage message) {
        if (webSocket == null) {
            ((SaigonParkingApplication) getApplicationContext()).initWebsocketConnection();
            webSocket = ((SaigonParkingApplication) getApplicationContext()).getWebSocket();
        }
        webSocket.send(new ByteString(message.toByteArray()));
    }

    protected final void sendWebSocketTextMessage(@NonNull String message) {
        if (webSocket == null) {
            ((SaigonParkingApplication) getApplicationContext()).initWebsocketConnection();
            webSocket = ((SaigonParkingApplication) getApplicationContext()).getWebSocket();
        }
        webSocket.send(message);
    }

    public final void callApiWithExceptionHandling(Runnable action) {
        try {
            action.run();
        } catch (StatusRuntimeException exception) {
            saigonParkingExceptionHandler.handleCommunicationException(exception, this);
        }
    }

    private void initProgressDialog() {
        progressDialog = new ProgressDialog(this, R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
    }
}