package com.vtb.parkingmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.booking.BookingRating;
import com.bht.saigonparking.api.grpc.booking.DeleteBookingRatingRequest;
import com.bht.saigonparking.api.grpc.booking.UpdateBookingRatingRequest;
import com.google.protobuf.Empty;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;

public final class UpdateRatingActivity extends BaseSaigonParkingActivity {

    private BookingRating bookingRating;


    RatingBar mRatingBar;
    TextView mRatingScale;
    EditText mFeedback;
    Button btnUpdate;
    Button btnDelete;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_rating);
        Intent intent = getIntent();

        bookingRating = (BookingRating) intent.getSerializableExtra("bookingRating");


        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mRatingScale = (TextView) findViewById(R.id.tvRatingScale);
        mFeedback = (EditText) findViewById(R.id.etFeedback);
        btnUpdate = (Button) findViewById(R.id.btnSubmit);
        btnDelete = (Button) findViewById(R.id.btnDelete);


        mRatingBar.setRating(bookingRating.getRating());
        mFeedback.setText(bookingRating.getComment());


        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                mRatingScale.setText(String.valueOf(v));

                switch ((int) ratingBar.getRating()) {
                    case 1:
                        mRatingScale.setText("Very bad");
                        break;
                    case 2:
                        mRatingScale.setText("Need some improvement");
                        break;
                    case 3:
                        mRatingScale.setText("Good");
                        break;
                    case 4:
                        mRatingScale.setText("Great");
                        break;
                    case 5:
                        mRatingScale.setText("Awesome. I love it");
                        break;
                    default:
                        mRatingScale.setText("");
                }
            }
        });

        btnDelete.setOnClickListener(v -> {
            callApiWithExceptionHandling(() -> {
                DeleteBookingRatingRequest request = DeleteBookingRatingRequest.newBuilder()
                        .setBookingId(bookingRating.getBookingId())
                        .build();

                callApiWithExceptionHandling(() -> {
                    serviceStubs.getBookingServiceStub()
                            .deleteBookingRating(request, new StreamObserver<Empty>() {
                                @Override
                                public void onNext(Empty value) {
                                    Log.d("BachMap", "delete rating successfully");
                                }

                                @SneakyThrows
                                @Override
                                public void onError(Throwable t) {
                                    throw t;
                                }

                                @Override
                                public void onCompleted() {
                                    setResult(BookingHistoryDetailsActivity.DELETE_RATING_RESULT_CODE);
                                    finish();
                                }
                            });
                });
            });
        });

        btnUpdate.setOnClickListener(view -> {
            if (mRatingScale.getText().toString().isEmpty()) {
                Toast.makeText(UpdateRatingActivity.this, "Please rating for us", Toast.LENGTH_LONG).show();
            } else {

                UpdateBookingRatingRequest request = UpdateBookingRatingRequest.newBuilder()
                        .setComment(mFeedback.getText().toString())
                        .setRating((int) mRatingBar.getRating())
                        .setBookingId(bookingRating.getBookingId())
                        .build();

                callApiWithExceptionHandling(() -> {
                    serviceStubs.getBookingServiceStub()
                            .updateBookingRating(request, new StreamObserver<Empty>() {
                                @Override
                                public void onNext(Empty value) {
                                    Log.d("BachMap", "update rating successfully");
                                }

                                @SneakyThrows
                                @Override
                                public void onError(Throwable t) {
                                    throw t;
                                }

                                @Override
                                public void onCompleted() {
                                    mFeedback.setText("");
                                    mRatingBar.setRating(0);

                                    Intent result = new Intent();
                                    result.putExtra("updateBookingRatingRequest", request);
                                    setResult(BookingHistoryDetailsActivity.UPDATE_RATING_RESULT_CODE, result);
                                    finish();
                                }
                            });
                });
            }
        });
    }
}
