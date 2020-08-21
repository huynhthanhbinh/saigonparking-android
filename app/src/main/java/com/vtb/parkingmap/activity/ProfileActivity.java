package com.vtb.parkingmap.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bht.saigonparking.api.grpc.user.Customer;
import com.google.protobuf.StringValue;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabase;

public final class ProfileActivity extends BaseSaigonParkingActivity {
    private TextView firstName;
    private TextView lastName;
    private TextView userName;
    private TextView email;
    private TextView mobile;
    Customer data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        callApiWithExceptionHandling(() -> {
            String username = saigonParkingDatabase.getAuthKeyValueMap().get(SaigonParkingDatabase.USERNAME_KEY);

            data = serviceStubs.getUserServiceBlockingStub()
                    .getCustomerByUsername(StringValue.of(username));
            Log.d("First name: ", data.getFirstName());
            Log.d("Last name: ", data.getLastName());
            Log.d("User name: ", data.getUserInfo().getUsername());
            Log.d("Phone: ", data.getPhone());
            Log.d("Email: ", data.getUserInfo().getEmail());

        });
        firstName = findViewById(R.id.firstName);
        lastName = findViewById(R.id.lastName);
        userName = findViewById(R.id.userName);
        email = findViewById(R.id.email);
        mobile = findViewById(R.id.mobile);

        firstName.setText(data.getFirstName());
        lastName.setText(data.getLastName());
        userName.setText(data.getUserInfo().getUsername());
        email.setText(data.getUserInfo().getEmail());
        mobile.setText(data.getPhone());
    }
}
