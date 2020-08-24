package com.vtb.parkingmap.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.bht.saigonparking.api.grpc.booking.Booking;
import com.bht.saigonparking.api.grpc.booking.GenerateBookingQrCodeRequest;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLot;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import java.io.Serializable;

public final class PermissionsActivity extends BaseSaigonParkingActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        initBtnGrant();

        if (ContextCompat.checkSelfPermission(PermissionsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (saigonParkingDatabase.isAuthTableEmpty()) { /* Not Logged In Yet */
                onNotYetLoggedIn();

            } else { /* Already Logged In */
                callApiWithExceptionHandling(() -> {
                    boolean isCustomerHasOnGoingBooking = serviceStubs
                            .getBookingServiceBlockingStub()
                            .checkCustomerHasOnGoingBooking(Empty.getDefaultInstance())
                            .getValue();

                    ((SaigonParkingApplication) getApplicationContext()).setIsBooked(isCustomerHasOnGoingBooking);

                    if (isCustomerHasOnGoingBooking) {  /* Already Logged In, Has On Going Booking */
                        onAlreadyLoggedInHasOnGoingBooking();

                    } else { /* Already Logged In, Not Has On Going Booking */
                        onAlreadyLoggedInNotHasOnGoingBooking();
                    }
                });
            }
        }
    }

    private void onNotYetLoggedIn() {
        changeActivity(LoginActivity.class);
        finish();
    }

    private void onAlreadyLoggedInNotHasOnGoingBooking() {
        changeActivity(MapActivity.class);
        finish();
    }

    private void onAlreadyLoggedInHasOnGoingBooking() {
        Booking currentBooking = serviceStubs
                .getBookingServiceBlockingStub()
                .getCustomerOnGoingBooking(Empty.getDefaultInstance());

        byte[] qrCode = serviceStubs
                .getBookingServiceBlockingStub()
                .generateBookingQrCode(GenerateBookingQrCodeRequest.newBuilder()
                        .setBookingId(currentBooking.getId())
                        .build())
                .getQrCode()
                .toByteArray();

        ParkingLot currentParkingLot = serviceStubs
                .getParkingLotServiceBlockingStub()
                .getParkingLotById(Int64Value.of(currentBooking.getParkingLotId()));

        Intent intent = new Intent(PermissionsActivity.this, BookingActivity.class);
        intent.putExtra("parkingLot", currentParkingLot);
        intent.putExtra("mylatfromplacedetail", saigonParkingDatabase.getBookingEntity().getMylat());
        intent.putExtra("mylongfromplacedetail", saigonParkingDatabase.getBookingEntity().getLongitude());
        intent.putExtra("postion3lat", saigonParkingDatabase.getBookingEntity().getPosition3lat());
        intent.putExtra("postion3long", saigonParkingDatabase.getBookingEntity().getPosition3long());
        intent.putExtra("placedetailtype", saigonParkingDatabase.getCurrentBookingEntity().getTmpType());
        intent.putExtra("Booking", currentBooking);
        intent.putExtra("QRcode", (Serializable) qrCode);

        startActivity(intent);
        finish();
    }

    private void initBtnGrant() {
        Button btnGrant = findViewById(R.id.btn_grant);
        btnGrant.setOnClickListener(v -> Dexter.withActivity(PermissionsActivity.this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startActivity(new Intent(PermissionsActivity.this, LoginActivity.class));
                        finish();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
                            builder.setTitle("Permission Denied")
                                    .setMessage("Permission to access device location is permanently denied. you need to go to setting to allow the permission.")
                                    .setNegativeButton("Cancel", null)
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        Intent intent = new Intent();
                                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                        intent.setData(Uri.fromParts("package", getPackageName(), null));
                                    });

                            builder.show();
                        } else {
                            Toast.makeText(PermissionsActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .check());
    }
}
