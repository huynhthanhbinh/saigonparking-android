package com.vtb.parkingmap.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

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
    //    SaigonParkingDatabase database ;
    private Button btnGrant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("BachMap", String.format("onCreate: %s, isSaigonParkingDatabaseInitialized: %b",
                getClass().getSimpleName(), saigonParkingDatabase != null));

        setContentView(R.layout.activity_permissions);


        if (ContextCompat.checkSelfPermission(PermissionsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            callApiWithExceptionHandling(() -> {
                boolean isCustomerHasOnGoingBooking = serviceStubs
                        .getBookingServiceBlockingStub()
                        .checkCustomerHasOnGoingBooking(Empty.getDefaultInstance())
                        .getValue();


                ((SaigonParkingApplication) getApplicationContext()).setIsBooked(isCustomerHasOnGoingBooking);


                if (isCustomerHasOnGoingBooking) {

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

                    Log.d("BachMap", "current booking: " + currentBooking);

                    ParkingLot currentParkingLot = serviceStubs
                            .getParkingLotServiceBlockingStub()
                            .getParkingLotById(Int64Value.of(currentBooking.getParkingLotId()));


                    Log.d("BachMap", "doclen: " + saigonParkingDatabase.getBookingEntity().toString());
                    Log.d("BachMap", "" + saigonParkingDatabase.getBookingEntity().getMylat());
                    Log.d("BachMap", "" + saigonParkingDatabase.getBookingEntity().getLongitude());

                    Intent intent = new Intent(PermissionsActivity.this, BookingActivity.class);
                    intent.putExtra("parkingLot", currentParkingLot);
                    intent.putExtra("mylatfromplacedetail", (Serializable) saigonParkingDatabase.getBookingEntity().getMylat());
                    intent.putExtra("mylongfromplacedetail", (Serializable) saigonParkingDatabase.getBookingEntity().getLongitude());
                    intent.putExtra("postion3lat", saigonParkingDatabase.getBookingEntity().getPosition3lat());
                    intent.putExtra("postion3long", saigonParkingDatabase.getBookingEntity().getPosition3long());
                    intent.putExtra("placedetailtype", saigonParkingDatabase.getCurrentBookingEntity().getTmpType());
                    intent.putExtra("Booking", (Serializable) currentBooking);
                    intent.putExtra("QRcode", (Serializable) qrCode);

                    startActivity(intent);
                    finish();
                    return;

                } else {
                    if (saigonParkingDatabase.getAuthKeyValueMap().size() == 3) { /* BachMap moi vao */
                        startActivity(new Intent(PermissionsActivity.this, MapActivity.class));
                        finish();
                        return;
                    } else { /* BachMapKoChoVao */
                        startActivity(new Intent(PermissionsActivity.this, LoginActivity.class));
                        finish();
                        return;
                    }
                }
            });

        }

        btnGrant = findViewById(R.id.btn_grant);

        btnGrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dexter.withActivity(PermissionsActivity.this)
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
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent();
                                                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    intent.setData(Uri.fromParts("package", getPackageName(), null));
                                                }
                                            })
                                            .show();
                                } else {
                                    Toast.makeText(PermissionsActivity.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }
                        })
                        .check();
            }
        });
    }
}
