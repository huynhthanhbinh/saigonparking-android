package com.vtb.parkingmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.bht.saigonparking.api.grpc.booking.Booking;
import com.bht.saigonparking.api.grpc.booking.BookingDetail;
import com.bht.saigonparking.api.grpc.booking.BookingHistory;
import com.bht.saigonparking.api.grpc.booking.BookingRating;
import com.bht.saigonparking.api.grpc.booking.BookingServiceGrpc;
import com.bht.saigonparking.api.grpc.booking.CreateBookingRatingRequest;
import com.bht.saigonparking.api.grpc.booking.GetBookingRatingRequest;
import com.bht.saigonparking.api.grpc.booking.UpdateBookingRatingRequest;
import com.google.protobuf.StringValue;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import java.util.List;
import java.util.Objects;

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
    private Button btnUpdate;
    private TextView tv_lastupdated;
    /* request code: 1X, result code: 2X */
    private static final int UPDATE_RATING_REQUEST_CODE = 12;
    public static final int UPDATE_RATING_RESULT_CODE = 22;
    public static final int DELETE_RATING_RESULT_CODE = 24;

    private static final int CREATE_RATING_REQUEST_CODE = 14;
    public static final int CREATE_RATING_RESULT_CODE = 26;

    private String originBookingId;
    private Booking booking;
    private BookingDetail bookingDetail;
    private List<BookingHistory> bookingHistoryList;
    private BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub;
    private BookingRating bookingRating;


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


        getWindow().setSharedElementEnterTransition(new ChangeBounds().setDuration(500));
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
        btnUpdate = findViewById(R.id.btnUpdateRating);
        tv_lastupdated = findViewById(R.id.tv_lastupdated);

        parkingLotName.setText(booking.getParkingLotName());
        licensePlate.setText(booking.getLicensePlate().toUpperCase());
        if (booking.getIsRated()) {
            callApiWithExceptionHandling(() -> {
                GetBookingRatingRequest getBookingRatingRequest = GetBookingRatingRequest.newBuilder()
                        .setBookingId(originBookingId)
                        .build();
                bookingRating = bookingServiceBlockingStub.getBookingRating(getBookingRatingRequest);
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

        initOnButtonUpdateClicked();
        tv_lastupdated.setText(booking.getCreatedAt());
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

    private void initOnButtonUpdateClicked() {
        btnUpdate.setOnClickListener(view -> {
            if (booking.getIsRated()) {
                Intent intent1 = new Intent(BookingHistoryDetailsActivity.this, UpdateRatingActivity.class);
                intent1.putExtra("bookingRating", bookingRating);
                startActivityForResult(intent1, UPDATE_RATING_REQUEST_CODE);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            } else {
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Notice!");
                alert.setMessage("You didn't rated before! Do you want to rate this booking?");
                alert.setNegativeButton("Yes", (dialogInterface, i) -> {
                    Intent intent2 = new Intent(BookingHistoryDetailsActivity.this
                            , CreateRatingActivity.class);
                    intent2.putExtra("isStartForResult", true);
                    intent2.putExtra("idbooking", originBookingId);
                    startActivityForResult(intent2, CREATE_RATING_REQUEST_CODE);
                    overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                });
                alert.setPositiveButton("No", (dialogInterface, i) -> {

                });
                AlertDialog dialog = alert.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setTextColor(getResources().getColor(R.color.colorPrimary));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                        .setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_RATING_REQUEST_CODE) {
            if (resultCode == UPDATE_RATING_RESULT_CODE) {
                UpdateBookingRatingRequest request = (UpdateBookingRatingRequest) Objects
                        .requireNonNull(data)
                        .getSerializableExtra("updateBookingRatingRequest");

                int updatedRating = request.getRating();
                String updatedComment = request.getComment();

                comment.setText(updatedComment);
                ratingBar.setRating(updatedRating);
                lnComment.setVisibility(updatedComment.isEmpty() ? View.GONE : View.VISIBLE);

                bookingRating = BookingRating.newBuilder(bookingRating)
                        .setComment(updatedComment)
                        .setRating(updatedRating)
                        .build();

            } else if (resultCode == DELETE_RATING_RESULT_CODE) {
                lnComment.setVisibility(View.GONE);
                lnRating.setVisibility(View.GONE);
                booking = Booking.newBuilder(booking).setIsRated(false).build();

            } else { /* resultCode == 0 (CANCEL/BACKPRESS from another activity) */
                // ...
            }
        } else if (requestCode == CREATE_RATING_REQUEST_CODE) {
            if (resultCode == CREATE_RATING_RESULT_CODE) {
                CreateBookingRatingRequest request = (CreateBookingRatingRequest) Objects
                        .requireNonNull(data)
                        .getSerializableExtra("createBookingRatingRequest");

                int createdRating = request.getRating();
                String createdComment = request.getComment();

                comment.setText(createdComment);
                ratingBar.setRating(createdRating);

                bookingRating = BookingRating.newBuilder(bookingRating)
                        .setComment(createdComment)
                        .setRating(createdRating)
                        .build();

                lnComment.setVisibility(createdComment.isEmpty() ? View.GONE : View.VISIBLE);
                lnRating.setVisibility(View.VISIBLE);
                booking = Booking.newBuilder(booking).setIsRated(true).build();

            } else { /* resultCode == 0 (CANCEL/BACKPRESS from another activity) */
                // ...
            }
        }
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }
}