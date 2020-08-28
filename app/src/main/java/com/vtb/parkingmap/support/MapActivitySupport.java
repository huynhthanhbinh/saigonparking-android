package com.vtb.parkingmap.support;

import android.content.Intent;

import com.bht.saigonparking.api.grpc.booking.Booking;
import com.bht.saigonparking.api.grpc.booking.GenerateBookingQrCodeRequest;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLot;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotResult;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotType;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.activity.BookingActivity;
import com.vtb.parkingmap.activity.MapActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabase;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MapActivitySupport {

    private final MapActivity mapActivity;
    private final SaigonParkingApplication saigonParkingApplication;

    public void checkCustomerHasOnGoingBooking() {
        mapActivity.callApiWithExceptionHandling(() -> {
            boolean isCustomerHasOnGoingBooking = saigonParkingApplication
                    .getServiceStubs()
                    .getBookingServiceBlockingStub()
                    .checkCustomerHasOnGoingBooking(Empty.getDefaultInstance())
                    .getValue();

            saigonParkingApplication.setIsBooked(isCustomerHasOnGoingBooking);

            if (isCustomerHasOnGoingBooking) {
                onCustomerHasOnGoingBooking();
            }
        });
    }

    private void onCustomerHasOnGoingBooking() {
        Booking currentBooking = saigonParkingApplication
                .getServiceStubs()
                .getBookingServiceBlockingStub()
                .getCustomerOnGoingBooking(Empty.getDefaultInstance());

        byte[] qrCode = saigonParkingApplication
                .getServiceStubs()
                .getBookingServiceBlockingStub()
                .generateBookingQrCode(GenerateBookingQrCodeRequest.newBuilder()
                        .setBookingId(currentBooking.getId())
                        .build())
                .getQrCode()
                .toByteArray();

        ParkingLot currentParkingLot = saigonParkingApplication
                .getServiceStubs()
                .getParkingLotServiceBlockingStub()
                .getParkingLotById(Int64Value.of(currentBooking.getParkingLotId()));

        SaigonParkingDatabase saigonParkingDatabase = saigonParkingApplication.getSaigonParkingDatabase();

        Intent intent = new Intent(mapActivity, BookingActivity.class);
        intent.putExtra("parkingLot", currentParkingLot);
        intent.putExtra("mylatfromplacedetail", saigonParkingDatabase.getBookingEntity().getMylat());
        intent.putExtra("mylongfromplacedetail", saigonParkingDatabase.getBookingEntity().getLongitude());
        intent.putExtra("postion3lat", saigonParkingDatabase.getBookingEntity().getPosition3lat());
        intent.putExtra("postion3long", saigonParkingDatabase.getBookingEntity().getPosition3long());
        intent.putExtra("placedetailtype", saigonParkingDatabase.getCurrentBookingEntity().getTmpType());
        intent.putExtra("Booking", currentBooking);
        intent.putExtra("QRcode", (Serializable) qrCode);
        mapActivity.startActivityWithLoading(intent);
    }

    public void setMarkerParkingLot(ParkingLotResult parkingLotResult) {
        String placeName = parkingLotResult.getName();
        ParkingLotType type = parkingLotResult.getType();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.snippet(String.valueOf(parkingLotResult.getId()));
        markerOptions.title(placeName);

        LatLng latLng = new LatLng(parkingLotResult.getLatitude(), parkingLotResult.getLongitude());
        markerOptions.position(latLng);

        switch (type) {
            case PRIVATE:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.makerprivate));
                break;
            case BUILDING:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.makerbuilding));
                break;
            case STREET:
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.makerstreet));
                break;
            default:
                break;
        }

        Marker marker = mapActivity.getMMap().addMarker(markerOptions);
        marker.setTag("saigon-parking-parking-lot");
        mapActivity.setMarker(marker);
    }

    public List<ParkingLotResult> sortParkingLotResultList(List<ParkingLotResult> parkingLotResultList) {
        return parkingLotResultList.stream()
                .sorted(Comparator.comparing(ParkingLotResult::getDistance))
                .collect(Collectors.toList());
    }
}