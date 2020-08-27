package com.vtb.parkingmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bht.saigonparking.api.grpc.booking.Booking;
import com.bht.saigonparking.api.grpc.booking.BookingDetail;
import com.bht.saigonparking.api.grpc.booking.BookingHistory;
import com.bht.saigonparking.api.grpc.booking.BookingRating;
import com.bht.saigonparking.api.grpc.booking.BookingServiceGrpc;
import com.bht.saigonparking.api.grpc.booking.GetBookingRatingRequest;
import com.google.protobuf.StringValue;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import java.util.List;

public final class BookingHistoryDetailsActivity extends BaseSaigonParkingActivity {
    private TextView parkingLotName;
    private TextView licensePlate;
    private TextView createAt;
    private TextView acceptAt;
    private TextView finishAt;
    private TextView rejected;
    private TextView canceled;
    private LinearLayout lnAccept;
    private LinearLayout lnReject;
    private LinearLayout lnFinish;
    private LinearLayout lnCancel;
    private LinearLayout lnRating;
    private LinearLayout lnComment;
    private RatingBar ratingBar;
    private TextView comment;


    private String originBookingId;
    private Booking booking;
    private BookingDetail bookingDetail;
    private List<BookingHistory> bookingHistoryList;
    private BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        originBookingId = (String) intent.getSerializableExtra("originBookingId");
        bookingServiceBlockingStub = serviceStubs.getBookingServiceBlockingStub();
        setContentView(R.layout.activity_booking_history_details);

        callApiWithExceptionHandling(() -> {
            bookingDetail = bookingServiceBlockingStub
                    .getBookingDetailByBookingId(StringValue.of(originBookingId));
            booking = bookingDetail.getBooking();
            bookingHistoryList = bookingDetail.getHistoryList();
        });


        parkingLotName = findViewById(R.id.txtParkingLotName);
        licensePlate = findViewById(R.id.txtLicensePlate);
        createAt = findViewById(R.id.txtCreateAt);
        acceptAt = findViewById(R.id.txtAccept);
        finishAt = findViewById(R.id.txtFinish);
        rejected = findViewById(R.id.txtReject);
        canceled = findViewById(R.id.txtCancel);
        lnAccept = findViewById(R.id.lnAccept);
        lnReject = findViewById(R.id.lnReject);
        lnFinish = findViewById(R.id.lnFinish);
        lnCancel = findViewById(R.id.lnCancel);
        ratingBar = findViewById(R.id.ratingBar);
        comment = findViewById(R.id.txtComment);
        lnComment = findViewById(R.id.lnComment);
        lnRating = findViewById(R.id.lnRating);


        parkingLotName.setText(booking.getParkingLotName());
        licensePlate.setText(booking.getLicensePlate().toUpperCase());
        if (booking.getIsRated()) {
            callApiWithExceptionHandling(() -> {
                GetBookingRatingRequest getBookingRatingRequest = GetBookingRatingRequest.newBuilder()
                        .setBookingId(originBookingId)
                        .build();
                BookingRating bookingRating = bookingServiceBlockingStub.getBookingRating(getBookingRatingRequest);
                String commentContent = bookingRating.getComment();
                if (commentContent.isEmpty()) {
                    lnComment.setVisibility(View.GONE);
                } else {
                    comment.setText(commentContent);
                    lnComment.setVisibility(View.VISIBLE);
                }
                ratingBar.setRating(bookingRating.getRating());
                lnRating.setVisibility(View.VISIBLE);
            });
        }


        bookingHistoryList.forEach(history -> {
            switch (history.getStatus()) {
                case CREATED:
                    createAt.setText(history.getTimestamp());
                    break;
                case REJECTED:
                    rejected.setText(history.getTimestamp());
                    lnReject.setVisibility(View.VISIBLE);
                    break;
                case ACCEPTED:
                    acceptAt.setText(history.getTimestamp());
                    lnAccept.setVisibility(View.VISIBLE);
                    break;
                case CANCELLED:
                    canceled.setText(history.getTimestamp());
                    lnCancel.setVisibility(View.VISIBLE);
                    break;
                case FINISHED:
                    finishAt.setText(history.getTimestamp());
                    lnFinish.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        });


    }
}
