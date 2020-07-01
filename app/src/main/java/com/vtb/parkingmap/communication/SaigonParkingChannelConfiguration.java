package com.vtb.parkingmap.communication;

import android.annotation.SuppressLint;
import android.content.Context;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.grpc.ManagedChannel;
import io.grpc.android.AndroidChannelBuilder;
import io.grpc.okhttp.OkHttpChannelBuilder;
import lombok.Getter;

public final class SaigonParkingChannelConfiguration {

    private final Context applicationContext;

    public SaigonParkingChannelConfiguration(Context applicationContext) {
        this.applicationContext = applicationContext;
        managedChannel = initSaigonParkingChannel();
    }

    @Getter
    private ManagedChannel managedChannel;

    private ManagedChannel initSaigonParkingChannel() {
        SaigonParkingMobileInterceptor interceptor = new SaigonParkingMobileInterceptor(applicationContext);

        try {
            return OkHttpChannelBuilder
                    .forAddress("saigonparking.wtf", 9081)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .maxInboundMetadataSize(2 * 1020 * 1024)
                    .idleTimeout(30000, TimeUnit.MILLISECONDS)
                    .sslSocketFactory(getSocketFactory())
                    .useTransportSecurity()
                    .intercept(interceptor)
                    .build();

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            return AndroidChannelBuilder
                    .forAddress("saigonparking.wtf", 9080)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .maxInboundMetadataSize(2 * 1020 * 1024)
                    .idleTimeout(30000, TimeUnit.MILLISECONDS)
                    .usePlaintext()
                    .intercept(interceptor)
                    .build();
        }
    }

    @SuppressLint("TrustAllX509TrustManager")
    private SSLSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext sc;
        sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());

        return sc.getSocketFactory();
    }
}