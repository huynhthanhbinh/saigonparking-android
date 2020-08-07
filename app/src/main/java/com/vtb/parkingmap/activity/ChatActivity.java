package com.vtb.parkingmap.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.contact.BookingAcceptanceContent;
import com.bht.saigonparking.api.grpc.contact.BookingRejectContent;
import com.bht.saigonparking.api.grpc.contact.NotificationContent;
import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.bht.saigonparking.api.grpc.contact.TextMessageContent;
import com.vtb.parkingmap.BuildConfig;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabase;
import com.vtb.parkingmap.database.SaigonParkingDatabaseEntity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Timestamp;

import io.paperdb.Paper;
import lombok.SneakyThrows;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public final class ChatActivity extends BaseSaigonParkingActivity implements TextWatcher {

    private String name;
    private String SERVER_PATH = BuildConfig.WEBSOCKET_PREFIX + BuildConfig.GATEWAY_HOST + ":" + BuildConfig.GATEWAY_HTTP_PORT + "/contact";
    private EditText messageEdit;
    private View sendBtn, pickImgBtn;
    private RecyclerView recyclerView;
    private int IMAGE_REQUEST_ID = 1;
    private long id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initializeView();

//        name = getIntent().getStringExtra("name");
        name = "VU TUONG BACH";
        Intent intent = getIntent();
        id = (long) intent.getSerializableExtra("idparkinglot");
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

        String string = s.toString().trim();

        if (string.isEmpty()) {
            resetMessageEdit();
        } else {

            sendBtn.setVisibility(View.VISIBLE);
            pickImgBtn.setVisibility(View.INVISIBLE);
        }

    }

    private void resetMessageEdit() {

        messageEdit.removeTextChangedListener(this);

        messageEdit.setText("");
        sendBtn.setVisibility(View.INVISIBLE);
        pickImgBtn.setVisibility(View.VISIBLE);

        messageEdit.addTextChangedListener(this);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private class SocketListener extends WebSocketListener {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);

            runOnUiThread(() -> {
                Toast.makeText(ChatActivity.this,
                        "Socket Connection Successful!",
                        Toast.LENGTH_SHORT).show();

                initializeView();
            });

        }

        @SneakyThrows
        @Override
        public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
            SaigonParkingMessage message = SaigonParkingMessage.parseFrom(bytes.toByteArray());
            runOnUiThread(() -> {
                try {
                    switch (message.getType()) {
                        case NOTIFICATION:
                            NotificationContent notificationContent = NotificationContent.parseFrom(message.getContent());
                            Log.d("BachMap", "Ket qua:" + notificationContent);
                            messageAdapter.doLoadInitData(Paper.book().read("historymessage"));
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
                            break;
                        case BOOKING_REJECT:
                            BookingRejectContent bookingRejectContent = BookingRejectContent.parseFrom(message.getContent());
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
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);

//            runOnUiThread(() -> {
//
//                try {
//                    JSONObject jsonObject = new JSONObject(text);
//                    jsonObject.put("isSent", false);
//
//                    messageAdapter.addItem(jsonObject);
//
//                    recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//
//            });

        }
    }

    private void initializeView() {

        messageEdit = findViewById(R.id.messageEdit);
        sendBtn = findViewById(R.id.sendBtn);
        pickImgBtn = findViewById(R.id.pickImgBtn);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setAdapter(messageAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        messageEdit.addTextChangedListener(this);

        sendBtn.setOnClickListener(v -> {

            JSONObject jsonObject = new JSONObject();
            try {
//                long tmpid = serviceStubs.getParkingLotServiceBlockingStub().getParkingLotEmployeeIdOfParkingLot(Int64Value.of(id)).getValue();
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                jsonObject.put("name", name);
                jsonObject.put("message", messageEdit.getText().toString());

                TextMessageContent textMessageContent = TextMessageContent.newBuilder()
                        .setSender(saigonParkingDatabase.getAuthKeyValueMap().get(SaigonParkingDatabase.USERNAME_KEY))
                        .setMessage(messageEdit.getText().toString())
                        .build();

                SaigonParkingMessage saigonParkingMessage = SaigonParkingMessage.newBuilder()

                        .setReceiverId(id)
                        .setClassification(SaigonParkingMessage.Classification.CUSTOMER_MESSAGE)
                        .setType(SaigonParkingMessage.Type.TEXT_MESSAGE)
                        .setTimestamp(timestamp.toString())
                        .setContent(textMessageContent.toByteString())
                        .build();

                sendWebSocketBinaryMessage(saigonParkingMessage);


                jsonObject.put("isSent", true);
                messageAdapter.addItem(jsonObject);

                recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

                resetMessageEdit();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        });

        pickImgBtn.setOnClickListener(v -> {

            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");

            startActivityForResult(Intent.createChooser(intent, "Pick image"),
                    IMAGE_REQUEST_ID);

        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_REQUEST_ID && resultCode == RESULT_OK) {

            try {
                InputStream is = getContentResolver().openInputStream(data.getData());
                Bitmap image = BitmapFactory.decodeStream(is);

                sendImage(image);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }

    }

    private void sendImage(Bitmap image) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

        String base64String = Base64.encodeToString(outputStream.toByteArray(),
                Base64.DEFAULT);

        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", name);
            jsonObject.put("image", base64String);

            sendWebSocketTextMessage(jsonObject.toString());

            jsonObject.put("isSent", true);

            messageAdapter.addItem(jsonObject);

            recyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}