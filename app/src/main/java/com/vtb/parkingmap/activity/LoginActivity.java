package com.vtb.parkingmap.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.bht.saigonparking.api.grpc.auth.AuthServiceGrpc;
import com.bht.saigonparking.api.grpc.auth.ValidateRequest;
import com.bht.saigonparking.api.grpc.auth.ValidateResponse;
import com.bht.saigonparking.api.grpc.user.UserRole;
import com.google.android.material.snackbar.Snackbar;
import com.vtb.parkingmap.R;
import com.vtb.parkingmap.SaigonParkingApplication;
import com.vtb.parkingmap.base.BaseSaigonParkingActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.grpc.StatusRuntimeException;
import lombok.Getter;

@SuppressLint("all")
@SuppressWarnings("all")
@Getter
public final class LoginActivity extends BaseSaigonParkingActivity {

    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

    @BindView(R.id.editusername)
    EditText _username;
    @BindView(R.id.input_password)
    EditText _passwordText;
    @BindView(R.id.btn_login)
    Button _loginButton;
    @BindView(R.id.link_signup)
    TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authServiceBlockingStub = serviceStubs.getAuthServiceBlockingStub();

        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login(v);
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
            }
        });
    }

    public void login(View v) {
        Log.d(TAG, "Login");

        String val = validate();

        switch (val) {
            case "Empty":
                return;
            case "Success":
                break;
            case "UNAVAILABLE":
                onLoginFailed(v, "Can't connect to Server!");
                return;
            default:
                onLoginFailed(v, "Username or Password was Wrong!");
                return;
        }
//        _loginButton.setEnabled(false);

//        ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
//                R.style.AppTheme_Dark_Dialog);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setMessage("Authenticating...");
//        progressDialog.show();

        String username = _username.getText().toString();
        String password = _passwordText.getText().toString();


        Intent intent = new Intent(getApplicationContext(), MapActivity.class);
        intent.putExtra("UserName", username);
        intent.putExtra("PassWord", password);
        startActivity(intent);
        finish();
        // TODO: Implement your own authentication logic here.

//        new android.os.Handler().postDelayed(
//                new Runnable() {
//                    @Override
//                    public void run() {
//                        // On complete call either onLoginSuccess or onLoginFailed
//                        onLoginSuccess();
//                        // onLoginFailed();
//                        progressDialog.dismiss();
//                    }
//                }, 500);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // Disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess() {
        _loginButton.setEnabled(true);
        finish();
    }

    public void onLoginFailed(View v, String temp) {
        Snackbar.make(v, temp, Snackbar.LENGTH_LONG)
                .setDuration(4000)
                .show();

        _loginButton.setEnabled(true);
    }

    public String validate() {
        String valid;

        String username = _username.getText().toString();
        String password = _passwordText.getText().toString();


        if (username.isEmpty()) {
            _username.setError("Pls input UserName");
            return "Empty";

        }

        if (password.isEmpty()) {
            _passwordText.setError("Pls Input password");
            return "Empty";
        }
        if (password.length() < 4 || password.length() > 10) {
            _passwordText.setError(" PassWord between 4 and 10 alphanumeric characters");
            return "Empty";
        }

        ValidateRequest validateRequest = ValidateRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .setRole(UserRole.CUSTOMER)
                .build();

        try {
            ValidateResponse loginResponse = authServiceBlockingStub
                    .validateUser(validateRequest);
            valid = "Success";

            saigonParkingDatabase.saveNewLoginInformation(username, loginResponse.getAccessToken(), loginResponse.getRefreshToken());
            ((SaigonParkingApplication) getApplicationContext()).initWebsocketConnection();

        } catch (StatusRuntimeException exception) {
            switch (String.valueOf(exception.getStatus().getCode())) {
                case "UNAVAILABLE":
                    valid = "UNAVAILABLE";
                    break;
                default:
                    valid = "False";
                    break;
            }
            saigonParkingExceptionHandler.handleCommunicationException(exception, LoginActivity.this);
        }

        return valid;
    }
}