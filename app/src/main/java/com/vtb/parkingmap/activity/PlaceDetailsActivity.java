package com.vtb.parkingmap.activity;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bht.saigonparking.api.grpc.booking.BookingRating;
import com.bht.saigonparking.api.grpc.booking.BookingServiceGrpc;
import com.bht.saigonparking.api.grpc.booking.GetAllRatingsOfParkingLotRequest;
import com.bht.saigonparking.api.grpc.booking.ParkingLotBookingAndRatingStatistic;
import com.bht.saigonparking.api.grpc.contact.BookingRequestContent;
import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLot;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotInformation;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotServiceGrpc;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotType;
import com.google.android.material.snackbar.Snackbar;
import com.google.protobuf.BoolValue;
import com.google.protobuf.Empty;
import com.google.protobuf.Int64Value;
import com.vtb.parkingmap.MessageChatAdapter.MessageAdapter;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.activity.commentAdapter.commentAdapter;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.models.Cardcomment;
import com.vtb.parkingmap.models.Photos;
import com.vtb.parkingmap.models.Results;

import java.io.Serializable;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
    private TextView texttotalSlot;
    private RatingBar ratingBar;
    private LinearLayout linearLayoutRating;
    private LinearLayout linearLayoutShowDistanceOnMap;
    private LinearLayout btnimgshow;
    private LinearLayout linearLayoutPrice;
    private TextView txtOpen;
    private TextView txtClose;
    private TextView txtStatus;
    private TextView txtphone;
    private LinearLayout lblPhone;
    private ImageView iconType;
    private TextView output;
    private Broadcast broadcast;

    // variable
    private Results results;
    private ParkingLot parkingLot;
    private ParkingLotBookingAndRatingStatistic ratingStatistic;
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
    private long numberOfRating;
    private byte[] imageData;
    private LinearLayout mainLnLayout;
    private LinearLayout infoLnLayout;
    private LinearLayout ratingLnLayout;

    private OkHttpClient client;
    private RecyclerView recyclerView;

    private MessageAdapter messageAdapter;
    private String bookingId;
    private String bookingreject = null;

    private ViewPager commentViewPaper;
    private ArrayList<Cardcomment> modelArrayList;
    private commentAdapter commentAdapter;
    List<BookingRating> getallcomment;

    private ParkingLotServiceGrpc.ParkingLotServiceBlockingStub parkingLotServiceBlockingStub;
    private BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
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


        parkingLot = (ParkingLot) intent.getSerializableExtra("parkingLot");
        mylat = (double) intent.getSerializableExtra("myLat");
        mylong = (double) intent.getSerializableExtra("myLong");
        position3lat = intent.getDoubleExtra("postion3lat", 1234);
        position3long = intent.getDoubleExtra("postion3long", 1234);
        Log.d("khongbiloi", "Nhan du lieu 2");

        parkingLotServiceBlockingStub = serviceStubs.getParkingLotServiceBlockingStub();
        bookingServiceBlockingStub = serviceStubs.getBookingServiceBlockingStub();

        callApiWithExceptionHandling(() -> {
            ratingStatistic = bookingServiceBlockingStub.getParkingLotBookingAndRatingStatistic(Int64Value.of(parkingLot.getId()));
        });

        processParkingLot();

        loadCardComment();
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
                if (onCheckParkingLotOnline(view)) {
                    showAlertDialog(view);
                }
            } else {
                /* notify user has ongoing booking ! */
                AlertDialog.Builder alert = new AlertDialog.Builder(PlaceDetailsActivity.this);
                alert
                        .setTitle("Booking Ongoing Warning")
                        .setMessage("You have on going booking. Please finish booking before!")
                        .setPositiveButton("OK", null);
                AlertDialog dialog = alert.create();
                dialog.setOnShowListener(dialogInterface -> {
                    Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(v -> {
                        dialog.dismiss();
                    });
                });

                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        //Animation Element on Start Activity
        Animation trans = AnimationUtils.loadAnimation(this, R.anim.fade_item);
        imageView.startAnimation(trans);
        trans = AnimationUtils.loadAnimation(this, R.anim.fade_item_2);
        mainLnLayout.startAnimation(trans);
        trans = AnimationUtils.loadAnimation(this, R.anim.fade_item_3);
        infoLnLayout.startAnimation(trans);
        trans = AnimationUtils.loadAnimation(this, R.anim.fade_item_4);
        ratingLnLayout.startAnimation(trans);
        trans = AnimationUtils.loadAnimation(this, R.anim.fade_item_5);
        commentViewPaper.startAnimation(trans);
    }

    @Override
    protected void onResume() {
        super.onResume();
        lblPhone.setVisibility((txtphone.getText().length() == 0) ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcast);
    }

    private boolean onCheckParkingLotOnline(View v) {
        Log.d("BachMap", "parking lot id: " + id);
        BoolValue.Builder isParkingLotOnline = BoolValue.newBuilder().setValue(false);
        callApiWithExceptionHandling(() -> {
            isParkingLotOnline.setValue(serviceStubs.getContactServiceBlockingStub()
                    .checkParkingLotOnlineByParkingLotId(Int64Value.of(id)).getValue());
        });


        if (!isParkingLotOnline.getValue()) {
            Snackbar.make(v, "Parking Lot is offline", Snackbar.LENGTH_LONG)
                    .setDuration(2000)
                    .show();
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
                .setNegativeButton("No", null);

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
        dialog.setCanceledOnTouchOutside(false);
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
        linearLayoutPrice.setOnClickListener(this::onClickShowPriceOfParking);
    }

    @SuppressLint("SetTextI18n")
    private void loadFormData() {
        txtOpen.setText(openingHour);
        txtClose.setText(closingHour);
        txtphone.setText(phone);

        switch (type) {
            case STREET:
                iconType.setImageResource(R.drawable.plstreet);
                tmpType = ParkingLotType.STREET_VALUE;
                break;
            case PRIVATE:
                iconType.setImageResource(R.drawable.plprivate);
                tmpType = ParkingLotType.PRIVATE_VALUE;
                break;
            case BUILDING:
                iconType.setImageResource(R.drawable.plbuilding);
                tmpType = ParkingLotType.BUILDING_VALUE;
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
        textViewRating.setText(String.format(Locale.US, "%.1f", ratingAverage));
        ratingBar.setRating((float) ratingAverage);
        textViewAddress.setText(address);
        textViewAvailability.setText(String.format(Locale.ENGLISH, "%d", availableSlot));
        texttotalSlot.setText(String.format(Locale.ENGLISH, "%d", totalSlot));
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


        ratingAverage = ratingStatistic.getRatingAverage();
        numberOfRating = ratingStatistic.getNRating();

        imageData = information.getImageData().toByteArray();
    }


    /**
     * on click show distance on map
     */

    /**
     * on click show parking's price
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

    private void onClickShowPriceOfParking(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(PlaceDetailsActivity.this);
        alert
                .setTitle("Parking Unit Price")
                .setMessage("First hour: 25.000VND/h\n" +
                        "Next two hours: 20.000VND/h\n" +
                        "Following hours: 15.000VND/h")
                .setPositiveButton("OK", null);
        AlertDialog dialog = alert.create();
        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(v -> {
                dialog.dismiss();
            });
        });

        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(getResources().getColor(R.color.colorPrimary));
    }

    private void init() {
        imageView = findViewById(R.id.imageView);
        btnimgshow = findViewById(R.id.imgshow);
        lblPhone = findViewById(R.id.lblPhone);

        linearLayoutRating = findViewById(R.id.linearLayoutRating);
        linearLayoutShowDistanceOnMap = findViewById(R.id.linearLayoutShowDistanceOnMap);
        linearLayoutPrice = findViewById(R.id.imgPrice);
        textViewName = findViewById(R.id.textViewName);
        textViewRating = findViewById(R.id.textViewRating);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewAvailability = findViewById(R.id.textViewAvailability);
        texttotalSlot = findViewById(R.id.TotalSlot);
        ratingBar = findViewById(R.id.ratingBar);
        mainLnLayout = findViewById(R.id.mainLnLayout);
        infoLnLayout = findViewById(R.id.infoLnLayout);
        ratingLnLayout = findViewById(R.id.ratingLnLayout);

        txtOpen = findViewById(R.id.txtOpen);
        txtClose = findViewById(R.id.txtClose);
        txtStatus = findViewById(R.id.txtStatus);
        txtphone = findViewById(R.id.txtphone);
        iconType = findViewById(R.id.iconType);

        commentViewPaper = findViewById(R.id.commentViewPaper);

        commentViewPaper.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void loadCardComment() {
        modelArrayList = new ArrayList<>();

        GetAllRatingsOfParkingLotRequest getAllRatingsOfParkingLotRequest = GetAllRatingsOfParkingLotRequest
                .newBuilder()
                .setParkingLotId(parkingLot.getId())
                .setNRow(10)
                .setPageNumber(1)
                .build();

        callApiWithExceptionHandling(() -> {
            getallcomment = bookingServiceBlockingStub.getAllRatingsOfParkingLot(getAllRatingsOfParkingLotRequest).getRatingList();
        });

        for (BookingRating bookingrating : getallcomment) {
            modelArrayList.add(new Cardcomment(
                    bookingrating.getUsername(),
                    bookingrating.getComment(),
                    bookingrating.getLastUpdated(),
                    bookingrating.getRating()
            ));
        }

        commentAdapter = new commentAdapter(this, modelArrayList);
        commentViewPaper.setOffscreenPageLimit(10);
        commentViewPaper.setAdapter(commentAdapter);
        commentViewPaper.measure(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    public void funcXemChiTietDanhGia(View view) {
        Intent intent = new Intent(PlaceDetailsActivity.this, CommentRatingActivity.class);
        intent.putExtra("idplacedetail", (Serializable) id);
        intent.putExtra("parkinglot", (Serializable) parkingLot);
        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(imageView, "imagePlaceDetail"));
        startActivityWithLoadingAndOption(intent, options);
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

