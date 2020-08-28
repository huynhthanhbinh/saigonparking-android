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

public final class CreateRatingActivity extends BaseSaigonParkingActivity {

    private Boolean isStartForResult;
    private String originBookingId;
    private RatingBar ratingBar;
    private TextView txtRatingScale;
    private EditText txtFeedback;
    private Button btnSendFeedback;
    private Button btnFeedbackLater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindLayoutFields();
        initAllListeners();
    }

    private void bindLayoutFields() {
        setContentView(R.layout.activity_create_rating);
        Intent intent = getIntent();
        isStartForResult = intent.getBooleanExtra("isStartForResult", false);
        originBookingId = (String) intent.getSerializableExtra("idbooking");
        ratingBar = (RatingBar) findViewById(R.id.ratingBar);
        txtRatingScale = (TextView) findViewById(R.id.tvRatingScale);
        txtFeedback = (EditText) findViewById(R.id.etFeedback);
        btnSendFeedback = (Button) findViewById(R.id.btnSubmit);
        btnFeedbackLater = (Button) findViewById(R.id.btnfeedBackLater);
    }

    private void initAllListeners() {
        ratingBar.setOnRatingBarChangeListener(this::onRatingBarValueChange);
        btnSendFeedback.setOnClickListener(this::onClickSendFeedBack);
        btnFeedbackLater.setOnClickListener(isStartForResult
                ? this::onClickFeedBackLaterBackToBookingDetailActivity
                : this::onClickFeedBackLaterBackToMapActivity);
    }

    private void onRatingBarValueChange(RatingBar ratingBar, float v, boolean b) {
        txtRatingScale.setText(String.valueOf(v));
        switch ((int) ratingBar.getRating()) {
            case 1:
                txtRatingScale.setText(R.string.txt_rating_scale_1);
                break;
            case 2:
                txtRatingScale.setText(R.string.txt_rating_scale_2);
                break;
            case 3:
                txtRatingScale.setText(R.string.txt_rating_scale_3);
                break;
            case 4:
                txtRatingScale.setText(R.string.txt_rating_scale_4);
                break;
            case 5:
                txtRatingScale.setText(R.string.txt_rating_scale_5);
                break;
            default:
                txtRatingScale.setText("");
        }
    }

    private void onClickSendFeedBack(View view) {
        if (txtRatingScale.getText().toString().isEmpty()) {
            Toast.makeText(CreateRatingActivity.this,
                    "Please rating for us", Toast.LENGTH_LONG).show();
        } else {
            Log.d("BachMap", "Rating: " + ratingBar.getRating());
            CreateBookingRatingRequest request = CreateBookingRatingRequest.newBuilder()
                    .setComment(txtFeedback.getText().toString())
                    .setRating((int) ratingBar.getRating())
                    .setBookingId(originBookingId)
                    .build();

            callApiWithExceptionHandling(() -> serviceStubs.getBookingServiceStub()
                    .createBookingRating(request, new StreamObserver<Empty>() {
                        @Override
                        public void onNext(Empty value) {
                            Log.d("BachMap", "create rating successfully");
                        }

                        @SneakyThrows
                        @Override
                        public void onError(Throwable t) {
                            throw t;
                        }

                        @Override
                        public void onCompleted() {
                            if (isStartForResult) {
                                Intent result = new Intent();
                                result.putExtra("createBookingRatingRequest", request);
                                setResult(BookingHistoryDetailsActivity.CREATE_RATING_RESULT_CODE, result);
                            }
                            finish();
                        }
                    }));
        }
    }

    private void onClickFeedBackLaterBackToMapActivity(View view) {
        Intent feedbacklater = new Intent(CreateRatingActivity.this, MapActivity.class);
        feedbacklater.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(feedbacklater);
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }

    private void onClickFeedBackLaterBackToBookingDetailActivity(View view) {
        finish();
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    }
}