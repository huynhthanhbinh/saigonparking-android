package com.vtb.parkingmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bht.saigonparking.api.grpc.booking.BookingServiceGrpc;
import com.bht.saigonparking.api.grpc.user.Customer;
import com.google.protobuf.StringValue;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabase;

public final class BookingHistoryDetailsActivity extends BaseSaigonParkingActivity {
    private TextView parkingLotName;
    private TextView licensePlate;
    private TextView userName;
    private TextView createAt;
    private TextView acceptAt;
    private TextView finishAt;
    private String originBookingId;
    private BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub;
    Customer data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        originBookingId = (String) intent.getSerializableExtra("originBookingId");
        bookingServiceBlockingStub = serviceStubs.getBookingServiceBlockingStub();
        setContentView(R.layout.activity_booking_history_details);

        callApiWithExceptionHandling(() -> {
            String username = saigonParkingDatabase.getAuthKeyValueMap().get(SaigonParkingDatabase.USERNAME_KEY);
            data = serviceStubs.getUserServiceBlockingStub()
                    .getCustomerByUsername(StringValue.of(username));
            Log.d("User name: ", data.getUserInfo().getUsername());
        });

        callApiWithExceptionHandling(() -> {
            Log.d("BachMap", "Booking Detail: \n" + bookingServiceBlockingStub
                    .getBookingDetailByBookingId(StringValue.of(originBookingId)));
        });


        userName = findViewById(R.id.userName);
        parkingLotName = findViewById(R.id.txtParkingLotName);
        licensePlate = findViewById(R.id.txtLicensePlate);
        createAt = findViewById(R.id.txtCreateAt);
        acceptAt = findViewById(R.id.txtAccept);
        finishAt = findViewById(R.id.txtFinish);

//        userName.setText(data.getUserInfo().getUsername());

    }
}
