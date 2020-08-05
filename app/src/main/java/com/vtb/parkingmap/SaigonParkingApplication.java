package com.vtb.parkingmap;

import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;

import com.vtb.parkingmap.MessageChatAdapter.MessageAdapter;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.handler.SaigonParkingExceptionHandler;
import com.vtb.parkingmap.remotes.GoogleApiService;
import com.vtb.parkingmap.remotes.RetrofitBuilder;
import com.vtb.parkingmap.websocket.SaigonParkingWebSocketListener;

import java.util.Locale;
import java.util.Objects;

import io.paperdb.Paper;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;

/**
 * Entry point of android application
 * Will be run first before all Activity classes
 *
 * @author bht
 */
@Getter
public final class SaigonParkingApplication extends Application {

    private static final String SERVER_PATH =
            BuildConfig.WEBSOCKET_PREFIX +
                    BuildConfig.GATEWAY_HOST + ":" +
                    BuildConfig.GATEWAY_HTTP_PORT +
                    "/contact";

    @Setter
    private BaseSaigonParkingActivity currentActivity = null;
    @Setter
    private Boolean isBooked = false;

    private static Context applicationContext;
    private WebSocket webSocket;
    private MessageAdapter messageAdapter;
    private SaigonParkingDatabase saigonParkingDatabase = new SaigonParkingDatabase(this, "saigonparking.sqlite", null, 1);
    private SaigonParkingServiceStubs serviceStubs = new SaigonParkingServiceStubs(this);
    private GoogleApiService googleApiService = RetrofitBuilder.builder("https://maps.googleapis.com/").create(GoogleApiService.class);
    private SaigonParkingExceptionHandler saigonParkingExceptionHandler = new SaigonParkingExceptionHandler(this);

    @Override
    public void onCreate() {
        Log.d("BachMap", "onCreate: SaigonParkingApplication");
        super.onCreate();

        /* Init all configurations for android mobile apps */
        Locale.setDefault(Locale.US);
        applicationContext = getApplicationContext();

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        messageAdapter = new MessageAdapter(inflater);
        saigonParkingDatabase.createDatabaseIfNotExist();

        //khởi tạo nơi lưu trữ dữ liệu historymessage
        Paper.init(applicationContext);
    }

    public void initWebsocketConnection() {
        closeSocketConnection();

        String token = Objects.requireNonNull(saigonParkingDatabase
                .getAuthKeyValueMap()
                .get(SaigonParkingDatabase.ACCESS_TOKEN_KEY));

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_PATH)
                .addHeader("Authorization", token)
                .build();

        SaigonParkingWebSocketListener listener = new SaigonParkingWebSocketListener(this);
        webSocket = client.newWebSocket(request, listener);
    }

    public void closeSocketConnection() {
        if (webSocket != null) {
            try {
                webSocket.cancel();
            } catch (Exception exception) {
                Log.d("BachMap", "initWebsocketConnectionError: " + exception.getMessage());
            }
            webSocket = null;
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        closeSocketConnection();
    }
}