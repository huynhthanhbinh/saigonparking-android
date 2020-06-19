package com.vtb.parkingmap;

import com.bht.parkingmap.api.proto.parkinglot.ParkingLotServiceGrpc;
import com.bht.parkingmap.api.proto.user.UserServiceGrpc;
import com.vtb.parkingmap.remotes.GoogleApiService;
import com.vtb.parkingmap.remotes.RetrofitBuilder;

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

public class Common {


    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static GoogleApiService getGoogleApiService() {
        return RetrofitBuilder.builder(GOOGLE_API_URL).create(GoogleApiService.class);
    }

    private static final ManagedChannel CHANNEL = initChannel();

    private static SSLSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
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

    private static ManagedChannel initChannel() {
        try {
            return OkHttpChannelBuilder
                    .forAddress("saigonparking.wtf", 9081)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .idleTimeout(5000, TimeUnit.MILLISECONDS)
                    .sslSocketFactory(getSocketFactory())
                    .useTransportSecurity()
                    .build();

        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            return AndroidChannelBuilder
                    .forAddress("saigonparking.wtf", 9080)
                    .maxInboundMessageSize(10 * 1024 * 1024)
                    .idleTimeout(5000, TimeUnit.MILLISECONDS)
                    .usePlaintext()
                    .build();
        }
    }

    public static ParkingLotServiceGrpc.ParkingLotServiceBlockingStub parkingLotServiceBlockingStub = ParkingLotServiceGrpc.newBlockingStub(CHANNEL);

    public static UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub = UserServiceGrpc.newBlockingStub(CHANNEL);
}