package com.vtb.parkingmap.websocket;


import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import com.bht.saigonparking.api.grpc.contact.BookingAcceptanceContent;
import com.bht.saigonparking.api.grpc.contact.BookingFinishContent;
import com.bht.saigonparking.api.grpc.contact.BookingProcessingContent;
import com.bht.saigonparking.api.grpc.contact.BookingRejectContent;
import com.bht.saigonparking.api.grpc.contact.ErrorContent;
import com.bht.saigonparking.api.grpc.contact.NotificationContent;
import com.bht.saigonparking.api.grpc.contact.SaigonParkingMessage;
import com.bht.saigonparking.api.grpc.contact.TextMessageContent;
import com.google.protobuf.InvalidProtocolBufferException;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.activity.BookingActivity;
import com.vtb.parkingmap.activity.ChatActivity;
import com.vtb.parkingmap.activity.MapActivity;
import com.vtb.parkingmap.activity.PlaceDetailsActivity;
import com.vtb.parkingmap.activity.RatingBookingActivity;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;
import com.vtb.parkingmap.database.SaigonParkingDatabaseEntity;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.Serializable;

import io.paperdb.Paper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

@RequiredArgsConstructor
public final class SaigonParkingWebSocketListener extends WebSocketListener {

    public static final int NORMAL_CLOSURE_STATUS = 1000;

    private final SaigonParkingApplication applicationContext;

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {

    }

    @SneakyThrows
    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
        BaseSaigonParkingActivity currentActivity = applicationContext.getCurrentActivity();
        currentActivity.runOnUiThread(() -> {
            try {
                SaigonParkingMessage message = SaigonParkingMessage.parseFrom(bytes.toByteArray());

                switch (message.getType()) {
                    case ERROR:
                        Log.d("BachMap", "Error booking again");
                        ErrorContent errorContent = ErrorContent.parseFrom(message.getContent());
                        String internalErrorCode = errorContent.getInternalErrorCode();
                        Log.d("BachMap", "Error booking again: " + internalErrorCode);
                        if ("SPE#00020".equals(internalErrorCode)) { /* CustomerHasOnGoingBooking */

                            AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);
                            alert
                                    .setTitle("Booking Confirm")
                                    .setMessage("You have on going booking !")
                                    .setNegativeButton("OK", (dialogInterface, i) ->
                                            Toast.makeText(currentActivity,
                                                    "Cancel booking successfully!", Toast.LENGTH_SHORT).show());

                            AlertDialog dialog = alert.create();
                            dialog.show();

                            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                                    .setTextColor(currentActivity
                                            .getResources()
                                            .getColor(R.color.colorPrimary));
                        }
                        break;
                    case NOTIFICATION:
                        NotificationContent notificationContent = NotificationContent.parseFrom(message.getContent());
                        Log.d("BachMap", "Ket qua:" + notificationContent);
                        break;
                    case TEXT_MESSAGE: {
                        try {
                            TextMessageContent textMessageContent = TextMessageContent.parseFrom(message.getContent());
                            Log.d("BachMap", "1" + textMessageContent);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("name", textMessageContent.getSender());
                            jsonObject.put("message", textMessageContent.getMessage());
                            jsonObject.put("isSent", false);
                            applicationContext.getMessageAdapter().addItem(jsonObject);
                            addNotification(textMessageContent.getSender(), textMessageContent.getMessage());

                        } catch (Exception exception) {
                            Log.d("BachMap", exception.getMessage());
                        }
                    }
                    break;
                    case BOOKING_ACCEPTANCE:
                        BookingAcceptanceContent bookingAcceptanceContent = BookingAcceptanceContent.parseFrom(message.getContent());

                        if (currentActivity instanceof BookingActivity) {
                            BookingActivity activity = (BookingActivity) currentActivity;
                            ((TextView) activity.findViewById(R.id.txtStatus)).setText("Accepted");
                            activity.findViewById(R.id.iconPendding).setVisibility(View.GONE);
                            activity.findViewById(R.id.iconAccept).setVisibility(View.VISIBLE);


                            if (applicationContext.getSaigonParkingDatabase().getBookingEntity()
                                    .equals(SaigonParkingDatabaseEntity.DEFAULT_INSTANCE)) {

                                SaigonParkingDatabaseEntity bookingEntity = SaigonParkingDatabaseEntity.builder()
                                        .id(activity.getId())
                                        .latitude(activity.getLatitude())
                                        .longitude(activity.getLongitude())
                                        .mylat(activity.getMylat())
                                        .mylong(activity.getMylong())
                                        .position3lat(activity.getPosition3lat())
                                        .position3long(activity.getPosition3long())
                                        .tmpType(activity.getTmpType())
                                        .bookingId(bookingAcceptanceContent.getBookingId())
                                        .build();

                                Log.d("BachMap", bookingEntity.toString());
                                applicationContext.getSaigonParkingDatabase().insertBookingTable(bookingEntity);
                            }
                        }
                        break;

                    case BOOKING_PROCESSING:
                        BookingProcessingContent bookingProcessingContent = BookingProcessingContent.parseFrom(message.getContent());
                        Log.d("BachMap", "Tai ID: " + bookingProcessingContent.getBookingId());

                        /* open Booking Activity + send bookingProcessingContent to Booking Activity */
                        Intent intent = new Intent(currentActivity, BookingActivity.class);


                        if (currentActivity instanceof PlaceDetailsActivity) {
                            PlaceDetailsActivity activity = (PlaceDetailsActivity) currentActivity;
                            intent.putExtra("parkingLot", activity.getParkingLot());
                            intent.putExtra("placedetaillat", (Serializable) activity.getLatitude());
                            intent.putExtra("placedetaillong", (Serializable) activity.getLongitude());
                            intent.putExtra("mylatfromplacedetail", (Serializable) activity.getMylat());
                            intent.putExtra("mylongfromplacedetail", (Serializable) activity.getMylong());
                            intent.putExtra("postion3lat", (Serializable) activity.getPosition3lat());
                            intent.putExtra("postion3long", (Serializable) activity.getPosition3long());
                            intent.putExtra("placedetailtype", (Serializable) activity.getTmpType());
                            intent.putExtra("idplacedetail", (Serializable) activity.getId());
                            intent.putExtra("licenseplate", (Serializable) activity.getLicensePlate());
                            intent.putExtra("parkinghour", (Serializable) activity.getAmountOfParkingHourString());
                            intent.putExtra("bookingProcessingContent", bookingProcessingContent);

                            if (activity.getPosition3lat() != 1234) {
                                intent.putExtra("position3lat", (Serializable) activity.getPosition3lat());
                                intent.putExtra("position3long", (Serializable) activity.getPosition3long());
                            }

                            SaigonParkingDatabaseEntity bookingEntity = SaigonParkingDatabaseEntity.builder()
                                    .id(activity.getId())
                                    .latitude(activity.getLatitude())
                                    .longitude(activity.getLongitude())
                                    .mylat(activity.getMylat())
                                    .mylong(activity.getMylong())
                                    .position3lat(activity.getPosition3lat())
                                    .position3long(activity.getPosition3long())
                                    .tmpType(activity.getTmpType())
                                    .bookingId(bookingProcessingContent.getBookingId())
                                    .build();

                            ((TextView) activity.findViewById(R.id.txtStatus)).setText("Accepted");

                            Log.d("BachMap", bookingEntity.toString());
                            applicationContext.getSaigonParkingDatabase().insertBookingTable(bookingEntity);
                        }

                        currentActivity.startActivity(intent);
                        currentActivity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                        break;

                    case BOOKING_REJECT:
                        try {
                            Log.d("BachMap", "ĐTAI");
                            BookingRejectContent bookingRejectContent = BookingRejectContent.parseFrom(message.getContent());
                            Log.d("BachMap", "1 : BOOKING REJ" + bookingRejectContent);
                            AlertDialog.Builder alert2 = new AlertDialog.Builder(currentActivity);
                            applicationContext.setIsBooked(false);
                            alert2.setTitle("Booking Confirm");
                            alert2.setMessage("Parking full slot! Please choose other parking lots!");
                            alert2.setPositiveButton("OK", (dialogInterface, i) -> {

                                Intent intent2 = new Intent(currentActivity, MapActivity.class);
                                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                currentActivity.startActivity(intent2);
                                currentActivity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                            });
                            AlertDialog dialog = alert2.create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(currentActivity.getResources().getColor(R.color.colorPrimary));
                        } catch (InvalidProtocolBufferException e) {
                            Toast.makeText(currentActivity, "ERROR!", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case BOOKING_FINISH:

                        try {
                            Log.d("BachMap", "ĐTAI");
                            BookingFinishContent bookingFinishContentContent = BookingFinishContent.parseFrom(message.getContent());
                            Log.d("BachMap", "1 : BOOKING Finish" + bookingFinishContentContent);
                            AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);

                            alert.setTitle("Booking Finish!");
                            alert.setMessage("Booking finished! Thanks for choose our service!");
                            alert.setPositiveButton("Back", (dialogInterface, i) -> {
                                //xử lý gọi database
                                applicationContext.getSaigonParkingDatabase().DeleteBookTable();
                                applicationContext.setIsBooked(false);
                                //xóa history message
                                Paper.book().delete("historymessage");

                                Intent intent2 = new Intent(currentActivity, RatingBookingActivity.class);

                                BookingActivity activity = (BookingActivity) currentActivity;
                                intent2.putExtra("idplacedetail", (Serializable) activity.getId());


                                intent2.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                currentActivity.startActivity(intent2);
                                currentActivity.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                currentActivity.finish();
                            });
                            AlertDialog dialog = alert.create();
                            dialog.show();
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                                    .setTextColor(currentActivity.getResources().getColor(R.color.colorPrimary));
                        } catch (InvalidProtocolBufferException e) {
                            Toast.makeText(currentActivity, "ERROR!", Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case IMAGE:
                        break;
                }

            } catch (
                    Exception e) {
                e.printStackTrace();
            }
        });
    }


    @Override
    public void onClosing(WebSocket webSocket, int code, @NotNull String reason) {
        webSocket.close(NORMAL_CLOSURE_STATUS, null);
        Log.d("BachMap", "On closing websocket");
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, Throwable t, Response response) {
        Log.d("BachMap", t.getMessage());
    }

    public void showAlertDialog(View v) {


    }

    private void addNotification(String name, String message) {

        // Builds your notification
        ComponentName componentName;
        ActivityManager activityManager = (ActivityManager) applicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        BaseSaigonParkingActivity currentActivity = applicationContext.getCurrentActivity();

        //noinspection deprecation
        componentName = activityManager.getRunningTasks(1).get(0).topActivity;
        String tmp = "com.vtb.parkingmap.activity.ChatActivity";

        if (!tmp.equals(componentName.getShortClassName())) {

            Intent notificationIntent = new Intent(currentActivity, ChatActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(currentActivity,
                    0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(currentActivity,
                    "ID_Notification")
                    .setSmallIcon(R.mipmap.ic_launcher_round)
                    .setWhen(System.currentTimeMillis())
                    .setContentTitle(name)
                    .setContentText(message)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVibrate(new long[5])
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent);

            // Add as notification
            NotificationManager manager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            manager.notify(0, builder.build());
        }
    }
}