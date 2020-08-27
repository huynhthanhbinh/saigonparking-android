package com.vtb.parkingmap.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bht.saigonparking.api.grpc.booking.CreateBookingRatingRequest;
import com.google.protobuf.Empty;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import io.grpc.stub.StreamObserver;
import lombok.SneakyThrows;

public final class RatingBookingActivity extends BaseSaigonParkingActivity {
    private String idbooking;


    RatingBar mRatingBar;
    TextView mRatingScale;
    EditText mFeedback;
    Button mSendFeedback;
    Button mFeedbackLater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating_booking);
        Intent intent = getIntent();
        idbooking = (String) intent.getSerializableExtra("idbooking");

        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mRatingScale = (TextView) findViewById(R.id.tvRatingScale);
        mFeedback = (EditText) findViewById(R.id.etFeedback);
        mSendFeedback = (Button) findViewById(R.id.btnSubmit);
        mFeedbackLater = (Button) findViewById(R.id.btnfeedBackLater);


        mFeedbackLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent feedbacklater = new Intent(RatingBookingActivity.this, MapActivity.class);
                feedbacklater.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(feedbacklater);
                overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
            }
        });

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

        mSendFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mRatingScale.getText().toString().isEmpty()) {
                    Toast.makeText(RatingBookingActivity.this, "Please rating for us", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("BachMap", "Rating: " + mRatingBar.getRating());
                    CreateBookingRatingRequest request = CreateBookingRatingRequest.newBuilder()
                            .setComment(mFeedback.getText().toString())
                            .setRating((int) mRatingBar.getRating())
                            .setBookingId(idbooking)
                            .build();

                    callApiWithExceptionHandling(() -> {
                        serviceStubs.getBookingServiceStub()
                                .createBookingRating(request, new StreamObserver<Empty>() {
                                    @Override
                                    public void onNext(Empty value) {
                                        Log.d("BachMap", "create rating successfully");
//                                        Intent sendFeedBack = new Intent(RatingBookingActivity.this, PlaceDetailsActivity.class);
//                                        startActivity(sendFeedBack);
//                                        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
                                        finish();
                                    }

                                    @SneakyThrows
                                    @Override
                                    public void onError(Throwable t) {
                                        throw t;
                                    }

                                    @Override
                                    public void onCompleted() {

                                    }
                                });
                    });
                    mFeedback.setText("");
                    mRatingBar.setRating(0);

                    Toast.makeText(RatingBookingActivity.this, "Thank you for sharing your feedback", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
