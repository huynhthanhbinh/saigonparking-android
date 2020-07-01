package com.vtb.parkingmap.communication;

import android.content.Context;

import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.database.SaigonParkingDatabase;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.Metadata.Key;
import io.grpc.MethodDescriptor;
import lombok.AllArgsConstructor;

/**
 * Client Interceptor for Mobile Application of Customer
 * Before every request be sent,
 * Interceptor will intercept the request
 * For example: add Authorization Header into each request
 *
 * @author bht
 */
@AllArgsConstructor
public final class SaigonParkingMobileInterceptor implements ClientInterceptor {

    private Context applicationContext;
    private static final String AUTHORIZATION_KEY_NAME = "Authorization";
    private static final Key<String> AUTHORIZATION_KEY = Key.of(AUTHORIZATION_KEY_NAME, Metadata.ASCII_STRING_MARSHALLER);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                headers.put(AUTHORIZATION_KEY, getCurrentToken());
                super.start(responseListener, headers);
            }
        };
    }

    private String getCurrentToken() {
        SaigonParkingDatabase database = ((SaigonParkingApplication) applicationContext).getSaigonParkingDatabase();
        return "";
    }
}