package com.vtb.parkingmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.bht.saigonparking.api.grpc.booking.Booking;
import com.bht.saigonparking.api.grpc.booking.BookingStatus;
import com.bht.saigonparking.api.grpc.contact.BookingCancellationContent;
import com.bht.saigonparking.api.grpc.contact.BookingProcessingContent;
import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLot;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotType;
import com.vtb.parkingmap.BuildConfig;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.paperdb.Paper;
import lombok.Getter;

@Getter
public final class BookingActivity extends BaseSaigonParkingActivity {

    private ParkingLot parkingLot;
    double mylat;
    double mylong;
    double position3lat;
    double position3long;
    int tmpType;
    private double latitude;
    private double longitude;
    private String closingHour;
    final int deltaHour = 1;
    private LinearLayout btnimgdirection;
    private LinearLayout btnimgchat;
    private LinearLayout btnimgcancel;
    private BookingProcessingContent bookingProcessingContent;
    private ImageView imgQRCode;
    private TextView txtBookingID;
    private TextView txtCreatedAt;
    private TextView txtStatus;
    private TextView txtParking;
    private TextView txtAddress;
    private TextView txtLicensePlate;
    private LottieAnimationView iconPendding;
    private LottieAnimationView iconAccept;

    private String reducedBookingId;
    private String licensePlate;
    private String amountOfParkingHourString;


    //parking-lot
    private boolean bookingsecond = false;
    private long id;
    private ParkingLotType type;
    //Booking
    private Booking currentBooking;
    byte[] imageData;
    boolean accepted = false;

    private String SERVER_PATH = BuildConfig.WEBSOCKET_PREFIX + BuildConfig.GATEWAY_HOST + ":" + BuildConfig.GATEWAY_HTTP_PORT + "/contact";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        bookingProcessingContent = (BookingProcessingContent) intent.getSerializableExtra("bookingProcessingContent");
        setContentView(R.layout.activity_booking);
        if (bookingProcessingContent != null) {
            Log.d("BachMap", "\n\nbooking id: " + bookingProcessingContent.getBookingId());
            Log.d("BachMap", "\n\ncreated at: " + bookingProcessingContent.getCreatedAt());
            Log.d("BachMap", "\n\nQR code: " + bookingProcessingContent.getQrCode());

            parkingLot = (ParkingLot) intent.getSerializableExtra("parkingLot");
            mylat = (double) intent.getSerializableExtra("mylatfromplacedetail");
            mylong = (double) intent.getSerializableExtra("mylongfromplacedetail");
            position3lat = intent.getDoubleExtra("postion3lat", 1234);
            position3long = intent.getDoubleExtra("postion3long", 1234);
            tmpType = (int) intent.getSerializableExtra("placedetailtype");
            licensePlate = intent.getStringExtra("licenseplate");
            amountOfParkingHourString = intent.getStringExtra("parkinghour");
            imageData = bookingProcessingContent.getQrCode().toByteArray();

            if ((Boolean) intent.getSerializableExtra("accept") != null) {
                accepted = (Boolean) intent.getSerializableExtra("accept");
            }
        } else {
            parkingLot = (ParkingLot) intent.getSerializableExtra("parkingLot");
            currentBooking = (Booking) intent.getSerializableExtra("Booking");
            licensePlate = currentBooking.getLicensePlate();
            imageData = (byte[]) intent.getSerializableExtra("QRcode");
            tmpType = (int) intent.getSerializableExtra("placedetailtype");
            mylat = (double) intent.getSerializableExtra("mylatfromplacedetail");
            mylong = (double) intent.getSerializableExtra("mylongfromplacedetail");
            if ((Boolean) intent.getSerializableExtra("accept") != null) {
                accepted = (Boolean) intent.getSerializableExtra("accept");
            }

            //Handle null when reInstall App
            position3lat = intent.getDoubleExtra("postion3lat", 1234);
            position3long = intent.getDoubleExtra("postion3long", 1234);

            Log.d("BachMap", "sau khi tat app: " + parkingLot);
        }


        Log.d("khongbiloi", "Nhan du lieu 2");

        //call function
        initReducedBookingId();
        initQRCode();
        initBookingDetail();
        initAllButtons();
        processParkingLot();

        //ping ping client
        btnimgcancel.setOnClickListener(view -> {
            cancelBooking();
            onBackPressed();
        });

        btnimgdirection.setOnClickListener(this::onClickDrawDirection);


        btnimgchat.setOnClickListener(view -> {
            Intent intent1 = new Intent(BookingActivity.this, ChatActivity.class);
            intent1.putExtra("idparkinglot", (Serializable) id);
            intent1.putExtra("parkingLot", (Serializable) parkingLot);
            intent1.putExtra("mylatfromplacedetail", (Serializable) mylat);
            intent1.putExtra("mylongfromplacedetail", (Serializable) mylong);
            intent1.putExtra("postion3lat", (Serializable) position3lat);
            intent1.putExtra("postion3long", (Serializable) position3long);
            intent1.putExtra("placedetailtype", (Serializable) tmpType);
            intent1.putExtra("licenseplate", (Serializable) licensePlate);
            intent1.putExtra("parkinghour", (Serializable) amountOfParkingHourString);
            intent1.putExtra("Booking", (Serializable) currentBooking);
            intent1.putExtra("QRcode", (Serializable) imageData);
            intent1.putExtra("accept", accepted);
            intent1.putExtra("bookingProcessingContent", bookingProcessingContent);
            startActivity(intent1);
            finish();
        });
    }


    private void processParkingLot() {
        if (parkingLot != null) {
            assignParkingLotFields();
        }
    }

    private void assignParkingLotFields() {
        id = parkingLot.getId();
        type = parkingLot.getType();
        latitude = parkingLot.getLatitude();
        longitude = parkingLot.getLongitude();
        closingHour = parkingLot.getClosingHour();
    }


    @SuppressWarnings("unused")
    private void onClickDrawDirection(View view) {
        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        String currentDateTimeString = sdf.format(d);

        Time currentTime = Time.valueOf(currentDateTimeString);
        Time closingTime = Time.valueOf(closingHour);

        boolean check = (currentTime.getTime() + deltaHour * 60 * 60) - closingTime.getTime() > 0;

        if (check) {
            Toast.makeText(BookingActivity.this, " Quá giờ rồi Không còn nhận xe nhé", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(BookingActivity.this, ViewDrawDirectionActivity.class);
        intent.putExtra("parkingLot", (Serializable) parkingLot);

        intent.putExtra("placedetaillat", (Serializable) latitude);
        intent.putExtra("placedetaillong", (Serializable) longitude);
        intent.putExtra("mylatfromplacedetail", (Serializable) mylat);
        intent.putExtra("mylongfromplacedetail", (Serializable) mylong);
        intent.putExtra("placedetailtype", (Serializable) tmpType);
        intent.putExtra("idplacedetail", (Serializable) id);
        intent.putExtra("licenseplate", (Serializable) licensePlate);
        intent.putExtra("parkinghour", (Serializable) amountOfParkingHourString);
        intent.putExtra("Booking", (Serializable) currentBooking);
        intent.putExtra("QRcode", (Serializable) imageData);
        intent.putExtra("accept", accepted);
        intent.putExtra("bookingProcessingContent", bookingProcessingContent);
        double tmp = 1234;
        if (position3lat != tmp) {
            intent.putExtra("position3lat", (Serializable) position3lat);
            intent.putExtra("position3long", (Serializable) position3long);
        }
        startActivity(intent);
        finish();
    }

    public class Broadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(ViewDrawDirectionActivity.Broadcast.class.getSimpleName(), "Đã nhận được broadcast");
            parkingLot = (ParkingLot) intent.getSerializableExtra("parkinglot_broadcast");
            mylat = (double) intent.getSerializableExtra("myLat_broadcast");
            mylong = (double) intent.getSerializableExtra("myLong_broadcast");
            position3lat = intent.getDoubleExtra("postion3lat_broadcast", 1234);
            position3long = intent.getDoubleExtra("postion3long_broadcast", 1234);
            Log.d("khongbiloi", "" + parkingLot);
            processParkingLot();

        }
    }


    private void initReducedBookingId() {
        if (bookingProcessingContent != null) {
            String originBookingId = bookingProcessingContent.getBookingId();
            reducedBookingId = "****** " + originBookingId.substring(originBookingId.length() - 12);
        } else {

            String originBookingId = currentBooking.getId();
            reducedBookingId = "****** " + originBookingId.substring(originBookingId.length() - 12);
        }

    }

    private void initQRCode() {
        if (bookingProcessingContent != null) {
            imgQRCode = findViewById(R.id.imgQRCode);
            imageData = bookingProcessingContent.getQrCode().toByteArray();

            if (imageData.length != 0) {
                Bitmap imageParkingLot = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                imgQRCode.setImageBitmap(imageParkingLot);
            }
        } else {
            imgQRCode = findViewById(R.id.imgQRCode);

            if (imageData.length != 0) {
                Bitmap imageParkingLot = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                imgQRCode.setImageBitmap(imageParkingLot);
            }
        }
    }


    private void initBookingDetail() {
        txtBookingID = findViewById(R.id.txtBookingID);
        txtCreatedAt = findViewById(R.id.txtCreatedAt);
        txtStatus = findViewById(R.id.txtStatus);
        txtParking = findViewById(R.id.txtParking);
        txtAddress = findViewById(R.id.txtAddress);
        txtLicensePlate = findViewById(R.id.txtLicensePlate);
        iconPendding = findViewById(R.id.iconPendding);
        iconAccept = findViewById(R.id.iconAccept);
        txtLicensePlate.setText(licensePlate.toUpperCase());


        if (bookingProcessingContent != null) {
            txtBookingID.setText(reducedBookingId);
            txtCreatedAt.setText(bookingProcessingContent.getCreatedAt());
            txtParking.setText(parkingLot.getInformation().getName());
            txtAddress.setText(parkingLot.getInformation().getAddress());
            if (accepted) {
                txtStatus.setText("Accepted");
                iconPendding.setVisibility(View.GONE);
                iconAccept.setVisibility(View.VISIBLE);
            } else {
                txtStatus.setText("Processing");
                iconPendding.setVisibility(View.VISIBLE);
                iconAccept.setVisibility(View.GONE);
            }
        } else {
            txtBookingID.setText(reducedBookingId);
            txtParking.setText(parkingLot.getInformation().getName());
            txtAddress.setText(parkingLot.getInformation().getAddress());
            txtCreatedAt.setText(currentBooking.getCreatedAt());
            Log.d("BachMap", "current booking" + currentBooking.getLatestStatus());
            txtStatus.setText(currentBooking.getLatestStatus().equals(BookingStatus.CREATED)
                    ? "Processing"
                    : "Accepted");
            iconPendding.setVisibility(currentBooking.getLatestStatus().equals(BookingStatus.CREATED)
                    ? View.VISIBLE
                    : View.GONE);
            iconAccept.setVisibility(currentBooking.getLatestStatus().equals(BookingStatus.CREATED)
                    ? View.GONE
                    : View.VISIBLE);
        }
    }

    private void initAllButtons() {
        btnimgdirection = findViewById(R.id.imgdirection);
        btnimgcancel = findViewById(R.id.imgcancel);
        btnimgchat = findViewById(R.id.imgchat);
    }

    private void cancelBooking() {
        if (((SaigonParkingApplication) getApplicationContext()).getIsBooked()) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            BookingCancellationContent bookingCancellationContent = BookingCancellationContent.newBuilder()
                    .setBookingId((bookingProcessingContent == null)
                            ? currentBooking.getId()
                            : bookingProcessingContent.getBookingId())
                    .setReason("DON'T WANT TO BOOK")
                    .build();
            SaigonParkingMessage saigonParkingMessage = SaigonParkingMessage.newBuilder()
                    .setReceiverId(id)
                    .setClassification(SaigonParkingMessage.Classification.CUSTOMER_MESSAGE)
                    .setType(SaigonParkingMessage.Type.BOOKING_CANCELLATION)
                    .setTimestamp(timestamp.toString())
                    .setContent(bookingCancellationContent.toByteString())
                    .build();

            sendWebSocketBinaryMessage(saigonParkingMessage);

            //xử lý gọi database
            saigonParkingDatabase.DeleteBookTable();
            ((SaigonParkingApplication) getApplicationContext()).setIsBooked(false);

            //xóa history message
            Paper.book().delete("historymessage");
        }

    }

    public void webSocketSetStateAccept() {
        accepted = true;
    }

    @Override
    public void onBackPressed() {
        if (!((SaigonParkingApplication) getApplicationContext()).getIsBooked()) {
            Intent intent = new Intent(this, MapActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        }
        //Write your code here
        else {
            Toast.makeText(getApplicationContext(), "Back press disabled!", Toast.LENGTH_SHORT).show();
        }
    }
}