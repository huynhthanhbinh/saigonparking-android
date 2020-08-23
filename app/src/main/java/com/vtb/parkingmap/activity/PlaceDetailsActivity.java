package com.vtb.parkingmap.activity;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.contact.BookingCancellationContent;
import com.bht.saigonparking.api.grpc.contact.BookingRequestContent;
import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLot;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotInformation;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotType;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.vtb.parkingmap.MessageChatAdapter.MessageAdapter;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.database.SaigonParkingDatabaseEntity;
import com.vtb.parkingmap.models.Photos;
import com.vtb.parkingmap.models.Results;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import io.paperdb.Paper;
import lombok.Getter;
import okhttp3.OkHttpClient;

@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public final class PlaceDetailsActivity extends BaseSaigonParkingActivity {

    private ImageView imageView;
    private ImageView btnimgchat;
    private ImageView btnimgcancel;
    private Photos photos;
    private TextView textViewName;
    private TextView textViewRating;
    private TextView textViewAddress;
    private TextView textViewAvailability;
    private RatingBar ratingBar;
    private LinearLayout linearLayoutRating;
    private LinearLayout linearLayoutShowDistanceOnMap;
    private LinearLayout btnimgshow;
    private TextView txtOpen;
    private TextView txtClose;
    private TextView txtStatus;
    private TextView txtphone;
    private TextView lblPhone;
    private ImageView iconType;
    private TextView output;
    private TextView txtXemChiTiet;
    private Broadcast broadcast;

    // variable
    private Results results;
    private ParkingLot parkingLot;
    double mylat;
    double mylong;
    double position3lat;
    double position3long;
    int tmpType;
    final int deltaHour = 1;

    //parking-lot
    private long id;
    private EditText txtlicensePlate;
    private EditText txtAmountOfParkingHour;
    private String licensePlate;
    private String amountOfParkingHourString;
    private ParkingLotType type;
    private double latitude;
    private double longitude;
    private String openingHour;
    private String closingHour;
    private int availableSlot;
    private int totalSlot;
    private String name;
    private String phone;
    private String address;
    private double ratingAverage;
    private int numberOfRating;
    private byte[] imageData;

    private OkHttpClient client;
    private RecyclerView recyclerView;

    private MessageAdapter messageAdapter;
    private String bookingId;
    private String bookingreject = null;
    private int LAUNCH_SECOND_ACTIVITY = 1;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d("khongbiloi", "Nhan du lieu");
        setContentView(R.layout.activity_place_details);
        // tạo thông báo
        createNotificationChannel();

        init();
        //lam hotel
//        Bundle bundle = getIntent().getExtras();
        //
        client = new OkHttpClient();

        //
        broadcast = new Broadcast();
        IntentFilter filter = new IntentFilter("parkinglot_broadcast");
        registerReceiver(broadcast, filter);
        // lam phần parking map

        Intent intent = getIntent();
        parkingLot = (ParkingLot) intent.getSerializableExtra("parkingLot");
        mylat = (double) intent.getSerializableExtra("myLat");
        mylong = (double) intent.getSerializableExtra("myLong");
        position3lat = intent.getDoubleExtra("postion3lat", 1234);
        position3long = intent.getDoubleExtra("postion3long", 1234);
        Log.d("khongbiloi", "Nhan du lieu 2");

        processParkingLot();


        btnimgshow.setOnClickListener(view -> {
            BoolValue.Builder isCustomerHasOnGoingBooking = BoolValue.newBuilder();
            callApiWithExceptionHandling(() -> {
                isCustomerHasOnGoingBooking.setValue(serviceStubs
                        .getBookingServiceBlockingStub()
                        .checkCustomerHasOnGoingBooking(Empty.getDefaultInstance())
                        .getValue());
            });

            if (!isCustomerHasOnGoingBooking.getValue()) {
                /* kiem tra bai xe co online hay khong */
                if (onCheckParkingLotOnline()) {
                    showAlertDialog(view);
                }
            } else {
                /* notify user has ongoing booking ! */
                AlertDialog.Builder alert = new AlertDialog.Builder(PlaceDetailsActivity.this);
                alert
                        .setTitle("Booking Ongoing Warning")
                        .setMessage("You have on going booking !")
                        .setNegativeButton("OK", (dialogInterface, i) ->
                                Toast.makeText(PlaceDetailsActivity.this,
                                        "Please complete booking before!", Toast.LENGTH_SHORT).show());

                AlertDialog dialog = alert.create();
                dialog.show();

                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources()
                                .getColor(R.color.colorPrimary));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        lblPhone.setVisibility((txtphone.getText().length() == 0) ? View.INVISIBLE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast);
    }

    private boolean onCheckParkingLotOnline() {
        Log.d("BachMap", "parking lot id: " + id);
        BoolValue.Builder isParkingLotOnline = BoolValue.newBuilder().setValue(false);
        callApiWithExceptionHandling(() -> {
            isParkingLotOnline.setValue(serviceStubs.getContactServiceBlockingStub()
                    .checkParkingLotOnlineByParkingLotId(Int64Value.of(id)).getValue());
        });


        if (!isParkingLotOnline.getValue()) {
            Toast.makeText(PlaceDetailsActivity.this, "Parking are not available!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    public void showAlertDialog(View view) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        view = inflater.inflate(R.layout.booking_dialog, null);

        alert.setView(view)
                .setTitle("Booking Confirm")
                .setMessage("Please input your License Plate and Parking Hour!")
                .setPositiveButton("Yes", null)
                .setNegativeButton("No", (dialogInterface, i) ->
                        Toast.makeText(PlaceDetailsActivity.this, "Cancel booking successfully!", Toast.LENGTH_SHORT).show());

        txtlicensePlate = view.findViewById(R.id.txtlicensePlate);
        txtAmountOfParkingHour = view.findViewById(R.id.txtAmountOfParkingHour);
        AlertDialog dialog = alert.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view1 -> {
                licensePlate = txtlicensePlate.getText().toString();
                amountOfParkingHourString = txtAmountOfParkingHour.getText().toString();

                Log.d("BachMap", " " + licensePlate);
                Log.d("BachMap", " " + amountOfParkingHourString);

                boolean isAmountOfParkingLotHourCorrect = false;
                boolean isNumberLicensePlateCorrect = isNumberLicensePlateCorrect(licensePlate);
                double amountOfParkingHour = 0.0;

                if (!isNumberLicensePlateCorrect) {
                    Toast.makeText(PlaceDetailsActivity.this, "License Plate Invalid!", Toast.LENGTH_SHORT).show();
                }

                try {
                    amountOfParkingHour = Double.valueOf(amountOfParkingHourString);
                    isAmountOfParkingLotHourCorrect = true;
                    /*1-5 hour*/
                    if (amountOfParkingHour > 5 || amountOfParkingHour < 1) {
                        isAmountOfParkingLotHourCorrect = false;
                        Toast.makeText(PlaceDetailsActivity.this, "Your Parking Hour must be 1->5 hour", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(PlaceDetailsActivity.this, "Parking Hour Invalid!", Toast.LENGTH_SHORT).show();
                }


                //Dismiss once everything is OK.
                if (isNumberLicensePlateCorrect && isAmountOfParkingLotHourCorrect) {
                    sendBooking(licensePlate, amountOfParkingHour);
                    dialog.dismiss();
                    Toast.makeText(PlaceDetailsActivity.this, "Booking successfully!", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    /* 59B-66059 */
    private boolean isNumberLicensePlateCorrect(String numberLicenseString) {
        String alternativeString = numberLicenseString.replace(".", "");
        return alternativeString.matches("^[0-9]{1,2}[A-Za-z]-[0-9]{4,5}$");
    }


    private void processParkingLot() {
        if (parkingLot != null) {
            assignParkingLotFields();
            loadFormData();
            initEventListeners();
        }
    }

    private void initEventListeners() {
        linearLayoutShowDistanceOnMap.setOnClickListener(this::onClickShowDistanceOnGoogleMap);
    }

    @SuppressLint("SetTextI18n")
    private void loadFormData() {
        txtOpen.setText(openingHour);
        txtClose.setText(closingHour);
        txtphone.setText(phone);

        switch (type) {
            case STREET:
                iconType.setImageResource(R.drawable.plstreet);
                tmpType = 2;
                break;
            case PRIVATE:
                iconType.setImageResource(R.drawable.plprivate);
                tmpType = 1;
                break;
            case BUILDING:
                iconType.setImageResource(R.drawable.plbuilding);
                tmpType = 0;
                break;
            default:
                break;
        }

        if (imageData.length != 0) { // co hinh trong db --> load hinh moi
            Bitmap imageParkingLot = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
            imageView.setImageBitmap(imageParkingLot);
        }

        textViewName.setText(name);
        linearLayoutRating.setVisibility(View.VISIBLE);
        textViewRating.setText(String.format(Locale.US, "%.2f", ratingAverage));
        ratingBar.setRating((float) ratingAverage);
        textViewAddress.setText(address);
        textViewAvailability.setText(String.format(Locale.ENGLISH, "%d", availableSlot));
        txtStatus.setText("Active");
    }

    private void assignParkingLotFields() {
        id = parkingLot.getId();
        type = parkingLot.getType();
        latitude = parkingLot.getLatitude();
        longitude = parkingLot.getLongitude();
        openingHour = parkingLot.getOpeningHour();
        closingHour = parkingLot.getClosingHour();
        availableSlot = parkingLot.getAvailableSlot();
        totalSlot = parkingLot.getTotalSlot();

        ParkingLotInformation information = parkingLot.getInformation();

        name = information.getName();
        phone = information.getPhone();
        address = information.getAddress();
        ratingAverage = information.getRatingAverage();
        numberOfRating = information.getNumberOfRating();

        imageData = information.getImageData().toByteArray();
    }

    /**
     * on click drawn direction
     */


    /**
     * on click show distion on map
     */
    private void onClickShowDistanceOnGoogleMap(View view) {


        Date d = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
        String currentDateTimeString = sdf.format(d);

        Time currentTime = Time.valueOf(currentDateTimeString);
        Time closingTime = Time.valueOf(closingHour);

        boolean check = (currentTime.getTime() + deltaHour * 60 * 60) - closingTime.getTime() > 0;

        if (check) {
            Toast.makeText(PlaceDetailsActivity.this, " Quá giờ rồi Không còn nhận xe nhé", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
        Log.d("TestDiaDiemMapChiDuong", " " + latitude + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        startActivity(mapIntent);

    }

    private void init() {
        imageView = findViewById(R.id.imageView);
        btnimgshow = findViewById(R.id.imgshow);
        lblPhone = findViewById(R.id.lblPhone);
        //đã có dự lieu booking
//        if (!saigonParkingDatabase.getCurrentBookingEntity().equals(SaigonParkingDatabaseEntity.DEFAULT_INSTANCE)) {
//            // hien nut cancel + chat + huong di bai xe ( flagbook = false )
//            btnimgshow.setVisibility(View.INVISIBLE);
//            flagbook = false;
//            btnimgcancel.setVisibility(View.VISIBLE);
//            btnimgchat.setVisibility(View.VISIBLE);
//            btnimgdirection.setVisibility(View.VISIBLE);
//
//        } else {//chua co
//            // hien nut booking thoi
//            btnimgshow.setVisibility(View.VISIBLE);
//            flagbook = true;
//            btnimgchat.setVisibility(View.INVISIBLE);
//            btnimgdirection.setVisibility(View.INVISIBLE);
//            btnimgcancel.setVisibility(View.INVISIBLE);
//        }


        linearLayoutRating = findViewById(R.id.linearLayoutRating);
        linearLayoutShowDistanceOnMap = findViewById(R.id.linearLayoutShowDistanceOnMap);
        textViewName = findViewById(R.id.textViewName);
        textViewRating = findViewById(R.id.textViewRating);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewAvailability = findViewById(R.id.textViewAvailability);
        ratingBar = findViewById(R.id.ratingBar);


        txtOpen = findViewById(R.id.txtOpen);
        txtClose = findViewById(R.id.txtClose);
        txtStatus = findViewById(R.id.txtStatus);
        txtphone = findViewById(R.id.txtphone);
        iconType = findViewById(R.id.iconType);
        txtXemChiTiet = findViewById(R.id.txtXemChiTiet);
    }


    public void funcXemChiTietDanhGia(View view) {
        progressDialog = new ProgressDialog(PlaceDetailsActivity.this,
                R.style.AppTheme_Dark_Dialog);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        Intent intent = new Intent(PlaceDetailsActivity.this, CommentRatingActivity.class);
        intent.putExtra("idplacedetail", (Serializable) id);
        intent.putExtra("parkinglot", (Serializable) parkingLot);
        startActivityForResult(intent, LAUNCH_SECOND_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == LAUNCH_SECOND_ACTIVITY) {
            if (resultCode == CommentRatingActivity.RESULT_OK) {
                progressDialog.dismiss();
            }
            if (resultCode == CommentRatingActivity.RESULT_CANCELED) {
            }
        }
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

    private void cancelbooking() {
        if (!saigonParkingDatabase.getCurrentBookingEntity().equals(SaigonParkingDatabaseEntity.DEFAULT_INSTANCE)) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            BookingCancellationContent bookingCancellationContent = BookingCancellationContent.newBuilder()
                    .setBookingId(saigonParkingDatabase.getBookingEntity().getBookingId())
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

            runOnUiThread(() -> {
                btnimgshow.setVisibility(View.VISIBLE);
                btnimgcancel.setVisibility(View.INVISIBLE);
                btnimgchat.setVisibility(View.INVISIBLE);
            });


//            btnimgshow.setEnabled(true);
//            btnimgcancel.setEnabled(false);
//            btnimgdirection.setEnabled(false);
//            btnimgphone.setEnabled(false);
//            Log.d("BachMap", "Gửi request CAMCELATION");
        }
    }


    public void sendBooking(String numberLicensePlate, double amountOfParkingHour) {

        //flagbook = true nghĩa là CHƯA có dữ liệu bôking
        //b1 hiện dialog xác nhận hoặc hủy
        // xác nhận thì => vào if flagbook == true
        // huy thì k có gì hết

        if (!((SaigonParkingApplication) getApplicationContext()).getIsBooked()) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            BookingRequestContent bookingRequestContent = BookingRequestContent.newBuilder()
                    .setCustomerName(Objects.requireNonNull(saigonParkingDatabase
                            .getAuthKeyValueMap().get(SaigonParkingDatabase.USERNAME_KEY)))
                    .setCustomerLicense(numberLicensePlate)
                    .setAmountOfParkingHour(amountOfParkingHour)
                    .setParkingLotId(id)
                    .build();


            SaigonParkingMessage saigonParkingMessage = SaigonParkingMessage.newBuilder()
                    .setReceiverId(id)
                    .setClassification(SaigonParkingMessage.Classification.CUSTOMER_MESSAGE)
                    .setType(SaigonParkingMessage.Type.BOOKING_REQUEST)
                    .setTimestamp(timestamp.toString())
                    .setContent(bookingRequestContent.toByteString())
                    .build();

            sendWebSocketBinaryMessage(saigonParkingMessage);
            ((SaigonParkingApplication) getApplicationContext()).setIsBooked(true);
            Log.d("BachMap", "Gửi request Booking");
        }
    }


    //Tạo chanel thông báo (Dùng cho android api 26 trở lên)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("ID_Notification", "TEST", NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("THONG BAO HET CHO");
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    // làm về hotel

//
//        if (bundle != null) {
//           results = (Results) bundle.getSerializable("result");
//
//
//
//            //Toast.makeText(this, String.valueOf(results.getPhotos()[0].getPhoto_reference()), Toast.LENGTH_SHORT).show();
//        } else {
//            Toast.makeText(this, "Got Nothing!!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//        imageView = findViewById(R.id.imageView);
//
//        try {
//            // get photo
//            photos = results.getPhotos()[0];
//            String photoUrl = String.format("https://maps.googleapis.com/maps/api/place/photo?maxwidth=%s&photoreference=%s&key=%s", 400, photos.getPhoto_reference(), getResources().getString(R.string.google_maps_api));
//            Log.d("photoUrl", photoUrl);
//            Picasso
//                    .get()
//                    .load(photoUrl)
//                    .into(imageView);
//        } catch (Exception e) {
//            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
//            Picasso
//                    .get()
//                    .load(R.drawable.ic_error_image)
//                    .into(imageView);
//        }
//
//        tc.setText(results.getFirstName());
//        textViewAddress.setText(results.getVicinity());
//        // check if ratings is available for the place
//        if (results.getRating() != null) {
//            linearLayoutRating.setVisibility(View.VISIBLE);
//            textViewRating.setText(results.getRating());
//            ratingBar.setRating(Float.valueOf(results.getRating()));
//        }
//        // check if opening hours is available
//        if (results.getOpeningHours() != null) {
//            textViewAvailability.setText(results.getOpeningHours().getOpenNow() == false ? "Close now" : "Open now");
//        } else {
//            textViewAvailability.setText("Not found!");
//        }
//
//        linearLayoutShowOnMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(PlaceDetailsActivity.this, ViewImage.class);
//                intent.putExtra("result", results);
//
//                intent.putExtra("type", "map");
//                startActivity(intent);
//            }
//        });
//
//        linearLayoutShowDistanceOnMap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Double.parseDouble(results.getGeometry().getLocation().getLat()) + "," + Double.parseDouble(results.getGeometry().getLocation().getLng()));
//                Log.d("TestDiaDiemMapChiDuong"," "+Double.parseDouble(results.getGeometry().getLocation().getLat())+Double.parseDouble(results.getGeometry().getLocation().getLng()));
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                mapIntent.setPackage("com.google.android.apps.maps");
//
//                    startActivity(mapIntent);
//            }
//        });
}

