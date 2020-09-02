package com.vtb.parkingmap.handler;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.auth.RefreshTokenResponse;
import com.google.protobuf.Empty;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.activity.LoginActivity;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.communication.SaigonParkingServiceStubs;
import com.vtb.parkingmap.database.SaigonParkingDatabase;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * This handler will handle exception occurs in Saigon Parking Mobile app
 * See error code in file error.txt. For example: SPE#00001: ExpiredToken
 *
 * @author bht
 */
public final class SaigonParkingExceptionHandler {

    private final Context applicationContext;
    private SaigonParkingDatabase saigonParkingDatabase;
    private SaigonParkingServiceStubs saigonParkingServiceStubs;

    public SaigonParkingExceptionHandler(Context applicationContext) {
        this.applicationContext = applicationContext;
        saigonParkingDatabase = ((SaigonParkingApplication) applicationContext).getSaigonParkingDatabase();
        saigonParkingServiceStubs = ((SaigonParkingApplication) applicationContext).getServiceStubs();
    }

    public void handleCommunicationException(StatusRuntimeException exception, ContextWrapper currentActivityOrService) {
        if (Status.UNAVAILABLE.equals(exception.getStatus())) {
            Toast.makeText(currentActivityOrService.getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
        }

        String internalErrorCode = exception.getStatus().getDescription();
        Log.d("BachMap", String
                .format("onHandleCommunicationException: %s, currentActivity: %s",
                        internalErrorCode, currentActivityOrService.getClass().getSimpleName()));

        if ("SPE#00001".equals(internalErrorCode)) {
            handleExpiredToken(currentActivityOrService);
        } else {
            handleUnknownException(currentActivityOrService);
        }
    }

    private void handleExpiredToken(ContextWrapper currentActivityOrService) {
        Log.d("BachMap", "onHandleExpiredToken");
        saigonParkingDatabase.deleteAccessToken();

        try {
            RefreshTokenResponse refreshTokenResponse = saigonParkingServiceStubs
                    .getAuthServiceBlockingStub().generateNewToken(Empty.getDefaultInstance());

            if (!refreshTokenResponse.getRefreshToken().isEmpty()) {
                saigonParkingDatabase.updateRefreshToken(refreshTokenResponse.getRefreshToken());
            }

            saigonParkingDatabase.saveNewAccessToken(refreshTokenResponse.getAccessToken());
            ((SaigonParkingApplication) applicationContext).initWebsocketConnection();
            ((SaigonParkingApplication) applicationContext).getCurrentActivity().reload();

        } catch (StatusRuntimeException exception) {
            String internalErrorCode = exception.getStatus().getDescription();

            /* refresh token is expired or refresh token is invalid */
            if ("SPE#00001".equals(internalErrorCode) || "SPE#00007".equals(internalErrorCode)) {

                /* BachMap xoa het database cu nha !!! */
                saigonParkingDatabase.emptyTable();

                /* BachMap bat user dang nhap lai: back to login activity */
                if (currentActivityOrService instanceof BaseSaigonParkingActivity) {
                    ((BaseSaigonParkingActivity) currentActivityOrService).changeActivity(LoginActivity.class);
                }
            }
        }
    }

    private void handleUnknownException(ContextWrapper currentActivityOrService) {
        Log.d("BachMap", "onHandleUnknownException");
    }
}