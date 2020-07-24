package com.vtb.parkingmap.base;

import android.app.Service;

import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.vtb.parkingmap.MessageChatAdapter.MessageAdapter;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.handler.SaigonParkingExceptionHandler;
import com.vtb.parkingmap.remotes.GoogleApiService;

import lombok.NonNull;
import okhttp3.WebSocket;
import okio.ByteString;

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

    /**
     * websocket will be private
     * so as to any child of this base class
     * cannot call websocket directly !!!!
     * <p>
     * if any child class want to use websocket to send message
     * they must call method inherit from their parent
     * for example sendMessage: sendWebSocketBinaryMessage/TextMessage(msg)
     */
    private WebSocket webSocket;

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

    protected void sendWebSocketBinaryMessage(@NonNull SaigonParkingMessage message) {
        if (webSocket == null) {
            ((SaigonParkingApplication) getApplicationContext()).initWebsocketConnection();
            webSocket = ((SaigonParkingApplication) getApplicationContext()).getWebSocket();
        }
        webSocket.send(new ByteString(message.toByteArray()));
    }

    protected void sendWebSocketTextMessage(@NonNull String message) {
        if (webSocket == null) {
            ((SaigonParkingApplication) getApplicationContext()).initWebsocketConnection();
            webSocket = ((SaigonParkingApplication) getApplicationContext()).getWebSocket();
        }
        webSocket.send(message);
    }
}