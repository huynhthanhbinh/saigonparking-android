package com.vtb.parkingmap;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.handler.SaigonParkingExceptionHandler;
import com.vtb.parkingmap.remotes.GoogleApiService;
import com.vtb.parkingmap.remotes.RetrofitBuilder;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

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
    private SaigonParkingExceptionHandler saigonParkingExceptionHandler = new SaigonParkingExceptionHandler(this);
    private WebSocketClient webSocketClient;

    @Override
    public void onCreate() {
        super.onCreate();
        applicationContext = getApplicationContext();
        Log.d("BachMap", "onCreate: SaigonParkingApplication");

        /* Init all configurations for android mobile apps */
        saigonParkingDatabase.createDatabaseIfNotExist();
//        initWebSocketConnection();
    }

    private void initWebSocketConnection() {
        Log.d("BachMap", "onInitWebSocketConnection");

        URI webSocketUri = URI.create(BuildConfig.WEBSOCKET_PREFIX + BuildConfig.GATEWAY_HOST + ':' + BuildConfig.GATEWAY_HTTP_PORT + "/contact");
        Log.d("BachMap", String.format("WebSocket URI: %s", webSocketUri));

        String token = saigonParkingDatabase.getKeyValueMap().get(SaigonParkingDatabase.ACCESS_TOKEN_KEY);
        Map<String, String> httpHeaders = new HashMap<>();
        httpHeaders.put("Authorization", "Bearer " + token);

        webSocketClient = new WebSocketClient(webSocketUri, new Draft_6455(), httpHeaders, 86400000) {
            @Override
            public void onOpen(ServerHandshake handshakeData) {
                Log.d("BachMap", "Successfully established new socket connection");
            }

            @Override
            public void onMessage(String message) {
                Log.d("BachMap", String.format("Receive message: %s", message));
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                Log.d("BachMap", "Closed socket connection");
            }

            @Override
            public void onError(Exception ex) {
                Log.d("BachMap", String.format("Connection error with exception: %s", ex.getClass().getSimpleName()));
            }
        };
        webSocketClient.connect();

    }
}