package com.vtb.parkingmap.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.bht.saigonparking.api.grpc.booking.GetAllBookingOfCustomerRequest;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.support.ParkingListAdapter;

public class HistoryActivity extends BaseSaigonParkingActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ListView listView = findViewById(R.id.listview);


//            serviceStubs.getBookingServiceBlockingStub().getAllBookingOfCustomer(GetAllBookingOfCustomerRequest.newBuilder()
//
//                    .setNRow(5)
//                    .setPageNumber(1)
//                    .build())
//                    .getBookingList()
//                    .forEach(booking -> Log.d("BachMap", "booking: " + booking));

//            ParkingListAdapter adapter = new ParkingListAdapter(HistoryActivity.this, R.layout.activity_history,  );
//            listView.setAdapter(adapter);

    }
}
