package com.vtb.parkingmap.handler;

import android.content.Context;
import android.util.Log;

import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import io.grpc.StatusRuntimeException;

/**
 * This handler will handle exception occurs in Saigon Parking Mobile app
 * See error code in file error.txt. For example: SPE#00001: ExpiredToken
 *
 * @author bht
 */
public final class SaigonParkingExceptionHandler {

    private final Context applicationContext;

    public SaigonParkingExceptionHandler(Context applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void handleCommunicationException(StatusRuntimeException exception, BaseSaigonParkingActivity currentActivity) {
        String internalErrorCode = exception.getStatus().getDescription();
        Log.d("BachMap", String
                .format("onHandleCommunicationException: %s, currentActivity: %s",
                        internalErrorCode, currentActivity.getClass().getSimpleName()));

        if ("SPE#00001".equals(internalErrorCode)) {
            handleExpiredToken();
        } else {
            handleUnknownException();
        }
    }

    private void handleExpiredToken() {
        Log.d("BachMap", "onHandleExpiredToken");
    }

    private void handleUnknownException() {
        Log.d("BachMap", "onHandleUnknownException");
    }
}