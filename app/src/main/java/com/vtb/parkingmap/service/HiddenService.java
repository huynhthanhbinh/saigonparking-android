package com.vtb.parkingmap.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.parkinglot.ParkingLotServiceGrpc;
import com.google.protobuf.Int64Value;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingService;

import java.io.Serializable;

import io.grpc.StatusRuntimeException;


public final class HiddenService extends BaseSaigonParkingService {

    long idplacedetail;
    private Handler mHandler = new Handler();
    private boolean isAvailable = true;
    ParkingLotServiceGrpc.ParkingLotServiceBlockingStub parkingLotServiceBlockingStub;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        parkingLotServiceBlockingStub = serviceStubs.getParkingLotServiceBlockingStub();
    }

    private Runnable mToastRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                isAvailable = parkingLotServiceBlockingStub.checkAvailability(Int64Value.of(idplacedetail)).getValue();
            } catch (StatusRuntimeException exception) {
                saigonParkingExceptionHandler.handleCommunicationException(exception, HiddenService.this);
            }


            if (isAvailable) {
                Toast.makeText(HiddenService.this, "Parkinglot available ", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(HiddenService.this, "Parkinglot is full! Please choose other parkinglot ", Toast.LENGTH_LONG).show();
                addNotification();
                Intent intent = new Intent();
                intent.setAction("dialogFlag.Broadcast");
                sendBroadcast(intent);
                onDestroy();
            }
            mHandler.postDelayed(this, 60000);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        idplacedetail = (long) intent.getSerializableExtra("idplacedetail");
        mToastRunnable.run();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
//        Toast.makeText(HiddenService.this, "Stop Service", Toast.LENGTH_SHORT).show();
        mHandler.removeCallbacks(mToastRunnable);
        Intent myIntent = new Intent(HiddenService.this, HiddenService.class);
        myIntent.putExtra("idplacedetail", (Serializable) idplacedetail);
        stopService(myIntent);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void addNotification() {
        // Builds your notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "ID_Notification")
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setWhen(System.currentTimeMillis())
                .setContentTitle("Notice")
                .setContentText("Parkinglot is full!")
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setVibrate(new long[5])
                .setAutoCancel(true);
        // Add as notification
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(0, builder.build());
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
}