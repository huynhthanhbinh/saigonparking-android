package com.vtb.parkingmap.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.contact.BookingAcceptanceContent;
import com.bht.saigonparking.api.grpc.contact.BookingCancellationContent;
import com.bht.saigonparking.api.grpc.contact.BookingRejectContent;
import com.bht.saigonparking.api.grpc.contact.BookingRequestContent;
import com.bht.saigonparking.api.grpc.contact.NotificationContent;
import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.bht.saigonparking.api.grpc.contact.TextMessageContent;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLot;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotInformation;
import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotType;
import com.google.gson.Gson;
import com.google.protobuf.Int64Value;
import com.vtb.parkingmap.BuildConfig;
import com.vtb.parkingmap.MessageChatAdapter.MessageAdapter;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.database.SaigonParkingDatabaseEntity;
import com.vtb.parkingmap.models.Photos;
import com.vtb.parkingmap.models.Results;

import org.json.JSONObject;

import java.io.Serializable;
import java.net.URI;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.paperdb.Paper;
import lombok.SneakyThrows;
import lombok.ToString;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;


@SuppressWarnings({"unused", "FieldCanBeLocal"})
public final class PlaceDetailsActivity extends BaseSaigonParkingActivity {

    private ImageView imageView;
    private ImageView btnimgdirection;
    private ImageView btnimgshow;
    private ImageView btnimgphone;
    private ImageView btnimgcancel;
    private Photos photos;
    private TextView textViewName;
    private TextView textViewRating;
    private TextView textViewAddress;
    private TextView textViewAvailability;
    private RatingBar ratingBar;
    private LinearLayout linearLayoutRating;
    private LinearLayout linearLayoutShowOnMap;
    private LinearLayout linearLayoutShowDistanceOnMap;
    private LinearLayout linearLayoutDrawDirection;
    private TextView txtOpen;
    private TextView txtClose;
    private TextView txtStatus;
    private TextView txtphone;
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
    int tmptype;
    final int deltaHour = 1;

    //parking-lot
    private long id;
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
    //websocket
    private static final URI WEB_SOCKET_LOCAL_URI = URI.create("ws://192.168.0.103:8000/contact");
    private WebSocket webSocket;
    private OkHttpClient client;
    private WebSocket ws;
    private String SERVER_PATH = BuildConfig.WEBSOCKET_PREFIX + BuildConfig.GATEWAY_HOST + ":" + BuildConfig.GATEWAY_HTTP_PORT + "/contact";
    private RecyclerView recyclerView;

    private MessageAdapter messageAdapter;
    private String bookingid = null;
    private String bookingreject = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("khongbiloi", "Nhan du lieu");
        setContentView(R.layout.activity_place_details);
        init();
        initiateSocketConnection();
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
        //ping ping client
        btnimgcancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelbooking();
            }
        });
        btnimgdirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                onClickDrawDirection(view);
            }
        });
        btnimgshow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendbooking();
            }
        });
        btnimgphone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PlaceDetailsActivity.this, ChatActivity.class);

                startActivity(intent);
            }
        });

    }

    private void processParkingLot() {
        if (parkingLot != null) {
            assignParkingLotFields();
            loadFormData();
            initEventListeners();
        }
    }

    private void initEventListeners() {
        linearLayoutShowDistanceOnMap.setOnClickListener(this::onClickShowDistanceOnMap);
        linearLayoutDrawDirection.setOnClickListener(this::onClickDrawDirection);
        linearLayoutShowOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelbooking();
            }
        });
    }

    private void loadFormData() {
        txtOpen.setText(openingHour);
        txtClose.setText(closingHour);
        txtphone.setText(phone);

        switch (type) {
            case STREET:
                iconType.setImageResource(R.drawable.plstreet);
                tmptype = 2;
                break;
            case PRIVATE:
                iconType.setImageResource(R.drawable.plprivate);
                tmptype = 1;
                break;
            case BUILDING:
                iconType.setImageResource(R.drawable.plbuilding);
                tmptype = 0;
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
        textViewRating.setText(String.valueOf(ratingAverage));
        ratingBar.setRating((float) ratingAverage);
        textViewAddress.setText(address);
        textViewAvailability.setText(String.format(Locale.ENGLISH, "%d/%d", availableSlot, totalSlot));
        txtStatus.setText("Active");
    }

    private void assignParkingLotFields() {

        ParkingLot tmpparkinglot = serviceStubs.getParkingLotServiceBlockingStub().getParkingLotById(Int64Value.of(parkingLot.getId()));

        id = tmpparkinglot.getId();
        type = tmpparkinglot.getType();
        latitude = tmpparkinglot.getLatitude();
        longitude = tmpparkinglot.getLongitude();
        openingHour = tmpparkinglot.getOpeningHour();
        closingHour = tmpparkinglot.getClosingHour();
        availableSlot = tmpparkinglot.getAvailableSlot();
        totalSlot = tmpparkinglot.getTotalSlot();

        ParkingLotInformation information = tmpparkinglot.getInformation();

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
    private void onClickDrawDirection(View view) {
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

        Intent intent = new Intent(PlaceDetailsActivity.this, ViewDrawDirectionActivity.class);
        intent.putExtra("placedetaillat", (Serializable) latitude);
        intent.putExtra("placedetaillong", (Serializable) longitude);
        intent.putExtra("mylatfromplacedetail", (Serializable) mylat);
        intent.putExtra("mylongfromplacedetail", (Serializable) mylong);
        intent.putExtra("placedetailtype", (Serializable) tmptype);
        intent.putExtra("idplacedetail", (Serializable) id);
        double tmp = 1234;
        if (position3lat != tmp) {
            intent.putExtra("position3lat", (Serializable) position3lat);
            intent.putExtra("position3long", (Serializable) position3long);
        }
        startActivity(intent);
    }

    /**
     * on click show distion on map
     */
    private void onClickShowDistanceOnMap(View view) {


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
        btnimgdirection = findViewById(R.id.imgdirection);
        btnimgcancel = findViewById(R.id.imgcancel);
        btnimgshow = findViewById(R.id.imgshow);
        if (!saigonParkingDatabase.getCurrentBookingEntity().equals(SaigonParkingDatabaseEntity.DEFAULT_INSTANCE)) {
            btnimgshow.setVisibility(View.INVISIBLE);
            btnimgcancel.setVisibility(View.VISIBLE);
        }

        btnimgphone = findViewById(R.id.imgphone);
        linearLayoutRating = findViewById(R.id.linearLayoutRating);
        linearLayoutShowOnMap = findViewById(R.id.linearLayoutShowOnMap);
        linearLayoutShowDistanceOnMap = findViewById(R.id.linearLayoutShowDistanceOnMap);
        textViewName = findViewById(R.id.textViewName);
        textViewRating = findViewById(R.id.textViewRating);
        textViewAddress = findViewById(R.id.textViewAddress);
        textViewAvailability = findViewById(R.id.textViewAvailability);
        ratingBar = findViewById(R.id.ratingBar);
        linearLayoutDrawDirection = findViewById(R.id.linearLayoutDrawDirection);

        txtOpen = findViewById(R.id.txtOpen);
        txtClose = findViewById(R.id.txtClose);
        txtStatus = findViewById(R.id.txtStatus);
        txtphone = findViewById(R.id.txtphone);
        iconType = findViewById(R.id.iconType);
        output = findViewById(R.id.txtlastupdate);
        txtXemChiTiet = findViewById(R.id.txtXemChiTiet);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            Log.d("back", "nut back da nhan");
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void funcXemChiTietDanhGia(View view) {
        Intent intent = new Intent(PlaceDetailsActivity.this, CommentRatingActivity.class);
        intent.putExtra("idplacedetail", (Serializable) id);
        startActivity(intent);
        Toast.makeText(PlaceDetailsActivity.this, "hết giờ nha  ", Toast.LENGTH_SHORT).show();
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

    private void initiateSocketConnection() {
        String token = saigonParkingDatabase.getAuthKeyValueMap().get(SaigonParkingDatabase.ACCESS_TOKEN_KEY);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(SERVER_PATH)
                .addHeader("Authorization", token)
                .build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        webSocket = client.newWebSocket(request, listener);

    }

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {

        }

        @ToString
        private class BachMap {
            private String message;
        }

        @SneakyThrows
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            JSONObject jsonObject = new JSONObject(text);


            output("Receiving : " + jsonObject.getString("message"));

            BachMap bachMap = new Gson().fromJson(text, BachMap.class);
//            Log.d("BachMap", bachMap.toString());
            Log.d("BachMap", bachMap.message);
        }

        @SneakyThrows
        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            SaigonParkingMessage message = SaigonParkingMessage.parseFrom(bytes.toByteArray());
            runOnUiThread(() -> {
                try {
                    switch (message.getType()) {
                        case NOTIFICATION:
                            NotificationContent notificationContent = NotificationContent.parseFrom(message.getContent());
                            Log.d("BachMap", "Ket qua:" + notificationContent);
                            break;
                        case TEXT_MESSAGE:
                            if (!saigonParkingDatabase.getCurrentBookingEntity().equals(SaigonParkingDatabaseEntity.DEFAULT_INSTANCE)) {
                                TextMessageContent textMessageContent = TextMessageContent.parseFrom(message.getContent());
                                Log.d("BachMap", "1" + textMessageContent);
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.put("name", textMessageContent.getSender());
                                jsonObject.put("message", textMessageContent.getMessage());
                                jsonObject.put("isSent", false);

                                messageAdapter.addItem(jsonObject);

                                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

                            }


                            break;
                        case BOOKING_ACCEPTANCE:
                            BookingAcceptanceContent bookingAcceptanceContent = BookingAcceptanceContent.parseFrom(message.getContent());
                            bookingid = bookingAcceptanceContent.getBookingId();
                            Log.d("BachMap", "1 : BOOKING ACC: " + bookingAcceptanceContent.getBookingId());
                            bookingid = bookingAcceptanceContent.getBookingId();
                            double tmp = 1234;

                            Log.d("BachMap", "onMessage: BachMap vao ");
                            SaigonParkingDatabaseEntity bookingEntity = SaigonParkingDatabaseEntity
                                    .builder()
                                    .id(id)
                                    .latitude(latitude)
                                    .longitude(longitude)
                                    .mylat(mylat)
                                    .mylong(mylong)
                                    .position3lat(position3lat)
                                    .position3long(position3long)
                                    .tmptype(tmptype)
                                    .bookingid(bookingAcceptanceContent.getBookingId())
                                    .build();


                            Log.d("BachMap", bookingEntity.toString());
                            saigonParkingDatabase.InsertBookingTable(bookingEntity);


//                            if (position3lat != tmp) {
//                                Log.d("BachMap", "onMessage: BachMap vao ");
//                                SaigonParkingDatabaseEntity bookingEntity = SaigonParkingDatabaseEntity
//                                        .builder()
//                                        .id(id)
//                                        .latitude(latitude)
//                                        .longitude(longitude)
//                                        .mylat(mylat)
//                                        .mylong(mylong)
//                                        .position3lat(position3lat)
//                                        .position3long(position3long)
//                                        .tmptype(tmptype)
//                                        .build();
//                                saigonParkingDatabase.InsertBookingTable(bookingEntity);
//                            }
//                            else
//                            {
//                                SaigonParkingDatabaseEntity bookingEntity = SaigonParkingDatabaseEntity
//                                        .builder()
//                                        .id(id)
//                                        .latitude(latitude)
//                                        .longitude(longitude)
//                                        .mylat(mylat)
//                                        .mylong(mylong)
//                                        .position3lat(1234)
//                                        .position3long(1234)
//                                        .tmptype(tmptype)
//                                        .build();
//                                saigonParkingDatabase.InsertBookingTable(bookingEntity);
//                            }


                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    btnimgshow.setVisibility(View.INVISIBLE);
                                    btnimgcancel.setVisibility(View.VISIBLE);
                                }
                            });
                            break;
                        case BOOKING_REJECT:
                            BookingRejectContent bookingRejectContent = BookingRejectContent.parseFrom(message.getContent());
                            Log.d("BachMap", "1 : BOOKING REJ" + bookingRejectContent);
                            break;
                        case IMAGE:

                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            });

        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            output("Closing : " + code + " / " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            output("Error : " + t.getMessage());
            Log.d("BachMap", t.getMessage());
        }
    }

    private void start() {
//        String token = saigonParkingDatabase.getKeyValueMap().get(SaigonParkingDatabase.ACCESS_TOKEN_KEY);
//        Request request = new Request.Builder()
//                .url("ws://192.168.0.102:8000/contact")
//                .addHeader("Authorization", token)
//
//
//                .build();
//        EchoWebSocketListener listener = new EchoWebSocketListener();
//        ws = client.newWebSocket(request, listener);
//
//        client.dispatcher().executorService().shutdown();
//
//        ws.send("xin chao Bach Dep Trai");
    }

    private void output(String txt) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                output.setText(output.getText().toString() + "\n\n" + txt);
            }
        });
    }

    private void cancelbooking() {
        long tmpid = serviceStubs.getParkingLotServiceBlockingStub().getParkingLotEmployeeIdOfParkingLot(Int64Value.of(id)).getValue();
        if (!saigonParkingDatabase.getCurrentBookingEntity().equals(SaigonParkingDatabaseEntity.DEFAULT_INSTANCE)) {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            BookingCancellationContent bookingCancellationContent = BookingCancellationContent.newBuilder()
                    .setBookingId(saigonParkingDatabase.getBookingEntity().getBookingid())
                    .setReason("Khong thich dat nua")
                    .build();
            SaigonParkingMessage saigonParkingMessage = SaigonParkingMessage.newBuilder()
                    .setSenderId(3)
                    .setReceiverId(32)
                    .setClassification(SaigonParkingMessage.Classification.CUSTOMER_MESSAGE)
                    .setType(SaigonParkingMessage.Type.BOOKING_CANCELLATION)
                    .setTimestamp(timestamp.toString())
                    .setContent(bookingCancellationContent.toByteString())
                    .build();
            webSocket.send(new ByteString(saigonParkingMessage.toByteArray()));
            //xử lý gọi database
            saigonParkingDatabase.DeleteBookTable(parkingLot.getId());

            //xóa history message
            Paper.book().delete("historymessage");
            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    btnimgshow.setVisibility(View.VISIBLE);
                    btnimgcancel.setVisibility(View.INVISIBLE);

                }
            });
            Log.d("BachMap", "Gửi request CAMCELATION");
        }


    }

    private void sendbooking() {

        long tmpid = serviceStubs.getParkingLotServiceBlockingStub().getParkingLotEmployeeIdOfParkingLot(Int64Value.of(id)).getValue();

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        BookingRequestContent bookingRequestContent = BookingRequestContent.newBuilder()
                .setCustomerName(saigonParkingDatabase.getAuthKeyValueMap().get(SaigonParkingDatabase.USERNAME_KEY))
                .setCustomerLicense("9954")
                .setAmountOfParkingHour(3)
                .build();
        SaigonParkingMessage saigonParkingMessage = SaigonParkingMessage.newBuilder()
                .setSenderId(3)
                .setReceiverId(32)
                .setClassification(SaigonParkingMessage.Classification.CUSTOMER_MESSAGE)
                .setType(SaigonParkingMessage.Type.BOOKING_REQUEST)
                .setTimestamp(timestamp.toString())
                .setContent(bookingRequestContent.toByteString())
                .build();
        webSocket.send(new ByteString(saigonParkingMessage.toByteArray()));
        Log.d("BachMap", "Gửi request Booking");
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
//        textViewName.setText(results.getName());
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

